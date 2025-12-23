package com.bytz.modules.cms.payway.wallet.application.service;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.domain.service.ChannelRefundRequest;
import com.bytz.modules.cms.payment.domain.service.ChannelRefundResponse;
import com.bytz.modules.cms.payment.domain.service.IPaymentChannelRefundService;
import com.bytz.modules.cms.payway.wallet.domain.WalletDomainService;
import com.bytz.modules.cms.payway.wallet.domain.command.RefundToWalletCommand;
import com.bytz.modules.cms.payway.wallet.domain.repository.IWalletRepository;
import com.bytz.modules.cms.payway.wallet.domain.valueobject.WalletTransactionValueObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 钱包渠道退款服务实现
 * Wallet Channel Refund Service Implementation
 * 
 * <p>钱包退款为同步操作，返回 SUCCESS 状态</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletChannelRefundService implements IPaymentChannelRefundService {
    
    private final WalletDomainService walletDomainService;
    private final IWalletRepository walletRepository;
    
    @Override
    public PaymentChannel getSupportedChannel() {
        return PaymentChannel.WALLET_PAYMENT;
    }
    
    @Override
    public ChannelRefundResponse executeRefund(ChannelRefundRequest request) {
        log.info("执行钱包退款，退款支付单ID: {}, 退款金额: {}", 
                request.getRefundPaymentId(), request.getRefundAmount());
        
        try {
            // 根据经销商ID获取钱包
            String walletId = walletRepository.findByResellerId(request.getResellerId())
                    .map(wallet -> wallet.getId())
                    .orElseThrow(() -> new RuntimeException("钱包不存在"));
            
            // 构建钱包退款命令
            RefundToWalletCommand walletCommand = RefundToWalletCommand.builder()
                    .walletId(walletId)
                    .amount(request.getRefundAmount())
                    .originalTransactionId(request.getOriginalChannelTransactionId())
                    .reason(request.getRefundReason())
                    .build();
            
            // 执行钱包退款（同步操作，立即完成）
            WalletTransactionValueObject transaction = walletDomainService.processRefund(walletCommand);

            
            log.info("钱包退款成功，渠道交易记录ID: {}", transaction.getId());
            
            // 返回 SUCCESS 状态表示同步退款已完成
            return ChannelRefundResponse.success(
                    transaction.getId(),
                    transaction.getCode(),
                    PaymentChannel.WALLET_PAYMENT,
                    request.getRefundAmount()
            );
            
        } catch (Exception e) {
            log.error("钱包退款失败，退款支付单ID: {}, 错误: {}", request.getRefundPaymentId(), e.getMessage(), e);
            return ChannelRefundResponse.failed("WALLET_REFUND_ERROR", e.getMessage());
        }
    }
}