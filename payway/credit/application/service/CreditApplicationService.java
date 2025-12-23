package com.bytz.modules.cms.payway.credit.application.service;

import com.bytz.common.util.SecurityUtils;
import com.bytz.modules.cms.payment.domain.PaymentDomainService;
import com.bytz.modules.cms.payment.domain.command.CreatePaymentCommand;
import com.bytz.modules.cms.payment.domain.command.CreateTransactionCommand;
import com.bytz.modules.cms.payment.domain.enums.*;
import com.bytz.modules.cms.payment.domain.model.PaymentAggregate;
import com.bytz.modules.cms.payment.domain.model.PaymentTransactionEntity;
import com.bytz.modules.cms.payment.domain.repository.IPaymentRepository;
import com.bytz.modules.cms.payment.shared.exception.PaymentErrorCode;
import com.bytz.modules.cms.payment.shared.exception.PaymentException;
import com.bytz.modules.cms.payway.credit.application.assembler.CreditAssembler;
import com.bytz.modules.cms.payway.credit.application.model.*;
import com.bytz.modules.cms.payway.credit.domain.CreditWalletDomainService;
import com.bytz.modules.cms.payway.credit.domain.TemporaryCreditDomainService;
import com.bytz.modules.cms.payway.credit.domain.command.*;
import com.bytz.modules.cms.payway.credit.domain.entity.CreditBillEntity;
import com.bytz.modules.cms.payway.credit.domain.model.CreditWalletAggregate;
import com.bytz.modules.cms.payway.credit.domain.model.TemporaryCreditAggregate;
import com.bytz.modules.cms.payway.credit.domain.repository.ICreditWalletRepository;
import com.bytz.modules.cms.payway.credit.domain.repository.ITemporaryCreditRepository;
import com.bytz.modules.cms.payway.credit.shared.event.*;
import com.bytz.modules.cms.payway.credit.shared.exception.CreditWalletErrorCode;
import com.bytz.modules.cms.payway.credit.shared.exception.CreditWalletException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 信用应用服务
 * Credit Application Service
 *
 * <p>处理所有写操作（CQRS中的Command端）</p>
 * <p>职责：
 * <ul>
 *   <li>统一处理所有写操作</li>
 *   <li>协调领域服务</li>
 *   <li>RO/VO转换</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreditApplicationService {

    private final CreditWalletDomainService creditWalletDomainService;
    private final TemporaryCreditDomainService temporaryCreditDomainService;
    private final ICreditWalletRepository creditWalletRepository;
    private final ITemporaryCreditRepository temporaryCreditRepository;
    private final CreditAssembler creditAssembler;
    private final ApplicationEventPublisher eventPublisher;
    private final PaymentDomainService paymentDomainService;
    private final IPaymentRepository paymentRepository;

    /**
     * 创建信用钱包
     *
     * @param ro 创建钱包请求对象
     * @return 信用钱包VO
     */
    @Transactional(rollbackFor = Exception.class)
    public CreditWalletVO createWallet(CreateWalletRO ro) {
        log.info("Creating wallet for reseller: {}", ro.getResellerId());

        // RO -> Command
        CreateCreditWalletCommand command = creditAssembler.toCreateCommand(ro);
        // 配置security
        command.setCreateBy(SecurityUtils.getUserId());
        command.setCreateByName(SecurityUtils.getRealname());
        // 调用领域服务
        CreditWalletAggregate wallet = creditWalletDomainService.createWallet(command);

        // 发布钱包创建事件
        eventPublisher.publishEvent(WalletCreatedEvent.builder()
                .walletId(wallet.getId())
                .resellerId(wallet.getResellerId())
                .totalLimit(wallet.getTotalLimit())
                .termDays(wallet.getTermDays())
                .createdAt(LocalDateTime.now())
                .build());

        // Aggregate -> VO
        return creditAssembler.toVO(wallet);
    }

    /**
     * 信用支付
     *
     * @param ro 信用支付请求对象
     * @return 账单VO
     */
    @Transactional(rollbackFor = Exception.class)
    public CreditBillVO creditPay(PayCreditRO ro) {
        log.info("Processing credit payment for wallet: {}, reseller: {}", ro.getWalletId(), ro.getResellerId());

        // 查询钱包
        CreditWalletAggregate wallet = creditWalletRepository.findById(ro.getWalletId(), false)
                .orElseThrow(() -> new CreditWalletException(CreditWalletErrorCode.WALLET_NOT_FOUND));

        // 验证钱包归属
        if (!wallet.getResellerId().equals(ro.getResellerId())) {
            log.warn("Wallet resellerId mismatch: expected {}, actual {}", ro.getResellerId(), wallet.getResellerId());
            throw new CreditWalletException(CreditWalletErrorCode.WALLET_NOT_FOUND);
        }

        if (!wallet.canPay(ro.getAmount())) {
            throw new CreditWalletException(CreditWalletErrorCode.INSUFFICIENT_LIMIT);
        }

        // 校验信用钱包是否支持该支付
        PaymentAggregate paymentAggregate = paymentRepository.findById(ro.getPaymentId()).orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        if (!wallet.isSupportPayment(paymentAggregate.getPaymentType())) {
            throw new CreditWalletException(CreditWalletErrorCode.UNSUPPORTED_PAYMENT_TYPE);
        }

        // RO -> Command
        CreditPayCommand command = CreditPayCommand.builder()
                .amount(ro.getAmount())
                .paymentId(ro.getPaymentId())
                .build();

        // 调用聚合根方法
        CreditBillEntity bill = wallet.creditPay(command);

        // 持久化
        creditWalletRepository.update(wallet);

        // 查询支付单，验证并构建支付明细
        createPayTransactionAndCallBackTrue(bill);

        // 发布账单创建事件
        eventPublisher.publishEvent(BillCreatedEvent.builder()
                .billId(bill.getId())
                .code(bill.getCode())
                .walletId(wallet.getId())
                .resellerId(wallet.getResellerId())
                .transactionType(bill.getTransactionType())
                .amount(bill.getAmount())
                .dueDate(bill.getDueDate())
                .remark(bill.getRemark())
                .createdAt(LocalDateTime.now())
                .build());

        // Entity -> VO
        return creditAssembler.toVO(bill);
    }

    private void createPayTransactionAndCallBackTrue(CreditBillEntity bill) {

        PaymentTransactionEntity paymentTransactionEntity = paymentDomainService.executePayment(CreateTransactionCommand.builder()
                .paymentId(bill.getPaymentId())
                .paymentChannel(PaymentChannel.CREDIT_ACCOUNT)
                .transactionAmount(bill.getAmount())
                .channelTransactionId(bill.getId())
                .channelTransactionNumber(bill.getCode())
                .transactionType(TransactionType.PAYMENT)
                .completedTime(LocalDateTime.now())
                .transactionStatus(TransactionStatus.SUCCESS)
                .build());
    }

    /**
     * 批量信用支付
     *
     * @param ro 批量信用支付请求对象
     * @return 账单VO列表
     */
    @Transactional(rollbackFor = Exception.class)
    public List<CreditBillVO> batchCreditPay(BatchPayCreditRO ro) {
        log.info("Processing batch credit payment for wallet: {}, reseller: {}", ro.getWalletId(), ro.getResellerId());

        // 查询钱包
        CreditWalletAggregate wallet = creditWalletRepository.findById(ro.getWalletId(), false)
                .orElseThrow(() -> new CreditWalletException(CreditWalletErrorCode.WALLET_NOT_FOUND));

        // 验证钱包归属
        if (!wallet.getResellerId().equals(ro.getResellerId())) {
            log.warn("Wallet resellerId mismatch: expected {}, actual {}", ro.getResellerId(), wallet.getResellerId());
            throw new CreditWalletException(CreditWalletErrorCode.WALLET_NOT_FOUND);
        }
        // 计算总金额
        BigDecimal totalAmount = ro.getPayments().stream()
                .map(BasePaymentItemRo::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (!wallet.canPay(totalAmount)) {
            throw new CreditWalletException(CreditWalletErrorCode.INSUFFICIENT_LIMIT);
        }

        // 校验信用钱包是否支持该支付
        List<String> paymentIds = ro.getPayments().stream().map(BasePaymentItemRo::getPaymentId).collect(Collectors.toList());
        List<PaymentAggregate> paymentAggregateList = paymentRepository.findByIds(paymentIds);
        List<PaymentType> paymentTypes = paymentAggregateList.stream().map(PaymentAggregate::getPaymentType).collect(Collectors.toList());
        if (!wallet.isSupportPayment(paymentTypes)) {
            throw new CreditWalletException(CreditWalletErrorCode.UNSUPPORTED_PAYMENT_TYPE);
        }

        // RO -> Command
        List<CreditPayCommand> collect = ro.getPayments().stream().map(paymentItem -> CreditPayCommand.builder()
                .amount(paymentItem.getAmount())
                .paymentId(paymentItem.getPaymentId())
                .build()).collect(Collectors.toList());
        BatchCreditPayCommand command = BatchCreditPayCommand.builder()
                .payments(collect)
                .build();

        // 校验信用钱包能不能用于信用支付

        // 调用聚合根方法
        List<CreditBillEntity> bills = wallet.batchCreditPay(command);

        // 持久化
        creditWalletRepository.update(wallet);

        // 验证并构建支付明细
        bills.forEach(this::createPayTransactionAndCallBackTrue);

        // Entity -> VO
        return creditAssembler.toBillVOList(bills);
    }

    /**
     * 接收PowerApps临时授信
     *
     * @param ro 接收临时授信请求对象
     * @return 临时授信VO
     */
    @Transactional(rollbackFor = Exception.class)
    public TemporaryCreditVO receiveTemporaryCredit(ReceiveTemporaryCreditRO ro) {
        log.info("Receiving temporary credit for reseller: {}", ro.getResellerId());

        // RO -> Command
        CreateTemporaryCreditCommand command = creditAssembler.toCreateTemporaryCreditCommand(ro);

        // 调用领域服务
        TemporaryCreditAggregate temporaryCredit =
                temporaryCreditDomainService.createTemporaryCredit(command);

        // Aggregate -> VO
        return creditAssembler.toVO(temporaryCredit);
    }

    /**
     * 使用临时授信支付
     *
     * @param ro 使用临时授信支付请求对象
     * @return 账单VO
     */
    @Transactional(rollbackFor = Exception.class)
    public CreditBillVO payWithTemporaryCredit(PayTemporaryCreditRO ro) {
        log.info("Processing temporary credit payment for temporaryCreditId: {}, reseller: {}",
                ro.getTemporaryCreditId(), ro.getResellerId());

        // 查询临时授信
        TemporaryCreditAggregate temporaryCredit = temporaryCreditRepository.findById(ro.getTemporaryCreditId(), false)
                .orElseThrow(() -> new CreditWalletException(CreditWalletErrorCode.TEMPORARY_CREDIT_NOT_FOUND));

        // 验证临时授信归属
        if (!temporaryCredit.getResellerId().equals(ro.getResellerId())) {
            log.warn("TemporaryCredit resellerId mismatch: expected {}, actual {}", ro.getResellerId(), temporaryCredit.getResellerId());
            throw new CreditWalletException(CreditWalletErrorCode.TEMPORARY_CREDIT_NOT_FOUND);
        }
        if (!temporaryCredit.hasAvailableAmount(ro.getAmount())) {
            throw new CreditWalletException(CreditWalletErrorCode.INSUFFICIENT_LIMIT);
        }

        // 获取账期天数（通过临时授信关联的钱包）
        CreditWalletAggregate wallet = creditWalletRepository.findById(temporaryCredit.getCreditWalletId(), false)
                .orElseThrow(() -> new CreditWalletException(CreditWalletErrorCode.WALLET_NOT_FOUND));

        // RO -> Command（termDays从钱包获取）
        TemporaryCreditPayCommand command = TemporaryCreditPayCommand.builder()
                .amount(ro.getAmount())
                .paymentId(ro.getPaymentId())
                .termDays(wallet.getTermDays())
                .build();

        // 调用聚合根方法
        CreditBillEntity bill = temporaryCredit.temporaryCreditPay(command);

        // 持久化
        temporaryCreditRepository.update(temporaryCredit);

        // 验证并构建支付明细
        createPayTransactionAndCallBackTrue(bill);

        // Entity -> VO
        return creditAssembler.toVO(bill);
    }

    /**
     * 使用临时授信支付
     *
     * @param ro 使用临时授信支付请求对象
     * @return 账单VO
     */
    @Transactional(rollbackFor = Exception.class)
    public List<CreditBillVO> batchPayWithTemporaryCredit(BatchPayTemporaryCreditRO ro) {
        log.info("Processing batch temporary credit payment for temporaryCreditId: {}, reseller: {}",
                ro.getTemporaryCreditId(), ro.getResellerId());
        // 查询临时授信
        TemporaryCreditAggregate temporaryCredit = temporaryCreditRepository.findById(ro.getTemporaryCreditId(), false)
                .orElseThrow(() -> new CreditWalletException(CreditWalletErrorCode.TEMPORARY_CREDIT_NOT_FOUND));

        // 验证临时授信归属
        if (!temporaryCredit.getResellerId().equals(ro.getResellerId())) {
            log.warn("TemporaryCredit resellerId mismatch: expected {}, actual {}", ro.getResellerId(), temporaryCredit.getResellerId());
            throw new CreditWalletException(CreditWalletErrorCode.TEMPORARY_CREDIT_NOT_FOUND);
        }
        BigDecimal totalAmount = ro.getPayments().stream().map(BasePaymentItemRo::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (!temporaryCredit.hasAvailableAmount(totalAmount)) {
            throw new CreditWalletException(CreditWalletErrorCode.INSUFFICIENT_LIMIT);
        }
        // 获取账期天数（通过临时授信关联的钱包）
        CreditWalletAggregate wallet = creditWalletRepository.findById(temporaryCredit.getCreditWalletId(), false)
                .orElseThrow(() -> new CreditWalletException(CreditWalletErrorCode.WALLET_NOT_FOUND));

        // RO -> Command
        List<TemporaryCreditPayCommand> collect = ro.getPayments().stream().map(payment -> TemporaryCreditPayCommand.builder()
                .amount(payment.getAmount())
                .paymentId(payment.getPaymentId())
                .termDays(wallet.getTermDays())
                .build()).collect(Collectors.toList());

        BatchTemporaryCreditPayCommand command = BatchTemporaryCreditPayCommand.builder()
                .payments(collect)
                .build();

        // 调用聚合根方法
        List<CreditBillEntity> bills = temporaryCredit.batchTemporaryCreditPay(command);
        // 持久化
        temporaryCreditRepository.update(temporaryCredit);

        // 验证并构建支付明细
        bills.forEach(this::createPayTransactionAndCallBackTrue);
        // Entity -> VO
        return creditAssembler.toBillVOList(bills);
    }

    // ==================== 管理操作 ====================

    /**
     * 冻结钱包
     *
     * @param ro 冻结钱包请求对象
     */
    @Transactional(rollbackFor = Exception.class)
    public void freezeWallet(FreezeWalletRO ro) {
        log.info("Freezing wallet: {}", ro.getWalletId());

        // 查询钱包
        CreditWalletAggregate wallet = creditWalletRepository.findById(ro.getWalletId(), false)
                .orElseThrow(() -> new CreditWalletException(CreditWalletErrorCode.WALLET_NOT_FOUND));

        // 从 ro 中的 operator 字段已被移除，设置本地占位并添加
        String operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getRealname();

        // 构建命令（使用本地变量）
        FreezeWalletCommand command = FreezeWalletCommand.builder()
                .operator(operatorId)
                .operatorName(operatorName)
                .reason(ro.getReason())
                .build();

        // 调用聚合根方法
        wallet.freeze(command);

        // 持久化
        creditWalletRepository.update(wallet);

        // 发布钱包冻结事件（使用本地 operatorId）
        eventPublisher.publishEvent(WalletFrozenEvent.builder()
                .walletId(wallet.getId())
                .resellerId(wallet.getResellerId())
                .frozenAt(LocalDateTime.now())
                .reason(ro.getReason())
                .operator(operatorId)
                .build());
    }

    /**
     * 解冻钱包
     *
     * @param ro 解冻钱包请求对象
     */
    @Transactional(rollbackFor = Exception.class)
    public void unfreezeWallet(UnfreezeWalletRO ro) {
        log.info("Unfreezing wallet: {}", ro.getWalletId());

        // 查询钱包
        CreditWalletAggregate wallet = creditWalletRepository.findById(ro.getWalletId(), false)
                .orElseThrow(() -> new CreditWalletException(CreditWalletErrorCode.WALLET_NOT_FOUND));

        // 从 ro 中的 operator 字段已被移除，设置本地占位并添加
        String operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getRealname();

        // 构建命令
        UnfreezeWalletCommand command = UnfreezeWalletCommand.builder()
                .operator(operatorId)
                .operatorName(operatorName)
                .reason(ro.getReason())
                .build();

        // 调用聚合根方法
        wallet.unfreeze(command);

        // 持久化
        creditWalletRepository.update(wallet);

        // 发布钱包解冻事件
        eventPublisher.publishEvent(WalletUnfrozenEvent.builder()
                .walletId(wallet.getId())
                .resellerId(wallet.getResellerId())
                .unfrozenAt(LocalDateTime.now())
                .reason(ro.getReason())
                .operator(operatorId)
                .build());
    }

    /**
     * 启用钱包
     *
     * @param ro 启用钱包请求对象
     */
    @Transactional(rollbackFor = Exception.class)
    public void enableWallet(EnableWalletRO ro) {
        log.info("Enabling wallet: {}", ro.getWalletId());

        // 查询钱包
        CreditWalletAggregate wallet = creditWalletRepository.findById(ro.getWalletId(), false)
                .orElseThrow(() -> new CreditWalletException(CreditWalletErrorCode.WALLET_NOT_FOUND));

        // 从 ro 中的 operator 字段已被移除，设置本地占位并添加
        String operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getRealname();

        // 构建命令
        EnableWalletCommand command = EnableWalletCommand.builder()
                .operator(operatorId)
                .operatorName(operatorName)
                .reason(ro.getReason())
                .build();

        // 调用聚合根方法
        wallet.enable(command);

        // 持久化
        creditWalletRepository.update(wallet);

        // 发布钱包启用事件
        eventPublisher.publishEvent(WalletEnabledEvent.builder()
                .walletId(wallet.getId())
                .resellerId(wallet.getResellerId())
                .enabledAt(LocalDateTime.now())
                .reason(ro.getReason())
                .operator(operatorId)
                .build());
    }

    /**
     * 停用钱包
     *
     * @param ro 停用钱包请求对象
     */
    @Transactional(rollbackFor = Exception.class)
    public void disableWallet(DisableWalletRO ro) {
        log.info("Disabling wallet: {}", ro.getWalletId());

        // 查询钱包
        CreditWalletAggregate wallet = creditWalletRepository.findById(ro.getWalletId(), false)
                .orElseThrow(() -> new CreditWalletException(CreditWalletErrorCode.WALLET_NOT_FOUND));

        // 从 ro 中的 operator 字段已被移除，设置本地占位并添加
        String operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getRealname();

        // 构建命令
        DisableWalletCommand command = DisableWalletCommand.builder()
                .operator(operatorId)
                .operatorName(operatorName)
                .reason(ro.getReason())
                .build();

        // 调用聚合根方法
        wallet.disable(command);

        // 持久化
        creditWalletRepository.update(wallet);

        // 发布钱包停用事件
        eventPublisher.publishEvent(WalletDisabledEvent.builder()
                .walletId(wallet.getId())
                .resellerId(wallet.getResellerId())
                .disabledAt(LocalDateTime.now())
                .reason(ro.getReason())
                .operator(operatorId)
                .build());
    }

    /**
     * 调整信用额度
     *
     * @param ro 调整额度请求对象
     */
    @Transactional(rollbackFor = Exception.class)
    public void adjustCreditLimit(AdjustCreditLimitRO ro) {
        log.info("Adjusting credit limit for wallet: {}, newLimit: {}", ro.getWalletId(), ro.getNewTotalLimit());

        // 查询钱包
        CreditWalletAggregate wallet = creditWalletRepository.findById(ro.getWalletId(), false)
                .orElseThrow(() -> new CreditWalletException(CreditWalletErrorCode.WALLET_NOT_FOUND));

        // 保存旧额度用于事件
        BigDecimal oldTotalLimit = wallet.getTotalLimit();

        // 从 ro 中的 operator 字段已被移除，设置本地占位并添加
        String operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getRealname();

        // 构建命令
        AdjustCreditLimitCommand command = AdjustCreditLimitCommand.builder()
                .newTotalLimit(ro.getNewTotalLimit())
                .operator(operatorId)
                .operatorName(operatorName)
                .reason(ro.getReason())
                .build();

        // 调用聚合根方法
        wallet.adjustLimit(command);

        // 持久化
        creditWalletRepository.update(wallet);

        // 发布额度调整事件
        eventPublisher.publishEvent(LimitAdjustedEvent.builder()
                .walletId(wallet.getId())
                .resellerId(wallet.getResellerId())
                .oldTotalLimit(oldTotalLimit)
                .newTotalLimit(wallet.getTotalLimit())
                .availableLimit(wallet.getAvailableLimit())
                .adjustedAt(LocalDateTime.now())
                .reason(ro.getReason())
                .operator(operatorId)
                .build());
    }

    /**
     * 更新账期天数
     *
     * @param ro 更新账期请求对象
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateTermDays(UpdateTermDaysRO ro) {
        log.info("Updating term days for wallet: {}, newTermDays: {}", ro.getWalletId(), ro.getNewTermDays());

        // 查询钱包
        CreditWalletAggregate wallet = creditWalletRepository.findById(ro.getWalletId(), false)
                .orElseThrow(() -> new CreditWalletException(CreditWalletErrorCode.WALLET_NOT_FOUND));

        // 保存旧账期用于事件
        Integer oldTermDays = wallet.getTermDays();

        // 从 ro 中的 operator 字段已被移除，设置本地占位并添加
        String operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getRealname();

        // 构建命令
        UpdateTermDaysCommand command = UpdateTermDaysCommand.builder()
                .newTermDays(ro.getNewTermDays())
                .operator(operatorId)
                .operatorName(operatorName)
                .reason(ro.getReason())
                .build();

        // 调用聚合根方法
        wallet.updateTermDays(command);

        // 持久化
        creditWalletRepository.update(wallet);

        // 发布账期更新事件
        eventPublisher.publishEvent(TermDaysUpdatedEvent.builder()
                .walletId(wallet.getId())
                .resellerId(wallet.getResellerId())
                .oldTermDays(oldTermDays)
                .newTermDays(wallet.getTermDays())
                .updatedAt(LocalDateTime.now())
                .reason(ro.getReason())
                .operator(operatorId)
                .build());
    }

    /**
     * 开启预付功能
     *
     * @param ro 开启预付功能请求对象
     */
    @Transactional(rollbackFor = Exception.class)
    public void enablePrepayment(EnablePrepaymentRO ro) {
        log.info("Enabling prepayment for wallet: {}", ro.getWalletId());

        // 查询钱包
        CreditWalletAggregate wallet = creditWalletRepository.findById(ro.getWalletId(), false)
                .orElseThrow(() -> new CreditWalletException(CreditWalletErrorCode.WALLET_NOT_FOUND));

        // 从 ro 中的 operator 字段已被移除，设置本地占位并添加
        String operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getRealname();

        // 构建命令
        EnablePrepaymentCommand command = EnablePrepaymentCommand.builder()
                .operator(operatorId)
                .operatorName(operatorName)
                .reason(ro.getReason())
                .build();

        // 调用聚合根方法
        wallet.enablePrepayment(command);

        // 持久化
        creditWalletRepository.update(wallet);

        log.info("Prepayment enabled for wallet: {}", ro.getWalletId());
    }

    /**
     * 关闭预付功能
     *
     * @param ro 关闭预付功能请求对象
     */
    @Transactional(rollbackFor = Exception.class)
    public void disablePrepayment(DisablePrepaymentRO ro) {
        log.info("Disabling prepayment for wallet: {}", ro.getWalletId());

        // 查询钱包
        CreditWalletAggregate wallet = creditWalletRepository.findById(ro.getWalletId(), false)
                .orElseThrow(() -> new CreditWalletException(CreditWalletErrorCode.WALLET_NOT_FOUND));

        // 从 ro 中的 operator 字段已被移除，设置本地占位并添加
        String operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getRealname();

        // 构建命令
        DisablePrepaymentCommand command = DisablePrepaymentCommand.builder()
                .operator(operatorId)
                .operatorName(operatorName)
                .reason(ro.getReason())
                .build();

        // 调用聚合根方法
        wallet.disablePrepayment(command);

        // 持久化
        creditWalletRepository.update(wallet);

        log.info("Prepayment disabled for wallet: {}", ro.getWalletId());
    }

    // ==================== 还款操作 ====================

    /**
     * 发起还款
     *
     * @param ro 发起还款请求对象
     */
    @Transactional(rollbackFor = Exception.class)
    public void initiateRepayment(InitiateRepaymentRO ro) {
        log.info("Initiating repayment for wallet: {}, billId: {}", ro.getWalletId(), ro.getBillId());

        // 查询钱包（需要加载账单）
        CreditWalletAggregate wallet = creditWalletRepository.findById(ro.getWalletId(), true)
                .orElseThrow(() -> new CreditWalletException(CreditWalletErrorCode.WALLET_NOT_FOUND));

        // 获取账单信息用于事件
        CreditBillEntity bill = wallet.getUnpaidBills().stream()
                .filter(b -> b.getId().equals(ro.getBillId()))
                .findFirst()
                .orElseThrow(() -> new CreditWalletException(CreditWalletErrorCode.REPAYMENT_AMOUNT_INVALID));

        PaymentAggregate paymentAggregate = paymentRepository.findById(bill.getPaymentId()).orElseThrow(() -> new CreditWalletException(CreditWalletErrorCode.TRANSACTION_NOT_FOUND));

        // 构建还款支付单
        PaymentAggregate payment = paymentDomainService.createPayment(CreatePaymentCommand.builder()
                .orderId(paymentAggregate.getOrderId())
                .resellerId(paymentAggregate.getResellerId())
                .paymentAmount(bill.getAmount())
                .paymentType(PaymentType.CREDIT_REPAYMENT)
                .businessDesc("信用还款")
                .businessExpireDate(bill.getDueDate())
                .relatedBusinessType(RelatedBusinessType.CREDIT_RECORD)
                .relatedBusinessId(bill.getId())
                .build());
        String repaymentPaymentId = payment.getId();

        // 构建命令
        InitiateRepaymentCommand command = InitiateRepaymentCommand.builder()
                .billId(ro.getBillId())
                .repaymentPaymentId(repaymentPaymentId)
                .build();

        // 调用聚合根方法
        wallet.initiateRepayment(command);

        // 持久化
        creditWalletRepository.update(wallet);

        // 发布还款发起事件
        eventPublisher.publishEvent(RepaymentInitiatedEvent.builder()
                .billId(bill.getId())
                .code(bill.getCode())
                .walletId(wallet.getId())
                .resellerId(wallet.getResellerId())
                .repaymentAmount(bill.getAmount())
                .repaymentPaymentId(repaymentPaymentId)
                .initiatedAt(LocalDateTime.now())
                .build());

    }
}