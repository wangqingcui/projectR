package com.bytz.modules.cms.payment.domain.command;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 应用退款成功回调命令对象
 * Apply Refund Command
 *
 * <p>用例来源：UC-RM-007 退款金额更新</p>
 * <p>使用场景：退款渠道回调成功后更新退款单金额（保留2位小数HALF_UP）</p>
 * <p>注意：不包含refundId，因为此命令用于聚合根实例方法refund.applyRefund(command)</p>
 * <p>领域服务handleRefundComplete会先加载聚合根，再调用实例方法</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyRefundCommand {

    /**
     * 本次退款金额（保留2位小数HALF_UP）
     */
    private BigDecimal amount;

    /**
     * 退款渠道
     */
    private PaymentChannel channel;

    /**
     * 渠道交易号
     */
    private String channelTransactionNumber;

    /**
     * 完成时间
     */
    private LocalDateTime completedTime;
}
