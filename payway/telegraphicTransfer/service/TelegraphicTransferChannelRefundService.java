package com.bytz.modules.cms.payway.telegraphicTransfer.service;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.domain.service.ChannelRefundRequest;
import com.bytz.modules.cms.payment.domain.service.ChannelRefundResponse;
import com.bytz.modules.cms.payment.domain.service.IPaymentChannelRefundService;
import com.bytz.modules.cms.payway.telegraphicTransfer.model.TelegraphicTransferPayDetailRO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * cms-backend
 * 电汇渠道退款服务实现
 * <p>电汇退款为同步操作，返回 SUCCESS 状态</p>
 *
 * @author bytz
 * @version 1.0
 * @date 2025/12/2 13:25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelegraphicTransferChannelRefundService implements IPaymentChannelRefundService {

    private final ITelegraphicTransferService telegraphicTransferService;

    @Override
    public PaymentChannel getSupportedChannel() {
        return PaymentChannel.WIRE_TRANSFER;
    }

    @Override
    public ChannelRefundResponse executeRefund(ChannelRefundRequest request) {
        log.info("执行电汇退款操作，退款支付单ID: {}, 退款金额: {}", request.getRefundPaymentId(), request.getRefundAmount());
        //电汇Id
        String telegraphicTransferId = request.getOriginalChannelTransactionId();
        //退回金额
        BigDecimal refundAmount = request.getRefundAmount();
        //退款单Id
        String paymentId = request.getRefundPaymentId();
        //经销商Id
        String resellerId = request.getResellerId();
        try {
            log.info("电汇退款所需参数，电汇Id{},退回的金额:{},退款单Id:{},经销商Id:{}", telegraphicTransferId, refundAmount, paymentId, resellerId);
            // 调用电汇退款，返回电汇退款详情信息
            TelegraphicTransferPayDetailRO transferPayDetailRo
                    = telegraphicTransferService.rebackTelegraphicTransfer(telegraphicTransferId, refundAmount, paymentId, resellerId);

            log.info("电汇退款成功，渠道交易记录ID: {}", transferPayDetailRo.getId());

            // 返回 SUCCESS 状态表示同步退款已完成
            return ChannelRefundResponse.success(
                    transferPayDetailRo.getId(),
                    transferPayDetailRo.getTransactionCode(),
                    PaymentChannel.WIRE_TRANSFER,
                    request.getRefundAmount());

        } catch (Exception e) {
            log.error("电汇退款失败，退款支付单ID: {}, 错误: {}", request.getRefundPaymentId(), e.getMessage(), e);
            return ChannelRefundResponse.failed("TELEGRAPHIC_TRANSFER_REFUND_ERROR", e.getMessage());
        }
    }
}