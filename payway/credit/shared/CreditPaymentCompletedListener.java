package com.bytz.modules.cms.payway.credit.shared;

import com.bytz.modules.cms.payment.shared.event.PaymentCompletedEvent;
import com.bytz.modules.cms.payway.credit.domain.CreditWalletDomainService;
import com.bytz.modules.cms.payway.credit.domain.command.CompleteRepaymentCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 信用支付单完成事件监听器
 * 专门处理信用还款类型的支付完成事件
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CreditPaymentCompletedListener {

    private final CreditWalletDomainService creditWalletDomainService;

    /**
     * 处理信用还款完成事件
     * 当支付单完成且支付类型为信用还款时触发
     *
     * @param event 支付完成事件，包含订单ID、支付金额、支付类型等信息
     *              条件说明：支付类型为信用还款
     *              业务逻辑：
     *              1. 检查关联业务ID是否存在
     *              2. 构建还款完成命令
     *              3. 调用领域服务完成还款流程
     */
    @EventListener(condition = "#event.paymentType.isCreditRepayment() == true")
    public void handleCreditRepaymentCompletedEvent(PaymentCompletedEvent event) {
        log.info("[信用支付监听器] 开始处理信用还款完成事件 - 支付ID: {}, 订单ID: {}",
                event.getPaymentId(), event.getOrderId());

        // 检查相关业务ID
        if (StringUtils.isBlank(event.getRelatedBusinessId())) {
            log.warn("[信用支付监听器] 信用还款事件缺少相关业务ID，无法处理 - 支付ID: {}", event.getPaymentId());
            return;
        }
        log.debug("[信用支付监听器] 信用还款事件相关业务ID: {}, 支付ID: {}",
                event.getRelatedBusinessId(), event.getPaymentId());

        // 构建还款完成命令
        CompleteRepaymentCommand command = CompleteRepaymentCommand.builder()
                .billId(event.getRelatedBusinessId())
                .repaymentTime(event.getCompletedTime())
                .build();
        log.debug("[信用支付监听器] 构建还款完成命令 - 账单ID: {}, 还款时间: {}",
                command.getBillId(), command.getRepaymentTime());

        // 调用领域服务完成还款
        creditWalletDomainService.completeRepayment(command);
        log.info("[信用支付监听器] 信用还款完成事件处理成功 - 支付ID: {}, 账单ID: {}",
                event.getPaymentId(), event.getRelatedBusinessId());
    }
}
