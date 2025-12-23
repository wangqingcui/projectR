package com.bytz.modules.cms.payment.domain.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 关闭支付单命令对象
 * Close Payment Command
 * 
 * <p>用例来源：UC-PM-004 支付单关闭</p>
 * <p>使用场景：订单取消、业务流程终止等场景</p>
 * <p>注意：不包含paymentId，因为此命令用于聚合根实例方法payment.close(command)</p>
 * <p>领域服务会先加载聚合根，再调用实例方法</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClosePaymentCommand {
    
    /**
     * 关闭原因
     */
    @NotBlank(message = "关闭原因不能为空")
    private String closeReason;
    
    /**
     * 业务来源（可选）
     */
    private String businessSource;
}
