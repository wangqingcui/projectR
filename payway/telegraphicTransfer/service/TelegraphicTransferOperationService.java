package com.bytz.modules.cms.payway.telegraphicTransfer.service;

import com.bytz.common.exception.BytzBootException;
import com.bytz.modules.cms.payway.telegraphicTransfer.constant.TelegraphicTransferConstant;
import com.bytz.modules.cms.payway.telegraphicTransfer.entity.TelegraphicTransfer;
import com.bytz.modules.cms.payway.telegraphicTransfer.entity.TelegraphicTransferPayDetail;
import com.bytz.modules.cms.payway.telegraphicTransfer.model.TelegraphicTransferDomain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * <p>
 * 电汇操作服务类 - 提供电汇支出记录的增加和撤销功能
 * </p>
 *
 * @author Bytz
 * @since 2025-12-12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelegraphicTransferOperationService {

    private final ITelegraphicTransferService telegraphicTransferService;
    private final ITelegraphicTransferPayDetailService telegraphicTransferPayDetailService;


    @Transactional(rollbackFor = Exception.class)
    public boolean addExpenseRecord(String telegraphicTransferId, BigDecimal expenseAmount, String expenseDescription) {
        log.info("增加支出记录 - 电汇ID: {}, 支出金额: {}, 描述: {}", telegraphicTransferId, expenseAmount, expenseDescription);

        // 1. 获取电汇领域对象
        TelegraphicTransferDomain telegraphicTransferDomain = telegraphicTransferService.getTelegraphicTransferDomain(telegraphicTransferId);

        // 2. 使用领域对象进行业务验证和操作
        telegraphicTransferDomain.performExpenseOperation(expenseAmount);

        // 3. 生成支出记录ID并保存交易记录
        String recordId = telegraphicTransferDomain.generatePaymentRecordId();
        String recordNumber = telegraphicTransferDomain.generatePaymentRecordNumber();

        telegraphicTransferPayDetailService.saveTransferPayDetail(
                null,// 其他费用没有支付单
                recordId,
                telegraphicTransferId,
                expenseAmount,
                TelegraphicTransferConstant.OPERATE_TYPE.OTHER_DEDUCTION, // 支出操作类型
                recordNumber,
                expenseDescription
        );

        // 4. 调用validateAmount方法校验金额（领域对象已经更新了金额，现在需要验证）
        TelegraphicTransfer updatedTelegraphicTransfer = telegraphicTransferDomain.getSnapshot();
        telegraphicTransferService.validateAmount(
                telegraphicTransferId,
                updatedTelegraphicTransfer.getAmount(),
                updatedTelegraphicTransfer.getUsedAmount(),
                updatedTelegraphicTransfer.getRemainingAmount()
        );

        // 5. 更新电汇实体到数据库
        this.updateTelegraphicTransfer(telegraphicTransferDomain.getSnapshot());
        log.info("增加支出记录成功 - 电汇ID: {}, 支出金额: {}", telegraphicTransferId, expenseAmount);
        return true;
    }

    /**
     * 撤销支出记录
     *
     * @param expenseRecordId 支出记录ID
     * @param revokeReason    撤销原因
     * @return 返回操作结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean revokeExpenseRecord(String expenseRecordId, String revokeReason) {
        log.info("撤销支出记录 - 支出记录ID: {}, 撤销原因: {}", expenseRecordId, revokeReason);

        // 1. 通过getById查询支出记录详情
        TelegraphicTransferPayDetail expenseRecord = telegraphicTransferPayDetailService.getById(expenseRecordId);
        if (expenseRecord == null) {
            throw new BytzBootException("支出记录不存在");
        }

        // 2. 校验是否为其他费用记录且未撤销过
        if (!TelegraphicTransferConstant.OPERATE_TYPE.OTHER_DEDUCTION.equals(expenseRecord.getOperateType())) {
            throw new BytzBootException("只能撤销其他费用类型的支出记录");
        }

        // 检查是否已经撤销过（有refundId表示已撤销）
        if (StringUtils.isNotBlank(expenseRecord.getRefundId())) {
            throw new BytzBootException("该支出记录已经撤销过，不能重复撤销");
        }

        // 3. 获取电汇领域对象并校验状态（允许已用尽的电汇单进行撤销）
        TelegraphicTransferDomain telegraphicTransferDomain = telegraphicTransferService.getTelegraphicTransferDomain(expenseRecord.getTelegraphicTransferId());

        // 4. 执行撤销操作（更新电汇金额和状态）
        telegraphicTransferDomain.performRefundOperation(expenseRecord.getOperateAmount());

        // 5. 创建撤销记录
        String recordId = telegraphicTransferDomain.generatePaymentRecordId();
        String recordNumber = telegraphicTransferDomain.generatePaymentRecordNumber();
        String refundRemark = String.format("%s (撤销记录编号: %s)", StringUtils.isBlank(revokeReason) ? "" : revokeReason, expenseRecord.getTransactionCode());

        telegraphicTransferPayDetailService.saveTransferPayDetail(
                null, // 其他费用没有支付单
                recordId,
                expenseRecord.getTelegraphicTransferId(),
                expenseRecord.getOperateAmount(), // 撤销金额等于原支出金额
                TelegraphicTransferConstant.OPERATE_TYPE.OTHER_REFUND, // 其他费用退款
                recordNumber,
                refundRemark
        );

        // 6. 更新原记录的refundId
        telegraphicTransferPayDetailService.lambdaUpdate()
                .set(TelegraphicTransferPayDetail::getRefundId, expenseRecord.getId())
                .eq(TelegraphicTransferPayDetail::getId, expenseRecordId)
                .update();


        // 7. 更新电汇实体到数据库
        this.updateTelegraphicTransfer(telegraphicTransferDomain.getSnapshot());

        log.info("撤销支出记录成功 - 支出记录ID: {}, 撤销记录ID: {}", expenseRecordId, recordId);
        return true;
    }

    private void updateTelegraphicTransfer(TelegraphicTransfer telegraphicTransfer) {
        // 更新电汇实体到数据库
        telegraphicTransferService.lambdaUpdate()
                .set(TelegraphicTransfer::getUsedAmount, telegraphicTransfer.getUsedAmount())
                .set(TelegraphicTransfer::getRemainingAmount, telegraphicTransfer.getRemainingAmount())
                .set(TelegraphicTransfer::getUsageStatus, telegraphicTransfer.getUsageStatus())
                .eq(TelegraphicTransfer::getId, telegraphicTransfer.getId())
                .update();
    }
}