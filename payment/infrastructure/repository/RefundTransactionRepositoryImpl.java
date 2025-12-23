package com.bytz.modules.cms.payment.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bytz.modules.cms.payment.domain.enums.TransactionStatus;
import com.bytz.modules.cms.payment.domain.enums.TransactionType;
import com.bytz.modules.cms.payment.domain.model.RefundTransactionEntity;
import com.bytz.modules.cms.payment.domain.repository.IRefundTransactionRepository;
import com.bytz.modules.cms.payment.infrastructure.assembler.PaymentInfrastructureAssembler;
import com.bytz.modules.cms.payment.infrastructure.entity.PaymentTransactionPO;
import com.bytz.modules.cms.payment.infrastructure.mapper.PaymentTransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 退款流水仓储实现
 * Refund Transaction Repository Implementation
 *
 * <p>实现退款流水实体的持久化操作</p>
 * <p>注意：退款流水与支付流水共用cms_payment_transaction表和PaymentTransactionPO，通过transactionType=REFUND区分</p>
 * <p>复用PaymentInfrastructureAssembler进行领域对象与持久化对象的转换</p>
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RefundTransactionRepositoryImpl implements IRefundTransactionRepository {

    private final PaymentTransactionMapper transactionMapper;
    private final PaymentInfrastructureAssembler assembler;

    @Override
    public RefundTransactionEntity save(RefundTransactionEntity transaction) {
        log.debug("Saving refund transaction: {}", transaction.getId());

        // 转换为持久化对象
        PaymentTransactionPO po = assembler.toPaymentTransactionPO(transaction);

        // 保存到数据库
        transactionMapper.insert(po);

        // 设置生成的ID
        transaction.setId(po.getId());
        transaction.setCode(po.getCode());

        return transaction;
    }

    @Override
    public Optional<RefundTransactionEntity> findById(String id) {
        log.debug("Finding refund transaction by id: {}", id);

        LambdaQueryWrapper<PaymentTransactionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransactionPO::getId, id)
                .eq(PaymentTransactionPO::getTransactionType, TransactionType.REFUND);

        PaymentTransactionPO po = transactionMapper.selectOne(wrapper);
        if (po == null) {
            return Optional.empty();
        }

        return Optional.of(assembler.toRefundTransactionEntity(po));
    }

    @Override
    public List<RefundTransactionEntity> findByRefundId(String refundId) {
        log.debug("Finding refund transactions by refund id: {}", refundId);

        LambdaQueryWrapper<PaymentTransactionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransactionPO::getPaymentId, refundId)
                .eq(PaymentTransactionPO::getTransactionType, TransactionType.REFUND)
                .orderByDesc(PaymentTransactionPO::getCreateTime);

        List<PaymentTransactionPO> pos = transactionMapper.selectList(wrapper);
        return pos.stream()
                .map(assembler::toRefundTransactionEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<RefundTransactionEntity> findByChannelTransactionId(String channelTransactionId) {
        log.debug("Finding refund transaction by channel transaction id: {}", channelTransactionId);

        LambdaQueryWrapper<PaymentTransactionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransactionPO::getChannelTransactionId, channelTransactionId)
                .eq(PaymentTransactionPO::getTransactionType, TransactionType.REFUND);

        PaymentTransactionPO po = transactionMapper.selectOne(wrapper);
        if (po == null) {
            return Optional.empty();
        }

        return Optional.of(assembler.toRefundTransactionEntity(po));
    }

    @Override
    public Optional<RefundTransactionEntity> findByOriginalTransactionId(String originalTransactionId) {
        log.debug("Finding refund transaction by original transaction id: {}", originalTransactionId);

        LambdaQueryWrapper<PaymentTransactionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransactionPO::getOriginalTransactionId, originalTransactionId)
                .eq(PaymentTransactionPO::getTransactionType, TransactionType.REFUND);

        PaymentTransactionPO po = transactionMapper.selectOne(wrapper);
        if (po == null) {
            return Optional.empty();
        }

        return Optional.of(assembler.toRefundTransactionEntity(po));
    }

    @Override
    public RefundTransactionEntity update(RefundTransactionEntity transaction) {
        log.debug("Updating refund transaction: {}", transaction.getId());

        // 转换为持久化对象
        PaymentTransactionPO po = assembler.toPaymentTransactionPO(transaction);

        // 更新到数据库
        transactionMapper.updateById(po);

        return transaction;
    }

    @Override
    public List<RefundTransactionEntity> findProcessingByRefundId(String refundId) {
        log.debug("Finding processing refund transaction by refund id: {}", refundId);

        LambdaQueryWrapper<PaymentTransactionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransactionPO::getPaymentId, refundId)
                .eq(PaymentTransactionPO::getTransactionType, TransactionType.REFUND)
                .eq(PaymentTransactionPO::getTransactionStatus, TransactionStatus.PROCESSING)
                .orderByDesc(PaymentTransactionPO::getCreateTime);

        List<PaymentTransactionPO> pos = transactionMapper.selectList(wrapper);

        return pos.stream()
                .map(assembler::toRefundTransactionEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<RefundTransactionEntity> findCompletedByRefundId(String refundId) {
        log.debug("Finding completed refund transactions by refund id: {}", refundId);

        LambdaQueryWrapper<PaymentTransactionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransactionPO::getPaymentId, refundId)
                .eq(PaymentTransactionPO::getTransactionType, TransactionType.REFUND)
                .in(PaymentTransactionPO::getTransactionStatus,
                        TransactionStatus.SUCCESS, TransactionStatus.FAILED)
                .orderByDesc(PaymentTransactionPO::getCreateTime);

        List<PaymentTransactionPO> pos = transactionMapper.selectList(wrapper);
        return pos.stream()
                .map(assembler::toRefundTransactionEntity)
                .collect(Collectors.toList());
    }
}