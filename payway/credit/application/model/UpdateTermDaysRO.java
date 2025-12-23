package com.bytz.modules.cms.payway.credit.application.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 更新账期请求对象
 * Update Term Days Request Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTermDaysRO {
    
    /**
     * 钱包ID
     */
    @NotBlank(message = "钱包ID不能为空")
    private String walletId;
    
    /**
     * 新的账期天数
     */
    @NotNull(message = "账期天数不能为空")
    @Min(value = 1, message = "账期天数必须大于0")
    private Integer newTermDays;
    
    /**
     * 更新原因
     */
//    @NotBlank(message = "更新原因不能为空")
    private String reason;
}