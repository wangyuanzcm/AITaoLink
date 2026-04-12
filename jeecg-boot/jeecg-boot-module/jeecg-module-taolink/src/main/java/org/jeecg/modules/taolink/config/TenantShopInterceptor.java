package org.jeecg.modules.taolink.config;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import org.jeecg.common.system.vo.LoginUser;
import org.apache.shiro.SecurityUtils;

/**
 * 租户/店铺数据隔离拦截器
 * 自动为SQL添加tenant_id和shop_id过滤条件
 */
public class TenantShopInterceptor implements TenantLineHandler {

    @Override
    public Expression getTenantId() {
        // 获取当前登录用户的租户ID
        try {
            Object principal = SecurityUtils.getSubject().getPrincipal();
            if (principal instanceof LoginUser) {
                LoginUser loginUser = (LoginUser) principal;
                if (loginUser != null && loginUser.getOrgCode() != null) {
                    return new StringValue(loginUser.getOrgCode());
                }
            }
        } catch (Exception e) {
            // 忽略异常
        }
        // 返回null表示不添加租户过滤条件
        return null;
    }

    @Override
    public String getTenantIdColumn() {
        return "sys_org_code";
    }

    @Override
    public boolean ignoreTable(String tableName) {
        // 系统表和全局共享的表不需要租户过滤
        if (tableName.startsWith("sys_") || 
            tableName.startsWith("act_") || 
            tableName.startsWith("qrtz_") || 
            tableName.startsWith("jeecg_") || 
            "taolink_inventory".equals(tableName) || 
            "taolink_inventory_movement".equals(tableName) || 
            "taolink_warehouse".equals(tableName)) {
            return true;
        }
        // 只对taolink相关的表进行租户过滤
        return !tableName.startsWith("taolink_");
    }

    // 注意：MyBatis-Plus的TenantLineHandler只支持单一租户列
    // 对于shop_id的过滤，需要在Service层通过AOP或手动添加条件
    // 或者使用自定义的多租户插件来实现
}

