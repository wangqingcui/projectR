package com.bytz.modules.cms.payment.infrastructure.channel;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.domain.service.ChannelRefundRequest;
import com.bytz.modules.cms.payment.domain.service.ChannelRefundResponse;
import com.bytz.modules.cms.payment.domain.service.IPaymentChannelRefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 线上支付渠道退款服务实现（银联、企业网银等）
 * Online Payment Channel Refund Service Implementation
 * 
 * <p>线上支付退款为异步操作，返回 PROCESSING 状态，需等待渠道回调</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OnlinePaymentChannelRefundService implements IPaymentChannelRefundService {
    
    @Override
    public PaymentChannel getSupportedChannel() {
        return PaymentChannel.ONLINE_PAYMENT;
    }
    
    @Override
    public ChannelRefundResponse executeRefund(ChannelRefundRequest request) {
        log.info("执行线上支付退款，退款支付单ID: {}, 原渠道交易ID: {}, 退款金额: {}", 
                request.getRefundPaymentId(), request.getOriginalChannelTransactionId(), request.getRefundAmount());
        
        try {
            // TODO: 调用实际的线上支付渠道API（银联、企业网银等）
            // 这里模拟异步退款请求
            
            // 生成渠道交易记录ID（用于关联回调）
            String channelTransactionId = "ONL" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
            
            log.info("线上支付退款请求已提交，等待渠道回调，渠道交易记录ID: {}", channelTransactionId);
            
            // 返回 PROCESSING 状态表示异步退款，等待回调
            return ChannelRefundResponse.processing(
                    channelTransactionId,
                    PaymentChannel.ONLINE_PAYMENT,
                    request.getRefundAmount()
            );
            
        } catch (Exception e) {
            log.error("线上支付退款请求失败，退款支付单ID: {}, 错误: {}", request.getRefundPaymentId(), e.getMessage(), e);
            return ChannelRefundResponse.failed("ONLINE_REFUND_ERROR", e.getMessage());
        }
    }
}
