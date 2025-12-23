package com.bytz.modules.cms.payway.credit.domain.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/**
 * 创建信用钱包命令
 * Create Credit Wallet Command
 *
 * <p>用例来源：UC-CW-001</p>
 * <p>需求来源：T01</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCreditWalletCommand {

    /**
     * 经销商ID（必填）
     */
    @NotBlank(message = "经销商ID不能为空")
    private String resellerId;

    /**
     * 初始授信额度（可选，默认0）
     */
    @PositiveOrZero(message = "初始授信额度不能为负数")
    private BigDecimal totalLimit;

    /**
     * 账期天数（可选，默认0天）
     */
    @PositiveOrZero(message = "账期天数必须大于0")
    private Integer termDays;

    /**
     * 是否可用于预付
     */
    @NotNull(message = "是否可用于预付不能为空")
    private Boolean prepaymentEnabled;

    /**
     * 创建人ID
     */
    private String createBy;

    /**
     * 创建人姓名
     */
    private String createByName;
}