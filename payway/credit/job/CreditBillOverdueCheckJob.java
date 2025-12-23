package com.bytz.modules.cms.payway.credit.job;

import com.bytz.modules.cms.payway.credit.domain.RiskControlDomainService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 信用账单逾期检查定时任务
 * 用于定期检查信用账单是否逾期，并进行相关处理
 */
@Component
@Slf4j
public class CreditBillOverdueCheckJob implements Job {

    @Autowired
    private RiskControlDomainService riskControlDomainService;
    
    /**
     * 执行定时任务的入口方法
     * @param context Quartz JobExecutionContext
     * @throws JobExecutionException 任务执行异常
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("开始执行信用账单逾期检查任务");
        try {
            // 检测逾期账单
            List<RiskControlDomainService.OverdueInfo> overdueInfos = riskControlDomainService.detectOverdueBills();
            
            // 如果有逾期账单，自动冻结钱包
            if (!overdueInfos.isEmpty()) {
                riskControlDomainService.autoFreezeOverdueWallets(overdueInfos);
            }
            
            log.info("信用账单逾期检查任务执行完成，共检测到 {} 个逾期钱包", overdueInfos.size());
        } catch (Exception e) {
            log.error("信用账单逾期检查任务执行失败", e);
            throw new JobExecutionException("信用账单逾期检查任务执行失败", e);
        }
    }
}
