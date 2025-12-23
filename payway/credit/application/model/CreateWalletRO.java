package com.bytz.modules.cms.payway.credit.application.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/**
 * 创建钱包请求对象
 * Create Wallet Request Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWalletRO {

    /**
     * 经销商ID
     */
    @NotBlank(message = "经销商ID不能为空")
    private String resellerId;

    /**
     * 初始授信额度
     */
    @PositiveOrZero(message = "初始授信额度必须大于等于0")
    private BigDecimal totalLimit;

    /**
     * 账期天数
     */
    @PositiveOrZero(message = "账期天数必须大于等于0")
    private Integer termDays;

    /**
     * 是否可用于预付
     */
    @NotNull(message = "是否可用于预付不能为空")
    private Boolean prepaymentEnabled;
}