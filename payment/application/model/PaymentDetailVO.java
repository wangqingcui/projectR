package com.bytz.modules.cms.payment.application.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 支付单详情响应对象
 * Payment Detail Response Object
 * 
 * <p>用于返回支付单的完整详情，包括支付单信息和流水记录</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetailVO {

    /**
     * 支付单信息
     */
    private PaymentVO payment;

    /**
     * 支付流水列表
     */
    private List<PaymentTransactionVO> transactions;
}
