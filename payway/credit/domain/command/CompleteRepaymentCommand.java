package com.bytz.modules.cms.payway.credit.domain.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 完成还款命令对象
 * Complete Repayment Command
 * 
 * <p>用例来源：UC-CW-021</p>
 * <p>需求来源：T18-2</p>
 * <p>说明：
 *   - 返还额度：usedLimit -= 账单金额，availableLimit += 账单金额
 *   - 生成LIMIT_CHANGE类型日志
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteRepaymentCommand {
    
    /**
     * 账单ID（必填）
     */
    @NotBlank(message = "账单ID不能为空")
    private String billId;

    /**
     * 还款时间（必填）
     */
    @NotNull(message = "还款时间不能为空")
    private LocalDateTime repaymentTime;
}