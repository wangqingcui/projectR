package com.bytz.modules.cms.payway.wallet.domain;

import com.bytz.modules.cms.payway.wallet.domain.command.*;
import com.bytz.modules.cms.payway.wallet.domain.model.WalletAggregate;
import com.bytz.modules.cms.payway.wallet.domain.repository.IWalletRepository;
import com.bytz.modules.cms.payway.wallet.domain.valueobject.WalletTransactionValueObject;
import com.bytz.modules.cms.payway.wallet.shared.exception.WalletBusinessException;
import com.bytz.modules.cms.payway.wallet.shared.exception.WalletErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.math.BigDecimal;

/**
 * 钱包领域服务
 * Wallet Domain Service
 * <p>
 * 统一管理钱包的所有业务逻辑
 * 需求来源：票据钱包需求 - 票据充值接口、票据钱包消费明细、票据钱包金额管理
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class WalletDomainService {

    private final IWalletRepository walletRepository;

    /**
     * 创建钱包
     *
     * 业务规则：每个经销商只能有一个钱包，钱包创建时默认状态为DISABLED
     */
    @Transactional
    public WalletAggregate createWallet(@Valid CreateWalletCommand command) {
        // 实现创建钱包的业务逻辑
        log.info("创建钱包：经销商ID={}", command.getResellerId());

        // 验证经销商唯一性（使用exists方法，不需要查询完整对象）
        if (walletRepository.existsByResellerId(command.getResellerId())) {
            throw new WalletBusinessException(WalletErrorCode.DUPLICATE_WALLET_FOR_RESELLER);
        }

        // 创建钱包聚合根（ID将由MyBatis-Plus自动生成，币种固定为CNY）
        WalletAggregate wallet = WalletAggregate.create(command.getResellerId());

        // 保存到仓储（MyBatis-Plus会自动生成ID和审计字段）
        walletRepository.save(wallet);

        return wallet;
    }

    /**
     * 校验充值操作的合法性
     */
    public void validateRecharge(@Valid ValidateRechargeCommand command) {
        // 实现校验充值操作合法性的业务逻辑
        log.info("校验充值操作：钱包ID={}，金额={}", command.getWalletId(), command.getAmount());

        WalletAggregate wallet = walletRepository.findById(command.getWalletId())
                .orElseThrow(() -> new WalletBusinessException(WalletErrorCode.WALLET_NOT_FOUND));

        wallet.validateRecharge(command.getAmount());
    }


    /**
     * 校验支付操作的合法性
     */
    public void validatePayment(@Valid ValidatePaymentCommand command) {
        // 实现校验支付操作合法性的业务逻辑
        log.info("校验支付操作：钱包ID={}，金额={}", command.getWalletId(), command.getAmount());

        WalletAggregate wallet = walletRepository.findById(command.getWalletId())
                .orElseThrow(() -> new WalletBusinessException(WalletErrorCode.WALLET_NOT_FOUND));

        wallet.validatePayment(command.getAmount());
    }

    /**
     * 校验退款操作的合法性
     */
    public void validateRefund(@Valid ValidateRefundCommand command) {
        // 实现校验退款操作合法性的业务逻辑
        log.info("校验退款操作：钱包ID={}，金额={}", command.getWalletId(), command.getAmount());

        WalletAggregate wallet = walletRepository.findById(command.getWalletId())
                .orElseThrow(() -> new WalletBusinessException(WalletErrorCode.WALLET_NOT_FOUND));

        wallet.validateRefund(command.getAmount(), command.getOriginalAmount());
    }

    /**
     * 处理充值请求
     */
    @Transactional
    public WalletTransactionValueObject processRecharge(@Valid RechargeWalletCommand command) {
        // 实现处理充值请求的业务逻辑
        log.info("处理充值请求：钱包ID={}，金额={}", command.getWalletId(), command.getAmount());

        // 获取钱包（只查询一次）
        WalletAggregate wallet = walletRepository.findById(command.getWalletId())
                .orElseThrow(() -> new WalletBusinessException(WalletErrorCode.WALLET_NOT_FOUND));

        // 执行充值操作（聚合根会创建交易记录）
        wallet.recharge(command.getAmount(), command.getRemark());

        // 更新钱包（包含钱包信息和交易记录）
        WalletAggregate update = walletRepository.update(wallet);

        return update.getCurrentTransaction();
    }

    /**
     * 处理支付请求
     */
    @Transactional
    public WalletTransactionValueObject processPayment(@Valid PayWithWalletCommand command) {
        // 实现处理支付请求的业务逻辑
        log.info("处理支付请求：钱包ID={}，金额={}",
                command.getWalletId(), command.getAmount());

        // 获取钱包（只查询一次）
        WalletAggregate wallet = walletRepository.findById(command.getWalletId())
                .orElseThrow(() -> new WalletBusinessException(WalletErrorCode.WALLET_NOT_FOUND));

        // 执行支付操作（聚合根会创建交易记录）
        wallet.payment(command.getAmount(), command.getRemark());

        // 更新钱包（包含钱包信息和交易记录）
        WalletAggregate update = walletRepository.update(wallet);

        return update.getCurrentTransaction();
    }

    /**
     * 处理退款请求
     */
    @Transactional
    public WalletTransactionValueObject processRefund(@Valid RefundToWalletCommand command) {
        // 实现处理退款请求的业务逻辑
        log.info("处理退款请求：钱包ID={}，金额={}",
                command.getWalletId(), command.getAmount());

        // 获取原交易记录
        WalletTransactionValueObject originalTransaction =
                walletRepository.findTransactionById(command.getOriginalTransactionId())
                        .orElseThrow(() -> new WalletBusinessException(WalletErrorCode.TRANSACTION_NOT_FOUND));

        // 获取钱包（只查询一次）
        WalletAggregate wallet = walletRepository.findById(command.getWalletId())
                .orElseThrow(() -> new WalletBusinessException(WalletErrorCode.WALLET_NOT_FOUND));

        // 验证退款（使用重载方法，传入聚合根以避免重复查询，使用原交易金额的绝对值）
        BigDecimal originalAmount = originalTransaction.getAmount().abs();
        wallet.validateRefund(command.getAmount(), originalAmount);

        // 执行退款操作（聚合根会创建交易记录）
        WalletTransactionValueObject refund = wallet.refund(command.getAmount(), command.getReason());

        // 更新钱包（包含钱包信息和交易记录）
        WalletAggregate update = walletRepository.update(wallet);

        return update.getCurrentTransaction();
    }

}