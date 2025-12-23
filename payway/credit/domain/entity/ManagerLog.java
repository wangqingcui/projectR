package com.bytz.modules.cms.payway.credit.domain.entity;

import com.bytz.common.aspect.annotation.Dict;
import com.bytz.modules.cms.payway.credit.domain.enums.ManageChangeType;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 管理日志实体
 * Manager Log Entity
 *
 * <p>记录钱包的管理变更和交易引发的额度变化。</p>
 * <p>需求来源：需求文档 T07-钱包管理日志记录</p>
 * <p>说明：
 * - 使用JSON格式存储afterState，支持不同场景
 * - 管理员调整：额度、账期、冻结、启用
 * - 交易自动：信用支付、还款引发的额度变化
 * - 日志表只有创建操作，没有更新操作
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerLog {

    /**
     * 日志主键ID，UUID，32位字符
     */
    @ApiModelProperty(value = "日志主键ID，UUID，32位字符")
    private String id;

    /**
     * 钱包ID，外键
     */
    @ApiModelProperty(value = "钱包ID，外键")
    private String creditWalletId;

    /**
     * 经销商ID
     */
    @ApiModelProperty(value = "经销商ID")
    private String resellerId;

    /**
     * 操作类型：LIMIT_CHANGE, TERM_CHANGE, FREEZE_STATUS_CHANGE, ENABLE_STATUS_CHANGE
     */
    @ApiModelProperty(value = "操作类型：LIMIT_CHANGE, TERM_CHANGE, FREEZE_STATUS_CHANGE, ENABLE_STATUS_CHANGE")
    @Dict(dicCode = "manage_change_type")
    private ManageChangeType operationType;

    /**
     * 变更后状态，使用Map存储，持久化时转换为MySQL JSON类型
     * 格式示例：
     * 1. 管理员额度调整：
     * {"totalLimit": 100000.00, "availableLimit": 80000.00, "usedLimit": 20000.00, "changeType": "LIMIT_ADJUST", "changeReason": "信用评级下调"}
     * 2. 交易额度变更（支付）：
     * {"totalLimit": 100000.00, "availableLimit": 75000.00, "usedLimit": 25000.00, "changeAmount": 5000.00, "changeType": "CREDIT_PAY", "billId": "BILL123"}
     * 3. 交易额度变更（还款）：
     * {"totalLimit": 100000.00, "availableLimit": 80000.00, "usedLimit": 20000.00, "changeAmount": 5000.00, "changeType": "REPAYMENT", "billId": "BILL123"}
     * 4. 账期变更：
     * {"termDays": 60}
     * 5. 冻结状态变更：
     * {"frozen": true, "reason": "逾期自动冻结", "overdueBills": ["CB001"], "overdueAmount": 15000.00}
     * 6. 启用状态变更：
     * {"enabled": true}
     */
    @ApiModelProperty(value = "变更后状态，使用Map存储，持久化时转换为MySQL JSON类型")
    private Map<String, Object> afterState;

    /**
     * 变更原因（可空，最大500字符）
     */
    @ApiModelProperty(value = "变更原因（可空，最大500字符）")
    private String reason;

    /**
     * 操作人ID，系统操作为"SYSTEM"，框架自动填充
     */
    @ApiModelProperty(value = "操作人ID，系统操作为\"SYSTEM\"，框架自动填充")
    private String createBy;

    /**
     * 操作人姓名，框架自动填充
     */
    @ApiModelProperty(value = "操作人姓名，框架自动填充")
    private String createByName;

    /**
     * 操作时间，框架自动填充
     */
    @ApiModelProperty(value = "操作时间，框架自动填充")
    private LocalDateTime createTime;
}
