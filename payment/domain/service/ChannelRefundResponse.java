package com.bytz.modules.cms.payment.domain.service;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.domain.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 渠道退款响应
 * Channel Refund Response
 * 
 * <p>支付渠道退款操作的响应结果</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelRefundResponse {
    
    /**
     * 退款状态
     */
    private TransactionStatus status;
    
    /**
     * 渠道交易记录ID（渠道侧唯一标识）
     */
    private String channelTransactionId;
    
    /**
     * 渠道交易号（渠道侧流水号）
     */
    private String channelTransactionNumber;
    
    /**
     * 支付渠道
     */
    private PaymentChannel paymentChannel;
    
    /**
     * 退款金额
     */
    private BigDecimal refundAmount;
    
    /**
     * 完成时间（同步退款成功时有值）
     */
    private LocalDateTime completedTime;
    
    /**
     * 错误码（失败时有值）
     */
    private String errorCode;
    
    /**
     * 错误信息（失败时有值）
     */
    private String errorMessage;
    
    /**
     * 创建成功响应
     */
    public static ChannelRefundResponse success(String channelTransactionId, 
                                                  String channelTransactionNumber,
                                                  PaymentChannel paymentChannel,
                                                  BigDecimal refundAmount) {
        return ChannelRefundResponse.builder()
                .status(TransactionStatus.SUCCESS)
                .channelTransactionId(channelTransactionId)
                .channelTransactionNumber(channelTransactionNumber)
                .paymentChannel(paymentChannel)
                .refundAmount(refundAmount)
                .completedTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建处理中响应（异步退款）
     */
    public static ChannelRefundResponse processing(String channelTransactionId,
                                                    PaymentChannel paymentChannel,
                                                    BigDecimal refundAmount) {
        return ChannelRefundResponse.builder()
                .status(TransactionStatus.PROCESSING)
                .channelTransactionId(channelTransactionId)
                .paymentChannel(paymentChannel)
                .refundAmount(refundAmount)
                .build();
    }
    
    /**
     * 创建失败响应
     */
    public static ChannelRefundResponse failed(String errorCode, String errorMessage) {
        return ChannelRefundResponse.builder()
                .status(TransactionStatus.FAILED)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }
    
    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return TransactionStatus.SUCCESS.equals(this.status);
    }
    
    /**
     * 判断是否处理中
     */
    public boolean isProcessing() {
        return TransactionStatus.PROCESSING.equals(this.status);
    }
    
    /**
     * 判断是否失败
     */
    public boolean isFailed() {
        return TransactionStatus.FAILED.equals(this.status);
    }
}