package com.bytz.modules.cms.payway.telegraphicTransfer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bytz.modules.cms.payway.telegraphicTransfer.entity.TelegraphicTransfer;
import com.bytz.modules.cms.payway.telegraphicTransfer.model.TelegraphicTransferDomain;
import com.bytz.modules.cms.payway.telegraphicTransfer.model.TelegraphicTransferModelView;
import com.bytz.modules.cms.payway.telegraphicTransfer.model.TelegraphicTransferPayDetailRO;
import com.bytz.modules.cms.payway.telegraphicTransfer.model.response.TelegraphicTransferResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 莱宝CMS—电汇-电汇主表 服务类
 * </p>
 *
 * @author Bytz
 * @since 2025-09-19
 */
public interface ITelegraphicTransferService extends IService<TelegraphicTransfer> {

    /**
     * 导入电汇模板
     *
     * @param telegraphicTransfers 导入的数据
     * @return 返回导入的结果
     */
    String importExcel(List<TelegraphicTransfer> telegraphicTransfers);

    /**
     * 查询电汇列表
     *
     * @param telegraphicTransfer 查询条件
     * @param page                分页条件
     * @param parameterMap        参数
     * @return 返回查询结果
     */
    IPage<TelegraphicTransfer> queryPage(TelegraphicTransfer telegraphicTransfer, Page<TelegraphicTransfer> page, Map<String, String[]> parameterMap) throws Exception;

    /**
     * 查询电汇详情
     *
     * @param id 电汇id
     * @return 详情
     */
    TelegraphicTransferResponse detail(String id);

    /**
     * mall端分页查询电汇列表
     * 只有下订单指定过电汇的经销商才能查询到
     *
     * @return 电汇列表
     */
    IPage<TelegraphicTransfer> mallQueryPage(TelegraphicTransfer telegraphicTransfer, Page<TelegraphicTransfer> page, Map<String, String[]> parameterMap) throws Exception;

    /**
     * 作废按钮接口
     *
     * @param id 电汇id
     */
    void enableStatus(String id);

    /*  *//**
     * 使用电汇,扣款
     *
     * @param id               电汇id
     * @param useAmount        使用金额
     * @param orderPayDetailId 订单支付详情表id
     * @param resellerId       经销商id
     *//*
    void useTelegraphicTransfer(String id, BigDecimal useAmount, String orderPayDetailId, String resellerId);*/


    /**
     * 退回电汇,退款
     *
     * @param id               电汇id
     * @param rebackAmount     退回金额
     * @param orderPayDetailId 订单支付详情表id
     * @param resellerId       经销商id
     * @return 退回结果
     */
    TelegraphicTransferPayDetailRO rebackTelegraphicTransfer(String id, BigDecimal rebackAmount, String orderPayDetailId, String resellerId);

    /**
     * 支付界面选择电汇账单查询接口
     *
     * @param resellerId 经销商id
     * @return 电汇对账单
     */
    List<TelegraphicTransferModelView> selectTelegraphicTransferBill(String resellerId);


    /**
     * 验证金额
     *
     * @param id              电汇id
     * @param totalAmount     总金额
     * @param usedAmount      已使用金额
     * @param remainingAmount 剩余金额
     */
    void validateAmount(String id, BigDecimal totalAmount, BigDecimal usedAmount, BigDecimal remainingAmount);

    /**
     * 获取电汇领域对象
     *
     * @param id 电汇id
     * @return 电汇领域对象
     */
    TelegraphicTransferDomain getTelegraphicTransferDomain(String id);
}
