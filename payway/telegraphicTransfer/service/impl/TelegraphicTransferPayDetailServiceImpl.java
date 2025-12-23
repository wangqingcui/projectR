package com.bytz.modules.cms.payway.telegraphicTransfer.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytz.common.system.query.MPJLambdaWrapperEx;
import com.bytz.modules.cms.order.entity.Order;
import com.bytz.modules.cms.payment.infrastructure.entity.PaymentPO;
import com.bytz.modules.cms.payway.telegraphicTransfer.entity.TelegraphicTransferPayDetail;
import com.bytz.modules.cms.payway.telegraphicTransfer.mapper.TelegraphicTransferPayDetailMapper;
import com.bytz.modules.cms.payway.telegraphicTransfer.model.TelegraphicTransferPayDetailRO;
import com.bytz.modules.cms.payway.telegraphicTransfer.model.response.TelegraphicTransferPayDetailResponse;
import com.bytz.modules.cms.payway.telegraphicTransfer.service.ITelegraphicTransferPayDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 莱宝CMS—电汇-电汇详细表 服务实现类
 * </p>
 *
 * @author Bytz
 * @since 2025-09-19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelegraphicTransferPayDetailServiceImpl extends ServiceImpl<TelegraphicTransferPayDetailMapper, TelegraphicTransferPayDetail> implements ITelegraphicTransferPayDetailService {

    /**
     * 通过电汇主表Id查询电汇详情列表
     *
     * @param id 电汇主表Id
     */
    @Override
    public List<TelegraphicTransferPayDetailResponse> detailByTelegraphicTransferId(String id) {
        MPJLambdaWrapperEx<TelegraphicTransferPayDetail> wrapperEx = new MPJLambdaWrapperEx<>();

        wrapperEx.select(TelegraphicTransferPayDetail::getId, TelegraphicTransferPayDetail::getOperateType,
                        TelegraphicTransferPayDetail::getRefundId,
                        TelegraphicTransferPayDetail::getOperateAmount, TelegraphicTransferPayDetail::getRemark)
                .selectAs(Order::getId, TelegraphicTransferPayDetailResponse::getOrderId)
                .selectAs(Order::getContractNumber, TelegraphicTransferPayDetailResponse::getContractNumber)
                .selectAs(Order::getOrderType, TelegraphicTransferPayDetailResponse::getOrderType)
                .selectAs(Order::getStatus, TelegraphicTransferPayDetailResponse::getStatus)

                .leftJoin(PaymentPO.class, left -> left
                        .eq(PaymentPO::getId, TelegraphicTransferPayDetail::getPaymentId)
                        .eq(PaymentPO::getDelFlag, 0)
                )
                .leftJoin(Order.class, left -> left
                        .eq(Order::getId, PaymentPO::getOrderId)
                        .eq(Order::getDelFlag, 0)
                )
                .eq(TelegraphicTransferPayDetail::getTelegraphicTransferId, id)
                .orderByDesc(TelegraphicTransferPayDetail::getCreateTime)
                .orderByDesc(TelegraphicTransferPayDetail::getId)
        ;
        return this.baseMapper.selectJoinList(TelegraphicTransferPayDetailResponse.class, wrapperEx);
    }


    /**
     * 保存电汇支付详情
     *
     * @param paymentId     支付单Id
     * @param detailId      电汇详情id
     * @param transferId    电汇id
     * @param operateAmount 操作金额
     * @param operateType   操作类型
     * @return 电汇支付详情
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TelegraphicTransferPayDetailRO saveTransferPayDetail(String paymentId, String detailId, String transferId, BigDecimal operateAmount, String operateType, String transactionCode) {
        this.saveTransferPayDetail(paymentId, detailId, transferId, operateAmount, operateType, transactionCode, null);
        // 返回结果
        return TelegraphicTransferPayDetailRO.builder()
                .id(detailId)
                .telegraphicTransferId(transferId)
                .transactionCode(transactionCode)
                .paymentId(paymentId)
                .operateType(operateType)
                .operateAmount(operateAmount.setScale(2, RoundingMode.HALF_UP))
                .remark("")
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTransferPayDetail(String paymentId, String detailId, String transferId, BigDecimal operateAmount, String operateType, String transactionCode, String remark) {
        TelegraphicTransferPayDetail payDetail = new TelegraphicTransferPayDetail();
        payDetail.setId(detailId);
        payDetail.setTelegraphicTransferId(transferId);
        payDetail.setTransactionCode(transactionCode);
        payDetail.setPaymentId(paymentId);
        payDetail.setOperateType(operateType);
        payDetail.setOperateAmount(operateAmount.setScale(2, RoundingMode.HALF_UP));
        payDetail.setCreateTime(LocalDateTime.now());
        payDetail.setRemark(remark);
        this.save(payDetail);
        log.info("保存电汇操作明细成功，ID：{}，操作类型：{}", payDetail.getId(), operateType);
    }
}
