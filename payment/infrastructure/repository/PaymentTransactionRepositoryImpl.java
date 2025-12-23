package com.bytz.modules.cms.payment.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bytz.modules.cms.payment.domain.enums.TransactionStatus;
import com.bytz.modules.cms.payment.domain.model.PaymentTransactionEntity;
import com.bytz.modules.cms.payment.domain.repository.IPaymentTransactionRepository;
import com.bytz.modules.cms.payment.infrastructure.assembler.PaymentInfrastructureAssembler;
import com.bytz.modules.cms.payment.infrastructure.entity.PaymentTransactionPO;
import com.bytz.modules.cms.payment.infrastructure.mapper.PaymentTransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 支付流水仓储实现
 * Payment Transaction Repository Implementation
 *
 * <p>实现IPaymentTransactionRepository接口，处理支付流水的持久化</p>
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PaymentTransactionRepositoryImpl implements IPaymentTransactionRepository {

    private final PaymentTransactionMapper transactionMapper;
    private final PaymentInfrastructureAssembler paymentInfrastructureAssembler;


    @Override
    public Optional<PaymentTransactionEntity> findById(String id) {
        log.debug("根据ID查找支付流水，ID: {}", id);

        PaymentTransactionPO po = transactionMapper.selectById(id);
        if (po == null) {
            return Optional.empty();
        }

        return Optional.of(paymentInfrastructureAssembler.poToTransaction(po));
    }

    @Override
    public List<PaymentTransactionEntity> findByIds(List<String> ids) {
        log.debug("根据ID列表批量查找支付流水，数量: {}", ids.size());

        if (ids.isEmpty()) {
            return new ArrayList<>();
        }

        List<PaymentTransactionPO> pos = transactionMapper.selectBatchIds(ids);
        return paymentInfrastructureAssembler.posToTransactions(pos);
    }

    @Override
    public List<PaymentTransactionEntity> findByPaymentId(String paymentId) {
        log.debug("根据支付单ID查找流水列表，paymentId: {}", paymentId);

        LambdaQueryWrapper<PaymentTransactionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransactionPO::getPaymentId, paymentId)
                .orderByDesc(PaymentTransactionPO::getCreateTime);

        List<PaymentTransactionPO> pos = transactionMapper.selectList(wrapper);
        return paymentInfrastructureAssembler.posToTransactions(pos);
    }

    @Override
    public List<PaymentTransactionEntity> findByPaymentIds(List<String> paymentIds) {
        log.debug("根据支付单ID列表批量查找流水，数量: {}", paymentIds.size());

        if (paymentIds.isEmpty()) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<PaymentTransactionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(PaymentTransactionPO::getPaymentId, paymentIds)
                .orderByDesc(PaymentTransactionPO::getCreateTime);

        List<PaymentTransactionPO> pos = transactionMapper.selectList(wrapper);
        return paymentInfrastructureAssembler.posToTransactions(pos);
    }

    @Override
    public Optional<PaymentTransactionEntity> findByPaymentIdAndChannelTransactionId(String paymentId, String channelTransactionId) {
        log.debug("根据支付单ID和渠道交易记录ID联合查询，paymentId: {}, channelTransactionId: {}",
                paymentId, channelTransactionId);

        LambdaQueryWrapper<PaymentTransactionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransactionPO::getPaymentId, paymentId)
                .eq(PaymentTransactionPO::getChannelTransactionId, channelTransactionId);

        PaymentTransactionPO po = transactionMapper.selectOne(wrapper);
        if (po == null) {
            return Optional.empty();
        }

        return Optional.of(paymentInfrastructureAssembler.poToTransaction(po));
    }

    @Override
    public boolean existsProcessingByPaymentId(String paymentId) {
        log.debug("检查支付单是否存在进行中的流水，paymentId: {}", paymentId);

        LambdaQueryWrapper<PaymentTransactionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransactionPO::getPaymentId, paymentId)
                .eq(PaymentTransactionPO::getTransactionStatus, TransactionStatus.PROCESSING);

        return transactionMapper.selectCount(wrapper) > 0;
    }

    @Override
    public List<PaymentTransactionEntity> findSuccessfulByPaymentId(String paymentId) {
        log.debug("查询支付单的成功流水，paymentId: {}", paymentId);

        LambdaQueryWrapper<PaymentTransactionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransactionPO::getPaymentId, paymentId)
                .eq(PaymentTransactionPO::getTransactionStatus, TransactionStatus.SUCCESS)
                .orderByDesc(PaymentTransactionPO::getCreateTime);

        List<PaymentTransactionPO> pos = transactionMapper.selectList(wrapper);
        return paymentInfrastructureAssembler.posToTransactions(pos);
    }

    @Override
    public List<PaymentTransactionEntity> findByOriginalTransactionId(String originalTransactionId) {
        log.debug("根据原支付流水ID查询退款流水，originalTransactionId: {}", originalTransactionId);

        LambdaQueryWrapper<PaymentTransactionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransactionPO::getOriginalTransactionId, originalTransactionId)
                .orderByDesc(PaymentTransactionPO::getCreateTime);

        List<PaymentTransactionPO> pos = transactionMapper.selectList(wrapper);
        return paymentInfrastructureAssembler.posToTransactions(pos);
    }
}