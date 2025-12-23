package com.bytz.modules.cms.payway.telegraphicTransfer.model;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.bytz.common.exception.BytzBootException;
import com.bytz.modules.cms.payway.telegraphicTransfer.constant.TelegraphicTransferConstant;
import com.bytz.modules.cms.payway.telegraphicTransfer.entity.TelegraphicTransfer;
import com.bytz.modules.cms.shared.util.BusinessCodeGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 电汇领域对象 - 封装电汇相关的业务逻辑和规则
 *
 * @author Bytz
 * @since 2025-12-12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TelegraphicTransferDomain {

    private TelegraphicTransfer telegraphicTransfer;

    /**
     * 验证是否可以进行支出操作
     *
     * @param expenseAmount 支出金额
     * @throws BytzBootException 如果验证失败
     */
    public void validateExpenseOperation(BigDecimal expenseAmount) {

        // 验证支出金额是否有效
        if (nullToZero(expenseAmount).compareTo(BigDecimal.ZERO) <= 0) {
            throw new BytzBootException("支出金额必须大于0");
        }

        // 验证电汇单状态是否正常
        if (!isStatusNormal()) {
            throw new BytzBootException("电汇单不允许该操作");
        }

        // 校验剩余金额是否满足扣减条件
        if (expenseAmount.compareTo(telegraphicTransfer.getRemainingAmount()) > 0) {
            throw new BytzBootException("电汇单剩余金额不足");
        }
    }

    /**
     * 执行支出操作 - 更新电汇相关字段
     *
     * @param expenseAmount 支出金额
     */
    public void performExpenseOperation(BigDecimal expenseAmount) {
        // 验证操作合法性
        validateExpenseOperation(expenseAmount);

        expenseAmount = nullToZero(expenseAmount);

        // 更新已用金额和剩余金额
        BigDecimal newUsedAmount = telegraphicTransfer.getUsedAmount().add(expenseAmount);
        BigDecimal newRemainingAmount = telegraphicTransfer.getRemainingAmount().subtract(expenseAmount);

        telegraphicTransfer.setUsedAmount(newUsedAmount);
        telegraphicTransfer.setRemainingAmount(newRemainingAmount);

        // 更新使用状态
        if (newRemainingAmount.compareTo(BigDecimal.ZERO) == 0) {
            telegraphicTransfer.setUsageStatus(TelegraphicTransferConstant.USAGE_STATUS.USE_UP);
        } else {
            telegraphicTransfer.setUsageStatus(TelegraphicTransferConstant.USAGE_STATUS.USED);
        }
    }

    /**
     * 获取当前电汇实体的快照（用于防止并发修改）
     *
     * @return 当前电汇实体
     */
    public TelegraphicTransfer getSnapshot() {
        return this.telegraphicTransfer;
    }


    /**
     * 检查电汇状态是否正常
     *
     * @return true如果状态正常
     */
    public boolean isStatusNormal() {
        return telegraphicTransfer != null
                && TelegraphicTransferConstant.ENABLED_STATUS.NORMAL.equals(telegraphicTransfer.getEnabledStatus());
    }


    /**
     * 生成支付记录id
     *
     * @return 支付记录id
     */
    public String generatePaymentRecordId() {
        return IdWorker.get32UUID();
    }


    // 生成支付记录编号
    public String generatePaymentRecordNumber() {
        return BusinessCodeGenerator.generateBillCode("TT");
    }


    /**
     * 确保金额不为null，若为null则返回0
     *
     * @param amount 金额
     * @return 非null金额
     */
    private BigDecimal nullToZero(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    /**
     * 验证是否可以进行撤销操作
     *
     * @param refundAmount 撤销金额
     * @throws BytzBootException 如果验证失败
     */
    public void validateRefundOperation(BigDecimal refundAmount) {
        // 验证撤销金额是否有效
        if (nullToZero(refundAmount).compareTo(BigDecimal.ZERO) <= 0) {
            throw new BytzBootException("撤销金额必须大于0");
        }

        // 验证电汇单是否被作废（允许已用尽的电汇单进行撤销）
        if (!isStatusNormal()) {
            throw new BytzBootException("电汇单已被作废，无法进行操作");
        }

        // 验证已用金额是否足够撤销
        if (refundAmount.compareTo(nullToZero(telegraphicTransfer.getUsedAmount())) > 0) {
            throw new BytzBootException("撤销金额不能超过已用金额");
        }
    }

    /**
     * 执行撤销操作 - 更新电汇相关字段
     *
     * @param refundAmount 撤销金额
     */
    public void performRefundOperation(BigDecimal refundAmount) {
        // 验证操作合法性
        validateRefundOperation(refundAmount);

        refundAmount = nullToZero(refundAmount);

        // 更新已用金额和剩余金额
        BigDecimal newUsedAmount = telegraphicTransfer.getUsedAmount().subtract(refundAmount);
        BigDecimal newRemainingAmount = telegraphicTransfer.getRemainingAmount().add(refundAmount);

        telegraphicTransfer.setUsedAmount(newUsedAmount);
        telegraphicTransfer.setRemainingAmount(newRemainingAmount);

        // 更新使用状态 撤销必定会让用尽变为未使用
        if (TelegraphicTransferConstant.USAGE_STATUS.USE_UP.equals(telegraphicTransfer.getUsageStatus())) {
            telegraphicTransfer.setUsageStatus(TelegraphicTransferConstant.USAGE_STATUS.USED);
        }
    }
}