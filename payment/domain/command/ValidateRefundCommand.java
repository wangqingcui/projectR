package com.bytz.modules.cms.payment.domain.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 验证退款请求命令对象
 * Validate Refund Command
 *
 * <p>用例来源：UC-RM-002 退款单验证</p>
 * <p>使用场景：退款渠道验证退款请求的合法性</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateRefundCommand {


    /**
     * 原支付流水ID（必须填写）
     */
    private String originalTransactionId;

    /**
     * 退款金额（必须大于0）
     */
    private BigDecimal amount;
}