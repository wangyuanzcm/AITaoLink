package org.jeecg.modules.taolink.config;

import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus配置类
 * 注册租户/店铺数据隔离拦截器
 * 注意：不定义独立的mybatisPlusInterceptor bean，避免与系统默认配置冲突
 * 租户隔离功能已在系统级MybatisPlusSaasConfig中配置
 */
@Configuration
public class MyBatisPlusConfig {
    // 租户隔离功能已在系统级MybatisPlusSaasConfig中配置
    // 本模块通过TenantShopInterceptor实现自定义的租户隔离逻辑
}

