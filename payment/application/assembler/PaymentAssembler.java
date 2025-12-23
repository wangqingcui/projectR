package com.bytz.modules.cms.payment.application.assembler;

import com.bytz.modules.cms.payment.application.model.*;
import com.bytz.modules.cms.payment.domain.command.CompletePaymentCommand;
import com.bytz.modules.cms.payment.domain.command.CreatePaymentCommand;
import com.bytz.modules.cms.payment.domain.command.CreateRefundCommand;
import com.bytz.modules.cms.payment.domain.command.CreateTransactionCommand;
import com.bytz.modules.cms.payment.domain.model.PaymentAggregate;
import com.bytz.modules.cms.payment.domain.model.PaymentTransactionEntity;
import com.bytz.modules.cms.payment.domain.model.RefundAggregate;
import com.bytz.modules.cms.payment.domain.model.RefundTransactionEntity;
import com.bytz.modules.cms.payment.infrastructure.entity.PaymentPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 支付单转换器
 * Payment Assembler
 *
 * <p>负责RO/VO ↔ Domain对象的转换</p>
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentAssembler {

    // ==================== Aggregate -> VO ====================

    /**
     * PaymentAggregate转换为PaymentVO
     */
    @Mapping(target = "pendingAmount", expression = "java(aggregate.getPendingAmount())")
    PaymentVO toVO(PaymentAggregate aggregate);

    /**
     * PaymentPO转换为PaymentVO
     */
    PaymentVO toVO(PaymentPO paymentPO);

    /**
     * PaymentAggregate列表转换为PaymentVO列表
     */
    List<PaymentVO> toVOList(List<PaymentAggregate> aggregates);

    // ==================== Transaction -> VO ====================

    /**
     * PaymentTransactionEntity转换为PaymentTransactionVO
     */
    PaymentTransactionVO toVO(PaymentTransactionEntity transaction);

    /**
     * PaymentTransactionEntity列表转换为PaymentTransactionVO列表
     */
    List<PaymentTransactionVO> toTransactionVOList(List<PaymentTransactionEntity> transactions);

    // ==================== Refund -> VO ====================

    /**
     * RefundAggregate转换为PaymentVO
     */
    PaymentVO toVO(RefundAggregate aggregate);

    /**
     * RefundTransactionEntity转换为PaymentTransactionVO
     */
    PaymentTransactionVO toRefundTransactionVO(RefundTransactionEntity transaction);

    /**
     * RefundTransactionEntity列表转换为PaymentTransactionVO列表
     */
    List<PaymentTransactionVO> toRefundTransactionVOList(List<RefundTransactionEntity> transactions);

    // ==================== RO -> Command ====================

    /**
     * PaymentCreateRO转换为CreatePaymentCommand
     * 仅用于正向支付单，退款请使用toCreateRefundCommand
     */
    CreatePaymentCommand toCreateCommand(PaymentCreateRO ro);

    /**
     * ExecutePaymentRO转换为CreateTransactionCommand
     */
    @Mapping(target = "transactionType", expression = "java(com.bytz.modules.cms.payment.domain.enums.TransactionType.PAYMENT)")
    @Mapping(target = "originalTransactionId", ignore = true)
    CreateTransactionCommand toCreateTransactionCommand(ExecutePaymentRO ro);

    /**
     * CompletePaymentRO转换为CompletePaymentCommand
     */
    CompletePaymentCommand toCompleteCommand(CompletePaymentRO ro);

    /**
     * CreateRefundRO转换为CreateRefundCommand（独立退款命令）
     */
    CreateRefundCommand toCreateRefundCommand(CreateRefundRO ro);
}