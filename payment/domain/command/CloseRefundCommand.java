package com.bytz.modules.cms.payment.domain.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 关闭退款单命令对象
 * Close Refund Command
 *
 * <p>用例来源：UC-RM-003 退款单状态管理</p>
 * <p>使用场景：订单取消、业务流程终止等场景</p>
 * <p>注意：不包含refundId，因为此命令用于聚合根实例方法refund.close(command)</p>
 * <p>领域服务会先加载聚合根，再调用实例方法</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloseRefundCommand {

    /**
     * 退款支付单ID（必填）
     * 退款流水关联的退款支付单
     */
    @NotBlank(message = "退款支付单ID不能为空")
    private String refundPaymentId;

    /**
     * 关闭原因（记录到退款单reason字段）
     */
    private String closeReason;
}