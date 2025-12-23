package com.bytz.modules.cms.payment.domain.repository;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.domain.model.PaymentAggregate;
import com.bytz.modules.cms.payment.domain.model.PaymentTransactionEntity;

import java.util.List;
import java.util.Optional;

/**
 * 支付单仓储接口
 * Payment Repository Interface
 * 
 * <p>职责：支付单聚合的持久化和查询</p>
 * <p>接口定义在领域层，实现在基础设施层</p>
 * <p>注意：流水的持久化应通过聚合根进行，不应有独立的流水仓储</p>
 */
public interface IPaymentRepository {
    
    /**
     * 保存支付单（仅用于新增）
     *
     * @param payment 支付单聚合根
     * @return 保存后的支付单聚合根
     */
    PaymentAggregate save(PaymentAggregate payment);
    
    /**
     * 更新支付单
     *
     * @param payment 支付单聚合根
     * @return 更新后的支付单聚合根
     */
    PaymentAggregate update(PaymentAggregate payment);
    
    /**
     * 批量保存支付单（仅用于新增）
     *
     * @param payments 支付单聚合根列表
     * @return 保存后的支付单聚合根列表
     */
    List<PaymentAggregate> saveAll(List<PaymentAggregate> payments);
    
    /**
     * 批量更新支付单
     *
     * @param payments 支付单聚合根列表
     * @return 更新后的支付单聚合根列表
     */
    List<PaymentAggregate> updateAll(List<PaymentAggregate> payments);
    
    /**
     * 根据ID查询支付单（不加载流水）
     *
     * @param id 支付单ID
     * @return 支付单聚合根（可能为空）
     */
    Optional<PaymentAggregate> findById(String id);
    
    /**
     * 根据ID查询支付单，支持选择性加载流水
     * 按业务需求选择性加载：不加载所有流水时可提高查询效率
     * 加载策略：
     *   - loadTransactions=false：简单查询、创建支付单场景（不加载流水）
     *   - loadTransactions=true：支付回调、退款验证、查询流水场景
     *
     * @param id 支付单ID
     * @param loadTransactions 是否加载流水
     * @return 支付单聚合根（可能为空）
     */
    Optional<PaymentAggregate> findById(String id, boolean loadTransactions);
    
    /**
     * 根据ID列表批量查询支付单
     *
     * @param ids 支付单ID列表
     * @return 支付单聚合根列表
     */
    List<PaymentAggregate> findByIds(List<String> ids);
    
    /**
     * 根据ID列表批量查询支付单，支持选择性加载流水
     *
     * @param ids 支付单ID列表
     * @param loadTransactions 是否加载流水
     * @return 支付单聚合根列表
     */
    List<PaymentAggregate> findByIds(List<String> ids, boolean loadTransactions);
    
    /**
     * 根据支付单号查询
     *
     * @param code 支付单号
     * @return 支付单聚合根（可能为空）
     */
    Optional<PaymentAggregate> findByCode(String code);
    
    /**
     * 根据订单ID查询支付单列表
     *
     * @param orderId 订单ID
     * @return 支付单列表
     */
    List<PaymentAggregate> findByOrderId(String orderId);
    
    /**
     * 根据原支付单ID查询退款支付单列表
     *
     * @param originalPaymentId 原支付单ID
     * @return 退款支付单列表
     */
    List<PaymentAggregate> findByOriginalPaymentId(String originalPaymentId);
    
    /**
     * 根据经销商ID查询支付单列表
     *
     * @param resellerId 经销商ID
     * @return 支付单列表
     */
    List<PaymentAggregate> findByResellerId(String resellerId);
    
    /**
     * 检查支付单是否存在进行中的流水
     *
     * @param paymentId 支付单ID
     * @return true如果存在进行中的流水，否则false
     */
    boolean existsProcessingTransaction(String paymentId);
    
    /**
     * 根据支付单ID和渠道交易记录ID查询流水
     *
     * @param paymentId 支付单ID
     * @param channelTransactionId 渠道交易记录ID
     * @return 支付流水（可能为空）
     */
    Optional<PaymentTransactionEntity> findTransactionByChannelId(
            String paymentId, String channelTransactionId);
    
    /**
     * 根据渠道交易记录ID查询支付单列表
     * 回调时使用：同一channelTransactionId可能对应多个支付单（批量支付场景）
     *
     * @param channelTransactionId 渠道交易记录ID
     * @param loadTransactions 是否加载流水
     * @return 支付单聚合根列表
     */
    List<PaymentAggregate> findByChannelTransactionId(String channelTransactionId, boolean loadTransactions);
    
    /**
     * 根据原支付流水ID查询退款流水列表
     * 用于计算某个正向流水的已退款金额
     *
     * @param originalTransactionId 原支付流水ID
     * @return 退款流水列表
     */
    List<PaymentTransactionEntity> findRefundTransactionsByOriginalId(
            String originalTransactionId);
    
    /**
     * 根据渠道和交易记录ID查找支付单
     * 用于支付完成回调，通过paymentChannel+channelTransactionId唯一标识交易
     *
     * @param paymentChannel 支付渠道
     * @param channelTransactionId 渠道交易记录ID
     * @param loadTransactions 是否加载流水
     * @return 支付单聚合根列表
     */
    List<PaymentAggregate> findByChannelAndTransactionId(
            PaymentChannel paymentChannel, 
            String channelTransactionId, 
            boolean loadTransactions);
}