package com.bytz.modules.cms.payway.credit.application.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytz.common.constant.BooleanConstant;
import com.bytz.common.system.query.MPJLambdaWrapperEx;
import com.bytz.modules.cms.payment.infrastructure.entity.PaymentPO;
import com.bytz.modules.cms.payway.credit.application.assembler.CreditAssembler;
import com.bytz.modules.cms.payway.credit.application.model.CreditBillListVO;
import com.bytz.modules.cms.payway.credit.domain.enums.RepaymentStatus;
import com.bytz.modules.cms.payway.credit.infrastructure.entity.CreditBillPO;
import com.bytz.modules.cms.payway.credit.infrastructure.mapper.CreditBillMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 信用账单查询服务
 * Credit Bill Query Service
 *
 * <p>处理信用账单查询操作（CQRS中的Query端）</p>
 * <p>职责：
 * <ul>
 *   <li>信用账单查询</li>
 *   <li>返回VO对象</li>
 *   <li>继承ServiceImpl以便利用MyBatis-Plus的查询能力</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreditBillQueryService extends ServiceImpl<CreditBillMapper, CreditBillPO> {

    private final CreditAssembler creditAssembler;

    /**
     * 根据钱包ID分页查询交易记录
     *
     * @param page         分页参数
     * @param queryWrapper 查询条件
     * @return 分页交易记录
     */
    public IPage<CreditBillListVO> queryTransactionPage(Page<CreditBillPO> page, MPJLambdaWrapperEx<CreditBillPO> queryWrapper) {

        queryWrapper
                .selectAll(CreditBillPO.class)
                .selectAs(PaymentPO::getCode, CreditBillListVO::getPaymentCode)
                .leftJoin(PaymentPO.class, left -> left.eq(PaymentPO::getId, CreditBillPO::getPaymentId).eq(PaymentPO::getDelFlag, BooleanConstant.INT_FALSE))
                .orderByDesc(CreditBillPO::getCreateTime);

        IPage<CreditBillListVO> voiPage = baseMapper.selectJoinPage(page, CreditBillListVO.class, queryWrapper);

        voiPage.getRecords().forEach(vo -> {
            vo.setCanCreateRepayment(StringUtils.isBlank(vo.getRepaymentPaymentId()));
        });
        return voiPage;
    }

    public BigDecimal debt(String WalletId) {

        BigDecimal decimal = this.lambdaQuery()
                .select(CreditBillPO::getAmount)
                .eq(CreditBillPO::getCreditWalletId, WalletId)
                .ne(CreditBillPO::getRepaymentStatus, RepaymentStatus.REPAID)
                .list()
                .stream()
                .map(CreditBillPO::getAmount)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
        return decimal;
    }
}