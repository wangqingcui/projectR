package com.bytz.modules.cms.payment.infrastructure.assembler;

import com.bytz.modules.cms.payment.domain.model.PaymentAggregate;
import com.bytz.modules.cms.payment.domain.model.PaymentTransactionEntity;
import com.bytz.modules.cms.payment.domain.model.RefundAggregate;
import com.bytz.modules.cms.payment.domain.model.RefundTransactionEntity;
import com.bytz.modules.cms.payment.infrastructure.entity.PaymentPO;
import com.bytz.modules.cms.payment.infrastructure.entity.PaymentTransactionPO;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 基础设施层转换器
 * Infrastructure Assembler
 * 
 * <p>负责Domain ↔ PO转换</p>
 * <p>注意：退款单和退款流水复用PaymentPO和PaymentTransactionPO，通过discriminator字段区分</p>
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentInfrastructureAssembler {
    
    // ==================== PaymentAggregate <-> PaymentPO ====================
    
    /**
     * 聚合根 -> PO
     */
    PaymentPO aggregateToPO(PaymentAggregate aggregate);
    
    /**
     * PO -> 聚合根
     */
    @Mapping(target = "paymentAmount", source = "paymentAmount", qualifiedByName = "toBigDecimalOrZero")
    @Mapping(target = "paidAmount", source = "paidAmount", qualifiedByName = "toBigDecimalOrZero")
    @Mapping(target = "currency", source = "currency", defaultValue = "CNY")
    @Mapping(target = "processingTransaction", ignore = true)
    @Mapping(target = "completedTransactions", ignore = true)
    PaymentAggregate poToAggregate(PaymentPO po);
    
    /**
     * 将PO数据同步回聚合根（用于持久化后反向同步）
     */
    void updateAggregateFromPO(@MappingTarget PaymentAggregate aggregate, PaymentPO po);
    
    /**
     * 聚合根列表 -> PO列表
     */
    List<PaymentPO> aggregatesToPOs(List<PaymentAggregate> aggregates);
    
    /**
     * PO列表 -> 聚合根列表
     */
    List<PaymentAggregate> posToAggregates(List<PaymentPO> pos);
    
    // ==================== RefundAggregate <-> PaymentPO ====================
    // 注意：退款单复用PaymentPO，通过paymentType=REFUND区分
    
    /**
     * 退款聚合根 -> PO
     * 注意：paymentType会自动映射为REFUND
     */
    @Mapping(target = "hasRefund", ignore = true)  // 退款单不使用hasRefund字段，忽略避免意外覆盖
    PaymentPO toPaymentPO(RefundAggregate aggregate);
    
    /**
     * PO -> 退款聚合根
     */
    @Mapping(target = "paymentAmount", source = "paymentAmount", qualifiedByName = "toBigDecimalOrZero")
    @Mapping(target = "paidAmount", source = "paidAmount", qualifiedByName = "toBigDecimalOrZero")
    @Mapping(target = "currency", source = "currency", defaultValue = "CNY")
    @Mapping(target = "processingTransactions", ignore = true)
    @Mapping(target = "completedTransactions", ignore = true)
    RefundAggregate toRefundAggregate(PaymentPO po);
    
    /**
     * 将PO数据同步回退款聚合根
     */
    void updateRefundAggregateFromPO(@MappingTarget RefundAggregate aggregate, PaymentPO po);
    
    // ==================== PaymentTransactionEntity <-> PaymentTransactionPO ====================
    
    /**
     * 流水实体 -> PO
     */
    PaymentTransactionPO transactionToPO(PaymentTransactionEntity transaction);
    
    /**
     * PO -> 流水实体
     */
    @Mapping(target = "transactionAmount", source = "transactionAmount", qualifiedByName = "toBigDecimalOrZero")
    PaymentTransactionEntity poToTransaction(PaymentTransactionPO po);
    
    /**
     * 将PO数据同步回流水实体（用于持久化后反向同步）
     */
    void updateTransactionFromPO(@MappingTarget PaymentTransactionEntity transaction, PaymentTransactionPO po);
    
    /**
     * 流水实体列表 -> PO列表
     */
    List<PaymentTransactionPO> transactionsToPOs(List<PaymentTransactionEntity> transactions);
    
    /**
     * PO列表 -> 流水实体列表
     */
    List<PaymentTransactionEntity> posToTransactions(List<PaymentTransactionPO> pos);
    
    // ==================== RefundTransactionEntity <-> PaymentTransactionPO ====================
    // 注意：退款流水复用PaymentTransactionPO，通过transactionType=REFUND区分
    
    /**
     * 退款流水实体 -> PO
     * 注意：transactionType会自动映射为REFUND
     */
    PaymentTransactionPO toPaymentTransactionPO(RefundTransactionEntity transaction);
    
    /**
     * PO -> 退款流水实体
     */
    @Mapping(target = "transactionAmount", source = "transactionAmount", qualifiedByName = "toBigDecimalOrZero")
    RefundTransactionEntity toRefundTransactionEntity(PaymentTransactionPO po);
    
    /**
     * 将PO数据同步回退款流水实体
     */
    void updateRefundTransactionFromPO(@MappingTarget RefundTransactionEntity transaction, PaymentTransactionPO po);
    
    /**
     * 退款流水实体列表 -> PO列表
     */
    List<PaymentTransactionPO> refundTransactionsToPOs(List<RefundTransactionEntity> transactions);
    
    /**
     * PO列表 -> 退款流水实体列表
     */
    List<RefundTransactionEntity> posToRefundTransactions(List<PaymentTransactionPO> pos);
    
    @Named("toBigDecimalOrZero")
    default BigDecimal toBigDecimalOrZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}