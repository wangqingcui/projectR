package com.bytz.modules.cms.payway.credit.job;

import com.bytz.modules.cms.payway.credit.domain.TemporaryCreditDomainService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 临时信用自动过期定时任务
 * 用于定期检查并处理临时信用的过期逻辑
 */
@Component
@Slf4j
public class TemporaryCreditExpireJob implements Job {

    @Autowired
    private TemporaryCreditDomainService temporaryCreditDomainService;
    
    /**
     * 执行定时任务的入口方法
     * @param context Quartz JobExecutionContext
     * @throws JobExecutionException 任务执行异常
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("开始执行临时信用自动过期检测任务");
        try {
            temporaryCreditDomainService.detectExpiredTemporaryCredits();
            log.info("临时信用自动过期检测任务执行完成");
        } catch (Exception e) {
            log.error("临时信用自动过期检测任务执行失败", e);
            throw new JobExecutionException("临时信用自动过期检测任务执行失败", e);
        }
    }
}
