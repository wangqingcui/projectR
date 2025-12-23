package com.bytz.modules.cms.payway.telegraphicTransfer.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytz.common.aspect.annotation.RedisLock;
import com.bytz.common.exception.BytzBootException;
import com.bytz.common.system.query.MPJLambdaWrapperEx;
import com.bytz.common.system.query.MPJQueryGenerator;
import com.bytz.modules.cms.payway.constant.PayWayConstants;
import com.bytz.modules.cms.payway.model.PayWayCanUseContext;
import com.bytz.modules.cms.payway.service.PayWayCanUseService;
import com.bytz.modules.cms.payway.telegraphicTransfer.constant.TelegraphicTransferConstant;
import com.bytz.modules.cms.payway.telegraphicTransfer.entity.TelegraphicTransfer;
import com.bytz.modules.cms.payway.telegraphicTransfer.mapper.TelegraphicTransferMapper;
import com.bytz.modules.cms.payway.telegraphicTransfer.model.TelegraphicTransferDomain;
import com.bytz.modules.cms.payway.telegraphicTransfer.model.TelegraphicTransferModelView;
import com.bytz.modules.cms.payway.telegraphicTransfer.model.TelegraphicTransferPayDetailRO;
import com.bytz.modules.cms.payway.telegraphicTransfer.model.response.TelegraphicTransferPayDetailResponse;
import com.bytz.modules.cms.payway.telegraphicTransfer.model.response.TelegraphicTransferResponse;
import com.bytz.modules.cms.payway.telegraphicTransfer.service.ITelegraphicTransferPayDetailService;
import com.bytz.modules.cms.payway.telegraphicTransfer.service.ITelegraphicTransferService;
import com.bytz.modules.cms.reseller.util.ResellerSecurityUtils;
import com.bytz.modules.cms.shared.util.BusinessCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 莱宝CMS—电汇-电汇主表 服务实现类
 * </p>
 *
 * @author Bytz
 * @since 2025-09-19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelegraphicTransferServiceImpl extends ServiceImpl<TelegraphicTransferMapper, TelegraphicTransfer> implements ITelegraphicTransferService, PayWayCanUseService {

    private final ITelegraphicTransferPayDetailService telegraphicTransferPayDetailService;
    public static final DateTimeFormatter PATTERN = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Random RANDOM = new Random();

    /**
     * 导入电汇
     *
     * @param telegraphicTransfers 电汇
     * @return 提示信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importExcel(List<TelegraphicTransfer> telegraphicTransfers) {
        if (telegraphicTransfers.isEmpty()) {
            return "导入0条数据";
        }
        //整体的银行流水号,不仅限制导入的不能重复,还不能和现有非作废的银行流水号重复
        List<String> serialNumbers = telegraphicTransfers.stream()
                .map(TelegraphicTransfer::getSerialNumber)
                .collect(Collectors.toList());
        Set<String> serialNumbersSet = new HashSet<>(serialNumbers);
        if (serialNumbersSet.size() != serialNumbers.size()) {
            throw new BytzBootException("银行流水号不能重复");
        }
        List<TelegraphicTransfer> amountsList = telegraphicTransfers.stream()
                .filter(telegraphicTransfer -> telegraphicTransfer.getAmount().compareTo(BigDecimal.ZERO) < 0)
                .collect(Collectors.toList());
        if (!amountsList.isEmpty()) {
            throw new BytzBootException("银行流水号:" + amountsList.stream()
                    .map(TelegraphicTransfer::getSerialNumber)
                    .collect(Collectors.joining(",")) + "金额不能无效");
        }
        List<TelegraphicTransfer> transfers = this.lambdaQuery()
                .eq(TelegraphicTransfer::getEnabledStatus, TelegraphicTransferConstant.ENABLED_STATUS.NORMAL)
                .in(TelegraphicTransfer::getSerialNumber, serialNumbers)
                .list();
        if (!transfers.isEmpty()) {
            throw new BytzBootException("银行流水号" + transfers.stream()
                    .map(TelegraphicTransfer::getSerialNumber)
                    .collect(Collectors.joining(",")) + "不能重复");
        }
        telegraphicTransfers.forEach(telegraphicTransfer -> {
            telegraphicTransfer.setTelegraphicTransferNumber(createTransferNumber());
            telegraphicTransfer.setRemainingAmount(telegraphicTransfer.getAmount());
            telegraphicTransfer.setUsedAmount(BigDecimal.ZERO);
            telegraphicTransfer.setUsageStatus(TelegraphicTransferConstant.USAGE_STATUS.NOT_USED);
            telegraphicTransfer.setEnabledStatus(TelegraphicTransferConstant.ENABLED_STATUS.NORMAL);
        });
        this.saveOrUpdateBatch(telegraphicTransfers);
        return String.format("成功导入%d条数据", telegraphicTransfers.size());
    }

    /**
     * 查询电汇
     *
     * @param telegraphicTransfer 电汇
     * @param page                分页参数
     * @param parameterMap        参数
     * @return 电汇
     */
    @Override
    public IPage<TelegraphicTransfer> queryPage(TelegraphicTransfer telegraphicTransfer, Page<TelegraphicTransfer> page, Map<String, String[]> parameterMap) throws Exception {
        MPJLambdaWrapperEx<TelegraphicTransfer> wrapperEx
                = MPJQueryGenerator.initQueryWrapper(telegraphicTransfer, parameterMap, TelegraphicTransfer.class);
        wrapperEx.selectAll(TelegraphicTransfer.class);
        return this.page(page, wrapperEx);
    }

    /**
     * 电汇详情
     *
     * @param id 电汇id
     * @return 电汇详情
     */
    @Override
    public TelegraphicTransferResponse detail(String id) {
        TelegraphicTransfer telegraphicTransfer
                = Optional.ofNullable(this.getById(id)).orElseThrow(() -> new BytzBootException("请选择要查看的电汇"));
        TelegraphicTransferResponse transferResponse = new TelegraphicTransferResponse();
        transferResponse.setValue(telegraphicTransfer);
        List<TelegraphicTransferPayDetailResponse> payDetailResponses
                = telegraphicTransferPayDetailService.detailByTelegraphicTransferId(id);

        payDetailResponses.forEach(res -> {
            res.setCanRevoke(TelegraphicTransferConstant.OPERATE_TYPE.OTHER_DEDUCTION.equals(res.getOperateType())
                    && StringUtils.isBlank(res.getRefundId()));
        });
        transferResponse.setPayDetailList(payDetailResponses);
        return transferResponse;
    }

    /**
     * mall端查询电汇列表
     *
     * @param telegraphicTransfer 电汇
     * @param page                分页参数
     * @param parameterMap        参数
     * @return 电汇
     */
    @Override
    public IPage<TelegraphicTransfer> mallQueryPage(TelegraphicTransfer telegraphicTransfer, Page<TelegraphicTransfer> page, Map<String, String[]> parameterMap) throws Exception {
        MPJLambdaWrapperEx<TelegraphicTransfer> wrapperEx
                = MPJQueryGenerator.initQueryWrapper(telegraphicTransfer, parameterMap, TelegraphicTransfer.class);
        wrapperEx.selectAll(TelegraphicTransfer.class);
        wrapperEx.eq(TelegraphicTransfer::getEnabledStatus, TelegraphicTransferConstant.ENABLED_STATUS.NORMAL)
                .eq(TelegraphicTransfer::getResellerId, ResellerSecurityUtils.getUserId());
        return this.page(page, wrapperEx);
    }

    /**
     * 更新电汇启用状态
     *
     * @param id 电汇id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableStatus(String id) {
        TelegraphicTransfer transfer = Optional.ofNullable(
                this.lambdaQuery()
                        .eq(TelegraphicTransfer::getEnabledStatus, TelegraphicTransferConstant.ENABLED_STATUS.NORMAL)
                        .ne(TelegraphicTransfer::getUsageStatus, TelegraphicTransferConstant.USAGE_STATUS.USE_UP)
                        .eq(TelegraphicTransfer::getId, id)
                        .one()).orElseThrow(() -> new BytzBootException("此电汇不允许作废"));
        if (transfer.getAmount().compareTo(transfer.getRemainingAmount()) > 0) {
            throw new BytzBootException("已经使用的电汇不允许作废");
        }
        transfer.setEnabledStatus(TelegraphicTransferConstant.ENABLED_STATUS.CANCELED);
        this.updateById(transfer);
    }

    @Override
    public List<TelegraphicTransferModelView> selectTelegraphicTransferBill(String resellerId) {
        List<TelegraphicTransfer> transfers = this.list(new LambdaQueryWrapper<TelegraphicTransfer>()
                .eq(TelegraphicTransfer::getEnabledStatus, TelegraphicTransferConstant.ENABLED_STATUS.NORMAL)
                .and(wrapper ->
                        wrapper.isNull(TelegraphicTransfer::getResellerId))
                .or(wrapper ->
                        wrapper.eq(TelegraphicTransfer::getResellerId, resellerId)
                                .eq(TelegraphicTransfer::getUsageStatus, TelegraphicTransferConstant.USAGE_STATUS.USE_UP))
        );
        return BeanUtil.copyToList(transfers, TelegraphicTransferModelView.class);
    }

    /**
     * 创建电汇编号
     *
     * @return 电汇编号
     */
    public static String createTransferNumber() {
        StringBuilder prefix = new StringBuilder(4);

        for (int i = 0; i < 4; i++) {
            // 生成65-90之间的随机数（对应ASCII码中的A-Z）
            int asciiCode = RANDOM.nextInt(26) + 97;
            // 将ASCII码转换为字符
            prefix.append((char) asciiCode);
        }
        return prefix + LocalDate.now().format(PATTERN);
    }

    /*  *//**
     * 使用电汇
     *
     * @param id               电汇id
     * @param useAmount        使用金额
     * @param orderPayDetailId 订单支付详情表id
     * @param resellerId       经销商id
     *//*
    @Override
    @RedisLock(value = "transferLock", key = "{#id+':'+#orderPayDetailId}")
    @Transactional(rollbackFor = Exception.class)
    public void useTelegraphicTransfer(String id, BigDecimal useAmount, String orderPayDetailId, String resellerId) {
        log.info("开始使用电汇，电汇ID：{}，使用金额：{}，订单支付详情ID：{}，经销商ID：{}",
                id, useAmount, orderPayDetailId, resellerId);
        // 校验使用金额是否合法
        if (useAmount == null || useAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("使用电汇失败，金额不合法：{}", useAmount);
            throw new BytzBootException("使用金额必须大于0");
        }
        // 根据经销商id查询经销商详情
        Reseller reseller = resellerService.getById(resellerId);
        if (reseller == null) {
            log.error("使用电汇失败，经销商不存在：{}", resellerId);
            throw new BytzBootException("经销商不存在");
        }
        // 判断电汇是否可用
        TelegraphicTransfer transfer = Optional.ofNullable(this.lambdaQuery()
                .eq(TelegraphicTransfer::getId, id)
                .eq(TelegraphicTransfer::getEnabledStatus, TelegraphicTransferConstant.ENABLED_STATUS.NORMAL)
                .one()).orElseThrow(() -> {
            log.error("使用电汇失败，电汇票据不可使用或不存在：{}", id);
            return new BytzBootException("此电汇票据不可使用");
        });
        // 校验电汇状态是否允许使用
        String usageStatus = transfer.getUsageStatus();
        if (TelegraphicTransferConstant.USAGE_STATUS.USE_UP.equals(usageStatus)) {
            log.error("使用电汇失败，电汇已用完：{}", id);
            throw new BytzBootException("此电汇已用完，不可使用");
        }
        // 校验当前电汇是否已经被其他经销商使用
        String transferResellerId = transfer.getResellerId();
        if (StringUtils.isNotBlank(transferResellerId) && !StringUtils.equals(transferResellerId, resellerId)) {
            log.error("使用电汇失败，电汇已被其他经销商使用：{}，当前经销商：{}，电汇所属经销商：{}", id, resellerId, transferResellerId);
            throw new BytzBootException("此电汇已被其他经销商使用");
        }
        // 如果电汇未被使用过，设置当前经销商信息
        if (StringUtils.isBlank(transferResellerId)) {
            transfer.setResellerId(resellerId);
            transfer.setResellerName(reseller.getResellerName());
            log.info("电汇首次使用，设置所属经销商：{}，电汇ID：{}", resellerId, id);
        }
        // 检验金额
        BigDecimal totalAmount = transfer.getAmount();
        BigDecimal remainingAmount = transfer.getRemainingAmount();
        BigDecimal usedAmount = transfer.getUsedAmount();
        // 确保金额不为null
        validateAmount(transfer.getId(), totalAmount, usedAmount, remainingAmount);

        // 校验余额是否充足（使用compareTo避免精度问题）
        if (remainingAmount.compareTo(useAmount) < 0) {
            log.error("电汇余额不足，电汇ID：{}，剩余金额：{}，使用金额：{}", id, remainingAmount, useAmount);
            throw new BytzBootException("此电汇余额不足");
        }
        // 更新电汇状态和金额
        if (remainingAmount.compareTo(useAmount) == 0) {
            transfer.setUsageStatus(TelegraphicTransferConstant.USAGE_STATUS.USE_UP);
            transfer.setRemainingAmount(BigDecimal.ZERO);
            transfer.setUsedAmount(totalAmount);
            log.info("电汇已全部使用，电汇ID：{}", id);
        } else {
            transfer.setUsageStatus(TelegraphicTransferConstant.USAGE_STATUS.USED);
            transfer.setRemainingAmount(remainingAmount.subtract(useAmount).setScale(2, RoundingMode.HALF_UP));
            transfer.setUsedAmount(usedAmount.add(useAmount).setScale(2, RoundingMode.HALF_UP));
            log.info("电汇部分使用，电汇ID：{}，使用金额：{}，剩余金额：{}", id, useAmount, transfer.getRemainingAmount());
        }
        transfer.setUpdateTime(LocalDateTime.now());
        this.updateById(transfer);
        // 保存支付明细
        telegraphicTransferPayDetailService.saveTransferPayDetail(id, useAmount, orderPayDetailId, TelegraphicTransferConstant.OPERATE_TYPE.DEDUCTION);
        log.info("使用电汇成功，电汇ID：{}", id);
    }*/

    /**
     * 退回电汇
     *
     * @param id           电汇id
     * @param rebackAmount 退回金额
     * @param paymentId    退款单Id
     * @param resellerId   经销商id
     */
    @Override
    @RedisLock(value = "transferLock", key = "{#id+':'+#resellerId}")
    @Transactional(rollbackFor = Exception.class)
    public TelegraphicTransferPayDetailRO rebackTelegraphicTransfer(String id, BigDecimal rebackAmount, String paymentId, String resellerId) {
        log.info("开始退回电汇，电汇ID：{}，退回金额：{}，退款单Id：{}，经销商ID：{}", id, rebackAmount, paymentId, resellerId);
        // 校验退回金额是否合法
        if (rebackAmount == null || rebackAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("退回电汇失败，金额不合法：{}", rebackAmount);
            throw new BytzBootException("退回金额必须大于0");
        }
        // 查询电汇信息
        TelegraphicTransfer transfer = Optional.ofNullable(this.getById(id)).orElseThrow(() -> {
            log.error("退回电汇失败，电汇不存在：{}", id);
            return new BytzBootException("此电汇不存在");
        });
        // 校验电汇是否启用
        if (!TelegraphicTransferConstant.ENABLED_STATUS.NORMAL.equals(transfer.getEnabledStatus())) {
            log.error("退回电汇失败，电汇未启用：{}，状态：{}", id, transfer.getEnabledStatus());
            throw new BytzBootException("此电汇未启用，无法退款");
        }
        // 校验电汇所属经销商
        String transferResellerId = transfer.getResellerId();
        if (!StringUtils.equals(transferResellerId, resellerId)) {
            log.error("退回电汇失败，电汇不属于当前经销商：{}，当前经销商：{}，电汇所属经销商：{}", id, resellerId, transferResellerId);
            throw new BytzBootException("退款失败，原因：此电汇票据不属于该经销商");
        }
        // 校验电汇状态是否允许退款
        String usageStatus = transfer.getUsageStatus();
        if (TelegraphicTransferConstant.USAGE_STATUS.NOT_USED.equals(usageStatus)) {
            log.error("退回电汇失败，电汇未使用：{}", id);
            throw new BytzBootException("此电汇未使用，无法退款");
        }
        // 金额处理
        BigDecimal totalAmount = transfer.getAmount();
        BigDecimal usedAmount = transfer.getUsedAmount();
        BigDecimal remainingAmount = transfer.getRemainingAmount();
        this.validateAmount(transfer.getId(), totalAmount, usedAmount, remainingAmount);
        // 校验退回金额是否合理
        if (usedAmount.compareTo(rebackAmount) < 0) {
            log.error("退回金额不合理，电汇ID：{}，已用金额：{}，退回金额：{}", id, usedAmount, rebackAmount);
            throw new BytzBootException(String.format("退回金额'%s'大于已使用金额'%s'，退款失败", rebackAmount, usedAmount));
        }
        // 更新电汇状态和金额
        if (usedAmount.compareTo(rebackAmount) == 0) {
            transfer.setUsageStatus(TelegraphicTransferConstant.USAGE_STATUS.NOT_USED);
            transfer.setUsedAmount(BigDecimal.ZERO);
            transfer.setRemainingAmount(totalAmount);
            log.info("电汇全额退回，电汇ID：{}", id);
        } else {
            transfer.setRemainingAmount(remainingAmount.add(rebackAmount).setScale(2, RoundingMode.HALF_UP));
            transfer.setUsedAmount(usedAmount.subtract(rebackAmount).setScale(2, RoundingMode.HALF_UP));
            // 如果剩余金额等于总金额，设置为未使用状态
            if (transfer.getRemainingAmount().compareTo(totalAmount) == 0) {
                transfer.setUsageStatus(TelegraphicTransferConstant.USAGE_STATUS.NOT_USED);
            } else {
                transfer.setUsageStatus(TelegraphicTransferConstant.USAGE_STATUS.USED);
            }
            log.info("电汇部分退回，电汇ID：{}，退回金额：{}，剩余金额：{}", id, rebackAmount, transfer.getRemainingAmount());
        }
        transfer.setUpdateTime(LocalDateTime.now());
        this.updateById(transfer);
        // 保存退款明细
        String tranCode = BusinessCodeGenerator.generateBillCode("TTR");
        // 保存退款明细并返回保存信息
        TelegraphicTransferPayDetailRO telegraphicTransferPayDetailRo
                = telegraphicTransferPayDetailService.saveTransferPayDetail(paymentId, IdWorker.get32UUID(), id, rebackAmount, TelegraphicTransferConstant.OPERATE_TYPE.REFUND, tranCode);
        log.info("退回电汇成功，电汇ID：{}", id);
        return telegraphicTransferPayDetailRo;
    }

    /**
     * 校验金额
     *
     * @param id              电汇id
     * @param totalAmount     总金额
     * @param usedAmount      已用金额
     * @param remainingAmount 剩余金额
     */
    @Override
    public void validateAmount(String id, BigDecimal totalAmount, BigDecimal usedAmount, BigDecimal remainingAmount) {
        // 确保金额不为null
        totalAmount = totalAmount == null ? BigDecimal.ZERO : totalAmount;
        remainingAmount = remainingAmount == null ? BigDecimal.ZERO : remainingAmount;
        usedAmount = usedAmount == null ? BigDecimal.ZERO : usedAmount;
        // 校验金额一致性
        if (totalAmount.compareTo(remainingAmount.add(usedAmount)) != 0) {
            log.error("电汇金额不一致，电汇ID：{}，总金额：{}，剩余金额：{}，已用金额：{}", id, totalAmount, remainingAmount, usedAmount);
            throw new BytzBootException("电汇金额数据异常");
        }
    }

    @Override
    public void getAvailablePayWays(PayWayCanUseContext payWayCanUseContext) {
        if (payWayCanUseContext.getIsAdmin()) {
            payWayCanUseContext.getAvailablePayWays().add(PayWayConstants.TELEGRAPHIC_TRANSFER);
        }
    }

    @Override
    public TelegraphicTransferDomain getTelegraphicTransferDomain(String id) {
        TelegraphicTransfer telegraphicTransfer = this.getById(id);
        return new TelegraphicTransferDomain(telegraphicTransfer);
    }
}
