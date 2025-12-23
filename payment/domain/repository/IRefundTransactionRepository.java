package com.bytz.modules.cms.payment.domain.repository;

import com.bytz.modules.cms.payment.domain.model.RefundTransactionEntity;

import java.util.List;
import java.util.Optional;

/**
 * 退款流水仓储接口
 * Refund Transaction Repository Interface
 *
 * <p>定义退款流水实体的持久化操作</p>
 * <p>来源：DDD分层架构，领域层定义接口，基础设施层实现</p>
 * <p>注意：退款流水与支付流水共用cms_payment_transaction表，通过transactionType=REFUND区分</p>
 */
public interface IRefundTransactionRepository {

    /**
     * 保存退款流水
     * 
     * @param transaction 退款流水实体
     * @return 保存后的退款流水实体
     */
    RefundTransactionEntity save(RefundTransactionEntity transaction);

    /**
     * 根据ID查找退款流水
     * 
     * @param id 退款流水ID
     * @return 退款流水实体，如果不存在则返回空
     */
    Optional<RefundTransactionEntity> findById(String id);

    /**
     * 根据退款单ID查找所有退款流水
     * 
     * @param refundId 退款单ID（对应paymentId字段）
     * @return 退款流水列表
     */
    List<RefundTransactionEntity> findByRefundId(String refundId);

    /**
     * 根据渠道交易记录ID查找退款流水
     * 用于回调时定位退款流水
     * 
     * @param channelTransactionId 渠道交易记录ID
     * @return 退款流水实体，如果不存在则返回空
     */
    Optional<RefundTransactionEntity> findByChannelTransactionId(String channelTransactionId);

    /**
     * 根据原支付流水ID查找退款流水
     * 用于验证1:1映射约束
     * 
     * @param originalTransactionId 原支付流水ID
     * @return 退款流水实体，如果不存在则返回空
     */
    Optional<RefundTransactionEntity> findByOriginalTransactionId(String originalTransactionId);

    /**
     * 更新退款流水
     * 
     * @param transaction 退款流水实体
     * @return 更新后的退款流水实体
     */
    RefundTransactionEntity update(RefundTransactionEntity transaction);

    /**
     * 查找退款单的进行中流水（PROCESSING状态）
     * 
     * @param refundId 退款单ID
     * @return 进行中的退款流水，如果不存在则返回空
     */
    List<RefundTransactionEntity> findProcessingByRefundId(String refundId);

    /**
     * 查找退款单的已完成流水（SUCCESS或FAILED状态）
     * 
     * @param refundId 退款单ID
     * @return 已完成的退款流水列表
     */
    List<RefundTransactionEntity> findCompletedByRefundId(String refundId);
}