package com.bytz.modules.cms.payway.telegraphicTransfer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bytz.modules.cms.payway.telegraphicTransfer.entity.TelegraphicTransferPayDetail;
import com.bytz.modules.cms.payway.telegraphicTransfer.model.TelegraphicTransferPayDetailRO;
import com.bytz.modules.cms.payway.telegraphicTransfer.model.response.TelegraphicTransferPayDetailResponse;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 莱宝CMS—电汇-电汇详细表 服务类
 * </p>
 *
 * @author Bytz
 * @since 2025-09-19
 */
public interface ITelegraphicTransferPayDetailService extends IService<TelegraphicTransferPayDetail> {

    /**
     * 通过电话主表Id查询电汇详情列表
     *
     * @param id 电汇主表Id
     */
    List<TelegraphicTransferPayDetailResponse> detailByTelegraphicTransferId(String id);


    /**
     * 保存电汇详情
     *
     * @param paymentId     支付单Id
     * @param detailId      自动生成的电汇详细Id
     * @param transferId    电汇主表Id
     * @param operateAmount 操作金额
     * @param operateType   操作类型
     * @return 电汇详情
     */
    TelegraphicTransferPayDetailRO saveTransferPayDetail(String paymentId, String detailId, String transferId, BigDecimal operateAmount, String operateType, String transactionCode);

    /**
     * 保存电汇详情
     *
     * @param paymentId     支付单Id
     * @param detailId      自动生成的电汇详细Id
     * @param transferId    电汇主表Id
     * @param operateAmount 操作金额
     * @param operateType   操作类型
     */
    void saveTransferPayDetail(String paymentId, String detailId, String transferId, BigDecimal operateAmount, String operateType, String transactionCode, String remark);
}
