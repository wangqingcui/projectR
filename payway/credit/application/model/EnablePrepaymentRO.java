package com.bytz.modules.cms.payway.credit.application.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 开启预付功能请求对象
 * Enable Prepayment Request Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnablePrepaymentRO {
    
    /**
     * 钱包ID
     */
    @NotBlank(message = "钱包ID不能为空")
    private String walletId;

    /**
     * 操作原因
     */
//    @NotBlank(message = "操作原因不能为空")
    private String reason;
}