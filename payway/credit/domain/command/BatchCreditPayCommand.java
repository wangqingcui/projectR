package com.bytz.modules.cms.payway.credit.domain.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 批量信用支付命令对象
 * Batch Credit Pay Command
 * 
 * <p>用例来源：UC-CW-012</p>
 * <p>需求来源：T16</p>
 * <p>说明：批量支付是原子操作，全部成功或全部失败</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchCreditPayCommand {
    
    /**
     * 批量支付项列表（至少1个）
     */
    @NotEmpty(message = "支付项列表不能为空")
    @Valid
    private List<CreditPayCommand> payments;
    

}