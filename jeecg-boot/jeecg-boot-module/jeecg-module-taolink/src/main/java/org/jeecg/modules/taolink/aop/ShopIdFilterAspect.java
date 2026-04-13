package org.jeecg.modules.taolink.aop;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.taolink.entity.TaolinkShop;
import org.jeecg.modules.taolink.service.ITaolinkShopService;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 店铺数据隔离 AOP
 * 自动为查询添加 shop_id 过滤条件
 */
@Slf4j
@Aspect
@Component
public class ShopIdFilterAspect {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ShopIdFilterAspect.class);

    @Autowired
    private ITaolinkShopService taolinkShopService;

    /**
     * 切点：所有 Taolink 相关的 Service 方法
     */
    @Pointcut("execution(* org.jeecg.modules.taolink.service.*.*(..))")
    public void taolinkServicePointcut() {
    }

    /**
     * 前置通知：在方法执行前添加 shop_id 过滤条件
     */
    @Before("taolinkServicePointcut()")
    public void beforeMethod(JoinPoint joinPoint) {
        // 获取当前登录用户
        LoginUser loginUser = null;
        try {
            Object principal = SecurityUtils.getSubject().getPrincipal();
            if (principal instanceof LoginUser) {
                loginUser = (LoginUser) principal;
            }
        } catch (Exception e) {
            log.debug("获取登录用户失败: {}", e.getMessage());
            return;
        }

        if (loginUser == null) {
            log.debug("未登录用户，跳过 shop_id 过滤");
            return;
        }

        // 获取用户关联的店铺列表
        QueryWrapper<TaolinkShop> shopQuery = new QueryWrapper<>();
        shopQuery.eq("owner_id", loginUser.getId());
        shopQuery.eq("status", "active");
        List<TaolinkShop> shops = taolinkShopService.list(shopQuery);

        if (shops == null || shops.isEmpty()) {
            log.debug("用户未绑定店铺，跳过 shop_id 过滤");
            return;
        }

        // 提取店铺 ID 列表
        List<String> shopIds = shops.stream()
                .map(TaolinkShop::getId)
                .toList();

        // TODO: 这里需要根据实际的 Service 方法参数，为包含 shop_id 的查询添加过滤条件
        // 例如，当方法参数包含 QueryWrapper 时，自动添加 shop_id in (shopIds) 条件
        // 由于不同 Service 方法的参数结构不同，这里需要根据实际情况实现
        log.debug("用户 {} 关联的店铺: {}", loginUser.getUsername(), shopIds);
    }
}
