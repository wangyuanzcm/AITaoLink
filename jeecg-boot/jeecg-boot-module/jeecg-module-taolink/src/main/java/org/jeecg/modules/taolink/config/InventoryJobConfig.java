package org.jeecg.modules.taolink.config;

import org.jeecg.modules.taolink.job.InventoryAlertCheckJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 库存相关定时任务配置
 */
@Configuration
public class InventoryJobConfig {

    /**
     * 库存预警检查任务
     */
    @Bean
    public JobDetail inventoryAlertCheckJobDetail() {
        return JobBuilder.newJob(InventoryAlertCheckJob.class)
                .withIdentity("inventoryAlertCheckJob")
                .storeDurably()
                .build();
    }

    /**
     * 库存预警检查任务触发器
     * 每天凌晨2点执行一次
     */
    @Bean
    public Trigger inventoryAlertCheckJobTrigger() {
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule("0 0 2 * * ?");
        return TriggerBuilder.newTrigger()
                .forJob(inventoryAlertCheckJobDetail())
                .withIdentity("inventoryAlertCheckJobTrigger")
                .withSchedule(scheduleBuilder)
                .build();
    }
}
