package com.bytz.modules.cms.payment.domain.repository;

import com.bytz.modules.cms.payment.domain.model.PaymentTransactionEntity;
import com.bytz.modules.cms.payment.domain.model.RefundAggregate;
import com.bytz.modules.cms.payment.domain.model.RefundTransactionEntity;

import java.util.List;
import java.util.Optional;

/**
 * 退款单仓储接口
 * Refund Repository Interface
 *
 * <p>定义退款单聚合根的持久化操作</p>
 * <p>来源：DDD分层架构，领域层定义接口，基础设施层实现</p>
 * <p>注意：退款单与支付单共用cms_payment表，通过paymentType=REFUND区分</p>
 */
public interface IRefundRepository {

    /**
     * 保存退款单聚合根
     * 
     * @param aggregate 退款单聚合根
     * @return 保存后的退款单聚合根
     */
    RefundAggregate save(RefundAggregate aggregate);

    /**
     * 根据ID查找退款单
     * 
     * @param id 退款单ID
     * @return 退款单聚合根，如果不存在则返回空
     */
    Optional<RefundAggregate> findById(String id);

    /**
     * 根据ID查找退款单
     *
     * @param id               退款单ID
     * @param loadTransactions 是否加载退款流水
     * @return 退款单聚合根，如果不存在则返回空
     */
    Optional<RefundAggregate> findById(String id, boolean loadTransactions);

    /**
     * 根据退款单号查找退款单
     * 
     * @param code 退款单号
     * @return 退款单聚合根，如果不存在则返回空
     */
    Optional<RefundAggregate> findByCode(String code);

    /**
     * 根据退款单号查找退款单
     *
     * @param code             退款单号
     * @param loadTransactions 是否加载退款流水
     * @return 退款单聚合根，如果不存在则返回空
     */
    Optional<RefundAggregate> findByCode(String code, boolean loadTransactions);

    /**
     * 根据原支付单ID查找退款单
     * 
     * @param originalPaymentId 原支付单ID
     * @return 退款单聚合根，如果不存在则返回空
     */
    List<RefundAggregate> findByOriginalPaymentId(String originalPaymentId);

    /**
     * 根据原支付单ID查找退款单
     *
     * @param originalPaymentId 原支付单ID
     * @param loadTransactions  是否加载退款流水
     * @return 退款单聚合根，如果不存在则返回空
     */
    List<RefundAggregate> findByOriginalPaymentId(String originalPaymentId, boolean loadTransactions);

    /**
     * 更新退款单
     * 
     * @param aggregate 退款单聚合根
     * @return 更新后的退款单聚合根
     */
    RefundAggregate update(RefundAggregate aggregate);

    /**
     * 删除退款单（逻辑删除）
     * 
     * @param id 退款单ID
     */
    void deleteById(String id);

    /**
     * 根据原支付流水ID查询退款流水列表
     * 用于计算某个正向流水的已退款金额
     *
     * @param originalTransactionId 原支付流水ID
     * @return 退款流水列表
     */
    List<RefundTransactionEntity> findRefundTransactionsByOriginalId(
            String originalTransactionId);

}