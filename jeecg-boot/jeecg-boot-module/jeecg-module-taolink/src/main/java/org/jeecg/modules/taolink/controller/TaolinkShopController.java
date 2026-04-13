package org.jeecg.modules.taolink.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.taolink.entity.TaolinkShop;
import org.jeecg.modules.taolink.integrations.taobao.TaobaoOauthClient;
import org.jeecg.modules.taolink.service.ITaolinkShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Tag(name = "TaoLink-店铺")
@RestController
@RequestMapping("/taolink/shop")
public class TaolinkShopController extends JeecgController<TaolinkShop, ITaolinkShopService> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TaolinkShopController.class);
    @Autowired
    private ITaolinkShopService taolinkShopService;
    @Autowired
    private TaobaoOauthClient taobaoOauthClient;

    @Operation(summary = "店铺列表")
    @GetMapping(value = "/list")
    public Result<IPage<TaolinkShop>> list(TaolinkShop shop,
                                            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                            HttpServletRequest req) {
        QueryWrapper<TaolinkShop> queryWrapper = QueryGenerator.initQueryWrapper(shop, req.getParameterMap());
        Page<TaolinkShop> page = new Page<>(pageNo, pageSize);
        IPage<TaolinkShop> pageList = taolinkShopService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @Operation(summary = "通过ID查询")
    @GetMapping(value = "/queryById")
    public Result<TaolinkShop> queryById(@RequestParam(name = "id") String id) {
        TaolinkShop entity = taolinkShopService.getById(id);
        if (entity == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(entity);
    }

    @Operation(summary = "获取授权URL")
    @GetMapping(value = "/authorizeUrl")
    public Result<String> getAuthorizeUrl(HttpServletRequest req) {
        // 生成随机state参数
        String state = UUID.randomUUID().toString();
        // 存储state到session，用于回调时验证
        req.getSession().setAttribute("taobao_oauth_state", state);
        try {
            String authorizeUrl = taobaoOauthClient.generateAuthorizeUrl(state);
            return Result.OK(authorizeUrl);
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "OAuth回调")
    @GetMapping(value = "/oauthCallback")
    public void oauthCallback(@RequestParam(name = "code") String code,
                              @RequestParam(name = "state") String state,
                              HttpServletRequest req,
                              HttpServletResponse resp) throws IOException {
        // 验证state参数
        String storedState = (String) req.getSession().getAttribute("taobao_oauth_state");
        if (!state.equals(storedState)) {
            resp.sendRedirect("/taolink/shop?error=invalid_state");
            return;
        }

        try {
            // 获取当前登录用户
            LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (loginUser == null) {
                resp.sendRedirect("/taolink/shop?error=not_login");
                return;
            }

            // 通过授权码获取令牌
            TaobaoOauthClient.TaobaoTokenInfo tokenInfo = taobaoOauthClient.getToken(code);
            if (tokenInfo == null) {
                resp.sendRedirect("/taolink/shop?error=token_failed");
                return;
            }

            // 构建店铺信息
            TaolinkShop shop = taobaoOauthClient.buildShopInfo(tokenInfo, loginUser.getUsername(), loginUser.getId());

            // 检查是否已绑定
            QueryWrapper<TaolinkShop> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("taobao_seller_nick", shop.getTaobaoSellerNick());
            queryWrapper.eq("owner_id", loginUser.getId());
            TaolinkShop existingShop = taolinkShopService.getOne(queryWrapper);

            if (existingShop != null) {
                // 更新现有店铺
                existingShop.setApiSessionKey(shop.getApiSessionKey());
                existingShop.setApiExpireAt(shop.getApiExpireAt());
                existingShop.setStatus("active");
                taolinkShopService.updateById(existingShop);
            } else {
                // 保存新店铺
                taolinkShopService.save(shop);
            }

            // 重定向到店铺列表页
            resp.sendRedirect("/taolink/shop?success=true");
        } catch (IllegalStateException e) {
            resp.sendRedirect("/taolink/shop?error=config_missing");
        } catch (Exception e) {
            log.error("OAuth回调处理失败", e);
            resp.sendRedirect("/taolink/shop?error=callback_failed");
        }
    }

    @Operation(summary = "重新授权")
    @PostMapping(value = "/{id}/reauthorize")
    public Result<String> reauthorize(@PathVariable(name = "id") String id, HttpServletRequest req) {
        TaolinkShop shop = taolinkShopService.getById(id);
        if (shop == null) {
            return Result.error("未找到对应店铺");
        }

        // 生成随机state参数，并包含店铺ID
        String state = UUID.randomUUID().toString() + "_" + id;
        // 存储state到session
        req.getSession().setAttribute("taobao_oauth_state", state);
        try {
            String authorizeUrl = taobaoOauthClient.generateAuthorizeUrl(state);
            return Result.OK(authorizeUrl);
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "绑定店铺")
    @PostMapping(value = "/bind")
    public Result<String> bind(@RequestBody TaolinkShop shop) {
        taolinkShopService.save(shop);
        return Result.OK("绑定成功！");
    }

    @Operation(summary = "解绑店铺")
    @PostMapping(value = "/unbind")
    public Result<String> unbind(@RequestParam(name = "id") String id) {
        TaolinkShop shop = taolinkShopService.getById(id);
        if (shop == null) {
            return Result.error("未找到对应店铺");
        }
        // 软解绑：改状态为unbound，不删除数据
        shop.setStatus("unbound");
        // 清空授权信息
        shop.setApiSessionKey(null);
        taolinkShopService.updateById(shop);
        return Result.OK("解绑成功");
    }

    @Operation(summary = "编辑店铺")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody TaolinkShop shop) {
        taolinkShopService.updateById(shop);
        return Result.OK("更新成功！");
    }

    @Operation(summary = "删除店铺")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id") String id) {
        taolinkShopService.removeById(id);
        return Result.OK("删除成功!");
    }
}
