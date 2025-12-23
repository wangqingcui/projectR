package com.bytz.modules.cms.payment.domain.repository;

import com.bytz.modules.cms.payment.domain.model.PaymentTransactionEntity;

import java.util.List;
import java.util.Optional;

/**
 * 支付流水仓储接口
 * Payment Transaction Repository Interface
 * 
 * <p>职责：支付流水的持久化和查询</p>
 * <p>接口定义在领域层，实现在基础设施层</p>
 */
public interface IPaymentTransactionRepository {
    

    /**
     * 根据ID查询流水
     *
     * @param id 流水ID
     * @return 支付流水（可能为空）
     */
    Optional<PaymentTransactionEntity> findById(String id);
    
    /**
     * 根据ID列表批量查询流水
     *
     * @param ids 流水ID列表
     * @return 支付流水列表
     */
    List<PaymentTransactionEntity> findByIds(List<String> ids);
    
    /**
     * 根据支付单ID查询流水列表
     *
     * @param paymentId 支付单ID
     * @return 支付流水列表
     */
    List<PaymentTransactionEntity> findByPaymentId(String paymentId);
    
    /**
     * 根据支付单ID列表批量查询流水
     *
     * @param paymentIds 支付单ID列表
     * @return 支付流水列表
     */
    List<PaymentTransactionEntity> findByPaymentIds(List<String> paymentIds);
    
    /**
     * 根据支付单ID和渠道交易记录ID联合查询
     *
     * @param paymentId 支付单ID
     * @param channelTransactionId 渠道交易记录ID
     * @return 支付流水（可能为空）
     */
    Optional<PaymentTransactionEntity> findByPaymentIdAndChannelTransactionId(String paymentId, String channelTransactionId);
    
    /**
     * 检查支付单是否存在进行中的流水
     *
     * @param paymentId 支付单ID
     * @return true如果存在进行中的流水，否则false
     */
    boolean existsProcessingByPaymentId(String paymentId);
    
    /**
     * 查询支付单的成功流水列表
     *
     * @param paymentId 支付单ID
     * @return 成功的支付流水列表
     */
    List<PaymentTransactionEntity> findSuccessfulByPaymentId(String paymentId);
    
    /**
     * 根据原支付流水ID查询退款流水列表
     * 用于计算某个正向流水的已退款金额
     *
     * @param originalTransactionId 原支付流水ID
     * @return 退款流水列表
     */
    List<PaymentTransactionEntity> findByOriginalTransactionId(String originalTransactionId);
}