package com.bytz.modules.cms.payment.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bytz.modules.cms.payment.domain.enums.PaymentType;
import com.bytz.modules.cms.payment.domain.enums.TransactionStatus;
import com.bytz.modules.cms.payment.domain.model.PaymentAggregate;
import com.bytz.modules.cms.payment.domain.model.PaymentTransactionEntity;
import com.bytz.modules.cms.payment.domain.model.RefundAggregate;
import com.bytz.modules.cms.payment.domain.model.RefundTransactionEntity;
import com.bytz.modules.cms.payment.domain.repository.IRefundRepository;
import com.bytz.modules.cms.payment.infrastructure.assembler.PaymentInfrastructureAssembler;
import com.bytz.modules.cms.payment.infrastructure.entity.PaymentPO;
import com.bytz.modules.cms.payment.infrastructure.entity.PaymentTransactionPO;
import com.bytz.modules.cms.payment.infrastructure.mapper.PaymentMapper;
import com.bytz.modules.cms.payment.infrastructure.mapper.PaymentTransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 退款单仓储实现
 * Refund Repository Implementation
 *
 * <p>实现退款单聚合根的持久化操作</p>
 * <p>注意：退款单与支付单共用cms_payment表和PaymentPO，通过paymentType=REFUND区分</p>
 * <p>复用PaymentInfrastructureAssembler进行领域对象与持久化对象的转换</p>
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RefundRepositoryImpl implements IRefundRepository {

    private final PaymentMapper paymentMapper;
    private final PaymentInfrastructureAssembler assembler;
    private final PaymentTransactionMapper transactionMapper;

    @Override
    public RefundAggregate save(RefundAggregate aggregate) {
        log.debug("Saving refund aggregate: {}", aggregate.getId());

        // 转换为持久化对象
        PaymentPO po = assembler.toPaymentPO(aggregate);

        // 保存到数据库
        paymentMapper.insert(po);

        // 设置生成的ID
        aggregate.setId(po.getId());
        aggregate.setCode(po.getCode());

        saveProcessingTransaction(aggregate);
        return aggregate;
    }

    @Override
    public Optional<RefundAggregate> findById(String id) {
        return findById(id, true);
    }

    @Override
    public Optional<RefundAggregate> findById(String id, boolean loadTransactions) {
        log.debug("Finding refund by id: {}, loadTransactions: {}", id, loadTransactions);

        // 查询退款单（paymentType=REFUND）
        LambdaQueryWrapper<PaymentPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentPO::getId, id)
                .eq(PaymentPO::getPaymentType, PaymentType.REFUND);

        PaymentPO po = paymentMapper.selectOne(wrapper);
        if (po == null) {
            return Optional.empty();
        }

        // 转换为领域对象
        RefundAggregate aggregate = assembler.toRefundAggregate(po);

        // 加载退款流水
        if (loadTransactions) {
            loadTransactions(aggregate);
        }

        return Optional.of(aggregate);
    }

    @Override
    public Optional<RefundAggregate> findByCode(String code) {
        return findByCode(code, true);
    }

    @Override
    public Optional<RefundAggregate> findByCode(String code, boolean loadTransactions) {
        log.debug("Finding refund by code: {}, loadTransactions: {}", code, loadTransactions);

        LambdaQueryWrapper<PaymentPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentPO::getCode, code)
                .eq(PaymentPO::getPaymentType, PaymentType.REFUND);

        PaymentPO po = paymentMapper.selectOne(wrapper);
        if (po == null) {
            return Optional.empty();
        }

        RefundAggregate aggregate = assembler.toRefundAggregate(po);
        if (loadTransactions) {
            loadTransactions(aggregate);
        }

        return Optional.of(aggregate);
    }

    @Override
    public List<RefundAggregate> findByOriginalPaymentId(String originalPaymentId) {
        return findByOriginalPaymentId(originalPaymentId, true);
    }

    @Override
    public List<RefundAggregate> findByOriginalPaymentId(String originalPaymentId, boolean loadTransactions) {
        log.debug("Finding refund by original payment id: {}, loadTransactions: {}", originalPaymentId, loadTransactions);

        LambdaQueryWrapper<PaymentPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentPO::getOriginalPaymentId, originalPaymentId)
                .eq(PaymentPO::getPaymentType, PaymentType.REFUND);

        List<PaymentPO> poList = paymentMapper.selectList(wrapper);

        return poList.stream().map(po -> {
            RefundAggregate aggregate = assembler.toRefundAggregate(po);
            if (loadTransactions) {
                loadTransactions(aggregate);
            }
            return aggregate;
        }).collect(Collectors.toList());
    }

    @Override
    public RefundAggregate update(RefundAggregate aggregate) {
        log.debug("Updating refund aggregate: {}", aggregate.getId());

        // 转换为持久化对象
        PaymentPO po = assembler.toPaymentPO(aggregate);

        // 更新到数据库
        paymentMapper.updateById(po);

        return aggregate;
    }

    @Override
    public void deleteById(String id) {
        log.debug("Deleting refund by id: {}", id);

        // 逻辑删除
        paymentMapper.deleteById(id);
    }

    @Override
    public List<RefundTransactionEntity> findRefundTransactionsByOriginalId(String originalTransactionId) {
        log.debug("根据原支付流水ID查询退款流水，originalTransactionId: {}", originalTransactionId);

        LambdaQueryWrapper<PaymentTransactionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransactionPO::getOriginalTransactionId, originalTransactionId)
                .orderByDesc(PaymentTransactionPO::getCreateTime);

        List<PaymentTransactionPO> pos = transactionMapper.selectList(wrapper);
        return assembler.posToRefundTransactions(pos);
    }

    /**
     * 加载退款流水（进行中和已完成）
     */
    private void loadTransactions(RefundAggregate aggregate) {
        LambdaQueryWrapper<PaymentTransactionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransactionPO::getPaymentId, aggregate.getId())
                .orderByDesc(PaymentTransactionPO::getCreateTime);

        List<PaymentTransactionPO> transactionPOs = transactionMapper.selectList(wrapper);
        List<RefundTransactionEntity> refunds = assembler.posToRefundTransactions(transactionPOs);

        Map<Boolean, List<RefundTransactionEntity>> collect = refunds.stream().collect(Collectors.partitioningBy(trans -> trans.getTransactionStatus() == TransactionStatus.PROCESSING));

        aggregate.setProcessingTransactions(collect.getOrDefault(true, new ArrayList<>()));
        aggregate.setCompletedTransactions(collect.getOrDefault(false, new ArrayList<>()));
    }

    /**
     * 保存进行中流水（新增或更新）
     */
    private void saveProcessingTransaction(RefundAggregate payment) {
        List<RefundTransactionEntity> processingTransactions = payment.getProcessingTransactions();
        if (CollectionUtils.isEmpty(processingTransactions)) {
            return;
        }
        
        for (RefundTransactionEntity transaction : processingTransactions) {
            PaymentTransactionPO transactionPO = assembler.toPaymentTransactionPO(transaction);
            if (transaction.getId() == null) {
                transactionPO.setPaymentId(payment.getId());
                // 新增
                transactionMapper.insert(transactionPO);
            } else {
                // 更新
                transactionMapper.updateById(transactionPO);
            }
            assembler.updateRefundTransactionFromPO(transaction, transactionPO);
        }
        payment.completeProcessingTransaction();
    }
}