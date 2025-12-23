package com.bytz.modules.cms.payway.wallet.application.service;

import com.bytz.modules.cms.payment.domain.PaymentDomainService;
import com.bytz.modules.cms.payment.domain.command.CreateTransactionCommand;
import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.domain.enums.TransactionStatus;
import com.bytz.modules.cms.payment.domain.enums.TransactionType;
import com.bytz.modules.cms.payment.domain.model.PaymentTransactionEntity;
import com.bytz.modules.cms.payway.wallet.application.model.BatchPayWalletRO;
import com.bytz.modules.cms.payway.wallet.application.model.PayWalletRO;
import com.bytz.modules.cms.payway.wallet.domain.WalletDomainService;
import com.bytz.modules.cms.payway.wallet.domain.command.PayWithWalletCommand;
import com.bytz.modules.cms.payway.wallet.domain.model.WalletAggregate;
import com.bytz.modules.cms.payway.wallet.domain.repository.IWalletRepository;
import com.bytz.modules.cms.payway.wallet.domain.valueobject.WalletTransactionValueObject;
import com.bytz.modules.cms.payway.wallet.shared.exception.WalletBusinessException;
import com.bytz.modules.cms.payway.wallet.shared.exception.WalletErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 钱包应用服务
 * Wallet Application Service
 * <p>
 * 协调钱包的创建、充值、支付、退款等用例
 * 职责：不包含业务逻辑，仅负责用例协调和事件发布
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletApplicationService {

    private final IWalletRepository walletRepository;
    private final WalletDomainService walletDomainService;
    private final PaymentDomainService paymentDomainService;


    /**
     * 启用钱包（外部REST接口）
     * <p>
     * 使用场景：管理员启用经销商钱包
     */
    @Transactional
    public Boolean enableWallet(String walletId) {
        log.info("启用钱包：{}", walletId);
        // 查询钱包
        Optional<WalletAggregate> optional = walletRepository.findById(walletId);
        WalletAggregate aggregate = optional.orElseThrow(() -> new WalletBusinessException(WalletErrorCode.WALLET_NOT_FOUND));
        // 调用钱包聚合enable方法
        aggregate.enable();
        walletRepository.update(aggregate);
        return true;
    }

    /**
     * 停用钱包（外部REST接口）
     * <p>
     * 使用场景：管理员停用经销商钱包
     */
    @Transactional
    public Boolean disableWallet(String walletId) {
        log.info("停用钱包：{}", walletId);
        // 查询钱包
        Optional<WalletAggregate> optional = walletRepository.findById(walletId);
        WalletAggregate aggregate = optional.orElseThrow(() -> new WalletBusinessException(WalletErrorCode.WALLET_NOT_FOUND));
        // 调用钱包聚合enable方法
        aggregate.disable();
        walletRepository.update(aggregate);
        return true;
    }

    /**
     * 停用钱包（外部REST接口）
     * <p>
     * 使用场景：管理员停用经销商钱包
     */
    @Transactional
    public Boolean disableWalletByResellerId(String resellerId) {
        log.info("停用钱包：经销商id {}", resellerId);
        // 查询钱包
        Optional<WalletAggregate> optional = walletRepository.findByResellerId(resellerId);
        WalletAggregate aggregate = optional.orElseThrow(() -> new WalletBusinessException(WalletErrorCode.WALLET_NOT_FOUND));
        // 调用钱包聚合enable方法
        aggregate.disable();
        walletRepository.update(aggregate);
        return true;
    }

    /**
     * 使用钱包支付（外部REST接口）
     * <p>
     * 使用场景：使用钱包余额进行支付
     */
    @Transactional
    public Boolean creditPay(PayWalletRO ro) {
        log.info("使用钱包支付：钱包ID={}, 支付单ID={}, 金额={}", ro.getWalletId(), ro.getPaymentId(), ro.getAmount());

        validParam(ro.getWalletId(), ro.getResellerId());

        createPayTran(ro.getWalletId(), ro.getAmount(), ro.getPaymentId());

        return true;
    }

    private WalletTransactionValueObject createPayTran(String walletId, BigDecimal amount, String paymentId) {
        PayWithWalletCommand command = PayWithWalletCommand.builder()
                .walletId(walletId)
                .amount(amount)
                .build();

        WalletTransactionValueObject tran = walletDomainService.processPayment(command);

        CreateTransactionCommand build = CreateTransactionCommand.builder()
                .paymentId(paymentId)
                .paymentChannel(PaymentChannel.WALLET_PAYMENT)
                .transactionAmount(amount)
                .channelTransactionId(tran.getId())
                .channelTransactionNumber(tran.getCode())
                .transactionType(TransactionType.PAYMENT)
                .transactionStatus(TransactionStatus.SUCCESS)
                .completedTime(LocalDateTime.now())
                .build();
        PaymentTransactionEntity paymentTransactionEntity = paymentDomainService.executePayment(build);
        return tran;
    }

    private void validParam(String walletId, String resellerId) {
        // 查询钱包
        Optional<WalletAggregate> optional = walletRepository.findById(walletId);
        // 调用钱包聚合支付方法
        WalletAggregate aggregate = optional.orElseThrow(() -> new WalletBusinessException(WalletErrorCode.WALLET_NOT_FOUND));

        // 验证钱包归属
        if (!aggregate.getResellerId().equals(resellerId)) {
            log.warn("Wallet resellerId mismatch: expected {}, actual {}", resellerId, aggregate.getResellerId());
            throw new WalletBusinessException(WalletErrorCode.PARAM_INVALID);
        }
    }

    /**
     * 批量使用钱包支付（外部REST接口）
     * <p>
     * 使用场景：批量使用钱包余额进行支付
     */
    @Transactional
    public Boolean batchCreditPay(BatchPayWalletRO ro) {
        log.info("批量使用钱包支付：钱包ID={}, 支付项数量={}", ro.getWalletId(), ro.getPayments().size());

        validParam(ro.getWalletId(), ro.getResellerId());

        ro.getPayments().forEach(payment -> {
            createPayTran(ro.getWalletId(), payment.getAmount(), payment.getPaymentId());
        });
        return true;
    }
}