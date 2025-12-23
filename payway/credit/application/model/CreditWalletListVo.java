package com.bytz.modules.cms.payway.credit.application.model;

import com.bytz.common.aspect.annotation.Dict;
import com.bytz.common.entity.annotation.MPJFieldMapping;
import com.bytz.modules.cms.payway.credit.infrastructure.entity.CreditWalletPO;
import com.bytz.modules.cms.reseller.entity.Reseller;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreditWalletListVo {


    @MPJFieldMapping(entityClass = Reseller.class)
    @ApiModelProperty(value = "经销商名称")
    private String resellerName;
    /**
     * 钱包ID
     */
    @MPJFieldMapping(entityClass = CreditWalletPO.class)
    @ApiModelProperty(value = "钱包ID")
    private String id;

    /**
     * 经销商ID
     */
    @MPJFieldMapping(entityClass = CreditWalletPO.class)
    @ApiModelProperty(value = "经销商ID")
    private String resellerId;

    /**
     * 是否启用
     */
    @MPJFieldMapping(entityClass = CreditWalletPO.class)
    @Dict(dicCode = "boolean_flag_type")
    @ApiModelProperty(value = "是否启用")
    private Boolean enabled;

    /**
     * 是否冻结
     */
    @MPJFieldMapping(entityClass = CreditWalletPO.class)
    @Dict(dicCode = "boolean_flag_type")
    @ApiModelProperty(value = "是否冻结")
    private Boolean frozen;

    /**
     * 是否可用于预付
     */
    @ApiModelProperty(value = "是否可用于预付")
    @Dict(dicCode = "boolean_flag_type")
    private Boolean prepaymentEnabled;

    /**
     * 总授信额度
     */
    @MPJFieldMapping(entityClass = CreditWalletPO.class)
    @ApiModelProperty(value = "总授信额度")
    private BigDecimal totalLimit;

    /**
     * 可用额度
     */
    @MPJFieldMapping(entityClass = CreditWalletPO.class)
    @ApiModelProperty(value = "可用额度")
    private BigDecimal availableLimit;

    /**
     * 已使用额度
     */
    @MPJFieldMapping(entityClass = CreditWalletPO.class)
    @ApiModelProperty(value = "已使用额度")
    private BigDecimal usedLimit;

    /**
     * 账期天数
     */
    @MPJFieldMapping(entityClass = CreditWalletPO.class)
    @ApiModelProperty(value = "账期天数")
    private Integer termDays;

    /**
     * 币种
     */
    @MPJFieldMapping(entityClass = CreditWalletPO.class)
    @ApiModelProperty(value = "币种")
    private String currency;

    /**
     * 创建时间
     */
    @MPJFieldMapping(entityClass = CreditWalletPO.class)
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @MPJFieldMapping(entityClass = CreditWalletPO.class)
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}
