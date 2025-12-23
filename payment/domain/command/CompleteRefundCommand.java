package com.bytz.modules.cms.payment.domain.command;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 完成退款命令对象
 * Complete Refund Command
 *
 * <p>用例来源：UC-RM-006 退款完成确认</p>
 * <p>使用场景：退款渠道完成退款后通知退款模块</p>
 * <p>回调通过paymentChannel+channelTransactionId唯一标识交易，不需要refundId</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteRefundCommand {

    /**
     * 退款渠道（与channelTransactionId一起唯一标识交易）
     */
    private PaymentChannel paymentChannel;

    /**
     * 渠道交易记录ID（通过该ID查找关联的退款单）
     */
    private String channelTransactionId;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 渠道响应信息（JSON格式，失败时记录错误信息）
     */
    private String channelResponse;

    /**
     * 渠道交易号（可选补录）
     */
    private String channelTransactionNumber;

    /**
     * 完成时间
     */
    private LocalDateTime completedTime;
}
