package com.bytz.modules.cms.payment.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.domain.enums.TransactionStatus;
import com.bytz.modules.cms.payment.domain.model.PaymentAggregate;
import com.bytz.modules.cms.payment.domain.model.PaymentTransactionEntity;
import com.bytz.modules.cms.payment.domain.repository.IPaymentRepository;
import com.bytz.modules.cms.payment.infrastructure.assembler.PaymentInfrastructureAssembler;
import com.bytz.modules.cms.payment.infrastructure.entity.PaymentPO;
import com.bytz.modules.cms.payment.infrastructure.entity.PaymentTransactionPO;
import com.bytz.modules.cms.payment.infrastructure.mapper.PaymentMapper;
import com.bytz.modules.cms.payment.infrastructure.mapper.PaymentTransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 支付单仓储实现
 * Payment Repository Implementation
 *
 * <p>实现IPaymentRepository接口，处理支付单聚合根的持久化</p>
 * <p>流水的持久化通过聚合根进行，不存在独立的流水仓储</p>
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements IPaymentRepository {

    private final PaymentMapper paymentMapper;
    private final PaymentTransactionMapper transactionMapper;
    private final PaymentInfrastructureAssembler paymentInfrastructureAssembler;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentAggregate save(PaymentAggregate payment) {
        log.debug("保存支付单聚合根（新增），支付单号: {}", payment.getCode());

        // 转换为PO
        PaymentPO po = paymentInfrastructureAssembler.aggregateToPO(payment);

        // 新增
        paymentMapper.insert(po);

        // 同步持久化后的数据回聚合根
        paymentInfrastructureAssembler.updateAggregateFromPO(payment, po);

        // 保存进行中流水
        saveProcessingTransaction(payment);

        return payment;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentAggregate update(PaymentAggregate payment) {
        log.debug("更新支付单聚合根，支付单号: {}", payment.getCode());

        // 转换为PO
        PaymentPO po = paymentInfrastructureAssembler.aggregateToPO(payment);

        // 更新
        paymentMapper.updateById(po);

        // 同步持久化后的数据回聚合根
        paymentInfrastructureAssembler.updateAggregateFromPO(payment, po);

        // 保存进行中流水（新增或更新）
        saveProcessingTransaction(payment);
        return payment;
    }

    /**
     * 保存进行中流水（新增或更新）
     */
    private void saveProcessingTransaction(PaymentAggregate payment) {
        PaymentTransactionEntity processingTransaction = payment.getProcessingTransaction();
        if (processingTransaction != null) {
            PaymentTransactionPO transactionPO = paymentInfrastructureAssembler.transactionToPO(processingTransaction);
            if (processingTransaction.getId() == null) {
                transactionPO.setPaymentId(payment.getId());
                // 新增
                transactionMapper.insert(transactionPO);
            } else {
                // 更新
                transactionMapper.updateById(transactionPO);
            }
            paymentInfrastructureAssembler.updateTransactionFromPO(processingTransaction, transactionPO);
        //  将进行中流水移动到已完成列表
            payment.completeProcessingTransaction();
        }
    }

    /**
     * 更新已完成的流水（状态变更）
     */
    private void updateCompletedTransactions(PaymentAggregate payment) {
        List<PaymentTransactionEntity> completedTransactions = payment.getCompletedTransactions();
        if (completedTransactions == null) {
            return;
        }
        for (PaymentTransactionEntity transaction : completedTransactions) {
            if (transaction.getId() != null) {
                PaymentTransactionPO transactionPO = paymentInfrastructureAssembler.transactionToPO(transaction);
                transactionMapper.updateById(transactionPO);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PaymentAggregate> saveAll(List<PaymentAggregate> payments) {
        log.debug("批量保存支付单聚合根（新增），数量: {}", payments.size());

        List<PaymentAggregate> result = new ArrayList<>();
        for (PaymentAggregate payment : payments) {
            result.add(save(payment));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PaymentAggregate> updateAll(List<PaymentAggregate> payments) {
        log.debug("批量更新支付单聚合根，数量: {}", payments.size());

        List<PaymentAggregate> result = new ArrayList<>();
        for (PaymentAggregate payment : payments) {
            result.add(update(payment));
        }
        return result;
    }

    @Override
    public Optional<PaymentAggregate> findById(String id) {
        return findById(id, false);
    }

    @Override
    public Optional<PaymentAggregate> findById(String id, boolean loadTransactions) {
        log.debug("根据ID查找支付单，ID: {}, loadTransactions: {}", id, loadTransactions);

        PaymentPO po = paymentMapper.selectById(id);
        if (po == null) {
            return Optional.empty();
        }

        PaymentAggregate aggregate = paymentInfrastructureAssembler.poToAggregate(po);

        // 选择性加载流水
        if (loadTransactions) {
            loadTransactionsForAggregate(aggregate);
        }

        return Optional.of(aggregate);
    }

    @Override
    public List<PaymentAggregate> findByIds(List<String> ids) {
        return findByIds(ids, false);
    }

    @Override
    public List<PaymentAggregate> findByIds(List<String> ids, boolean loadTransactions) {
        log.debug("根据ID列表批量查找支付单，数量: {}, loadTransactions: {}", ids.size(), loadTransactions);

        if (ids.isEmpty()) {
            return new ArrayList<>();
        }

        List<PaymentPO> pos = paymentMapper.selectBatchIds(ids);
        List<PaymentAggregate> aggregates = paymentInfrastructureAssembler.posToAggregates(pos);

        // 选择性加载流水
        if (loadTransactions && !aggregates.isEmpty()) {
            loadTransactionsForAggregates(aggregates, ids);
        }

        return aggregates;
    }

    /**
     * 为单个聚合根加载流水
     */
    private void loadTransactionsForAggregate(PaymentAggregate aggregate) {
        LambdaQueryWrapper<PaymentTransactionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransactionPO::getPaymentId, aggregate.getId())
                .orderByDesc(PaymentTransactionPO::getCreateTime);

        List<PaymentTransactionPO> transactionPOs = transactionMapper.selectList(wrapper);

        // 按状态分组
        PaymentTransactionEntity processingTransaction = null;
        List<PaymentTransactionEntity> completedList = new ArrayList<>();

        for (PaymentTransactionPO po : transactionPOs) {
            PaymentTransactionEntity transaction = paymentInfrastructureAssembler.poToTransaction(po);
            if (TransactionStatus.PROCESSING.equals(transaction.getTransactionStatus())) {
                // 只会有一条进行中的流水
                processingTransaction = transaction;
            } else {
                // SUCCESS 或 FAILED
                completedList.add(transaction);
            }
        }

        aggregate.setProcessingTransaction(processingTransaction);
        aggregate.setCompletedTransactions(completedList);
    }

    /**
     * 为多个聚合根批量加载流水
     */
    private void loadTransactionsForAggregates(List<PaymentAggregate> aggregates, List<String> paymentIds) {
        // 批量查询所有流水
        LambdaQueryWrapper<PaymentTransactionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(PaymentTransactionPO::getPaymentId, paymentIds)
                .orderByDesc(PaymentTransactionPO::getCreateTime);
        List<PaymentTransactionPO> transactionPOs = transactionMapper.selectList(wrapper);

        // 按paymentId和状态分组
        Map<String, PaymentTransactionEntity> processingMap = new HashMap<>();
        Map<String, List<PaymentTransactionEntity>> completedMap = new HashMap<>();

        for (PaymentTransactionPO po : transactionPOs) {
            PaymentTransactionEntity transaction = paymentInfrastructureAssembler.poToTransaction(po);
            String paymentId = transaction.getPaymentId();

            if (TransactionStatus.PROCESSING.equals(transaction.getTransactionStatus())) {
                // 只会有一条进行中的流水
                processingMap.put(paymentId, transaction);
            } else {
                completedMap.computeIfAbsent(paymentId, k -> new ArrayList<>()).add(transaction);
            }
        }

        // 设置流水到聚合根
        for (PaymentAggregate aggregate : aggregates) {
            aggregate.setProcessingTransaction(processingMap.get(aggregate.getId()));
            aggregate.setCompletedTransactions(
                    completedMap.getOrDefault(aggregate.getId(), new ArrayList<>()));
        }
    }

    @Override
    public Optional<PaymentAggregate> findByCode(String code) {
        log.debug("根据支付单号查找支付单，code: {}", code);

        LambdaQueryWrapper<PaymentPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentPO::getCode, code);

        PaymentPO po = paymentMapper.selectOne(wrapper);
        if (po == null) {
            return Optional.empty();
        }

        PaymentAggregate aggregate = paymentInfrastructureAssembler.poToAggregate(po);
        return Optional.of(aggregate);
    }

    @Override
    public List<PaymentAggregate> findByOrderId(String orderId) {
        log.debug("根据订单ID查找支付单列表，orderId: {}", orderId);

        LambdaQueryWrapper<PaymentPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentPO::getOrderId, orderId);

        List<PaymentPO> pos = paymentMapper.selectList(wrapper);
        return paymentInfrastructureAssembler.posToAggregates(pos);
    }

    @Override
    public List<PaymentAggregate> findByOriginalPaymentId(String originalPaymentId) {
        log.debug("根据原支付单ID查找退款支付单列表，originalPaymentId: {}", originalPaymentId);

        LambdaQueryWrapper<PaymentPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentPO::getOriginalPaymentId, originalPaymentId);

        List<PaymentPO> pos = paymentMapper.selectList(wrapper);
        return paymentInfrastructureAssembler.posToAggregates(pos);
    }

    @Override
    public List<PaymentAggregate> findByResellerId(String resellerId) {
        log.debug("根据经销商ID查找支付单列表，resellerId: {}", resellerId);

        LambdaQueryWrapper<PaymentPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentPO::getResellerId, resellerId);

        List<PaymentPO> pos = paymentMapper.selectList(wrapper);
        return paymentInfrastructureAssembler.posToAggregates(pos);
    }

    @Override
    public boolean existsProcessingTransaction(String paymentId) {
        log.debug("检查支付单是否存在进行中的流水，paymentId: {}", paymentId);

        LambdaQueryWrapper<PaymentTransactionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransactionPO::getPaymentId, paymentId)
                .eq(PaymentTransactionPO::getTransactionStatus, TransactionStatus.PROCESSING);

        return transactionMapper.selectCount(wrapper) > 0;
    }

    @Override
    public Optional<PaymentTransactionEntity> findTransactionByChannelId(String paymentId, String channelTransactionId) {
        log.debug("根据渠道交易ID查找流水，paymentId: {}, channelTransactionId: {}", paymentId, channelTransactionId);

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
    public List<PaymentTransactionEntity> findRefundTransactionsByOriginalId(String originalTransactionId) {
        log.debug("根据原支付流水ID查询退款流水，originalTransactionId: {}", originalTransactionId);

        LambdaQueryWrapper<PaymentTransactionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransactionPO::getOriginalTransactionId, originalTransactionId)
                .orderByDesc(PaymentTransactionPO::getCreateTime);

        List<PaymentTransactionPO> pos = transactionMapper.selectList(wrapper);
        return paymentInfrastructureAssembler.posToTransactions(pos);
    }

    @Override
    public List<PaymentAggregate> findByChannelTransactionId(String channelTransactionId, boolean loadTransactions) {
        log.debug("根据渠道交易记录ID查询支付单，channelTransactionId: {}, loadTransactions: {}",
                channelTransactionId, loadTransactions);

        // 先查询所有包含该channelTransactionId的流水
        LambdaQueryWrapper<PaymentTransactionPO> transactionWrapper = new LambdaQueryWrapper<>();
        transactionWrapper.eq(PaymentTransactionPO::getChannelTransactionId, channelTransactionId);
        List<PaymentTransactionPO> transactionPOs = transactionMapper.selectList(transactionWrapper);

        if (transactionPOs.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有支付单ID
        List<String> paymentIds = transactionPOs.stream()
                .map(PaymentTransactionPO::getPaymentId)
                .distinct()
                .collect(Collectors.toList());

        // 查询支付单
        return findByIds(paymentIds, loadTransactions);
    }

    @Override
    public List<PaymentAggregate> findByChannelAndTransactionId(
            PaymentChannel paymentChannel,
            String channelTransactionId,
            boolean loadTransactions) {
        log.debug("根据渠道和交易记录ID查询支付单，paymentChannel: {}, channelTransactionId: {}, loadTransactions: {}",
                paymentChannel, channelTransactionId, loadTransactions);

        // 先查询所有包含该paymentChannel和channelTransactionId的流水
        LambdaQueryWrapper<PaymentTransactionPO> transactionWrapper = new LambdaQueryWrapper<>();
        transactionWrapper.eq(PaymentTransactionPO::getPaymentChannel, paymentChannel)
                .eq(PaymentTransactionPO::getChannelTransactionId, channelTransactionId);
        List<PaymentTransactionPO> transactionPOs = transactionMapper.selectList(transactionWrapper);

        if (transactionPOs.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有支付单ID
        List<String> paymentIds = transactionPOs.stream()
                .map(PaymentTransactionPO::getPaymentId)
                .distinct()
                .collect(Collectors.toList());

        // 查询支付单
        return findByIds(paymentIds, loadTransactions);
    }
}