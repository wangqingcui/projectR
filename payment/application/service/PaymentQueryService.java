package com.bytz.modules.cms.payment.application.service;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytz.common.constant.CommonConstant;
import com.bytz.common.system.query.MPJLambdaWrapperEx;
import com.bytz.common.util.SecurityUtils;
import com.bytz.modules.cms.order.constant.AdminAuthConstants;
import com.bytz.modules.cms.order.entity.Order;
import com.bytz.modules.cms.payment.application.assembler.PaymentAssembler;
import com.bytz.modules.cms.payment.application.model.PaymentDetailVO;
import com.bytz.modules.cms.payment.application.model.PaymentListVo;
import com.bytz.modules.cms.payment.application.model.PaymentTransactionVO;
import com.bytz.modules.cms.payment.application.model.PaymentVO;
import com.bytz.modules.cms.payment.domain.enums.PaymentStatus;
import com.bytz.modules.cms.payment.domain.enums.PaymentType;
import com.bytz.modules.cms.payment.domain.enums.RelatedBusinessType;
import com.bytz.modules.cms.payment.domain.model.PaymentTransactionEntity;
import com.bytz.modules.cms.payment.domain.repository.IPaymentTransactionRepository;
import com.bytz.modules.cms.payment.infrastructure.entity.PaymentPO;
import com.bytz.modules.cms.payment.infrastructure.mapper.PaymentMapper;
import com.bytz.modules.cms.payment.shared.exception.PaymentErrorCode;
import com.bytz.modules.cms.payment.shared.exception.PaymentException;
import com.bytz.modules.cms.user.constants.CmsSysUserConstants;
import com.bytz.modules.cms.user.service.ICmsSysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 支付查询服务
 * Payment Query Service
 *
 * <p>处理所有读操作（CQRS中的Query端）</p>
 * <p>直接使用MyBatis-Plus进行查询，绕过领域层</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentQueryService extends ServiceImpl<PaymentMapper, PaymentPO> {

    private final PaymentAssembler paymentAssembler;
    private final IPaymentTransactionRepository transactionRepository;
    private final ICmsSysUserService sysUserService;

    /**
     * 根据ID查询支付单
     *
     * @param paymentId 支付单ID
     * @return 支付单VO
     */
    public PaymentVO getById(String paymentId) {
        log.debug("根据ID查询支付单，paymentId: {}", paymentId);

        PaymentPO po = getBaseMapper().selectById(paymentId);
        if (po == null) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND);
        }

        return paymentAssembler.toVO(po);
    }

    /**
     * 根据支付单号查询支付单
     *
     * @param code 支付单号
     * @return 支付单VO
     */
    public PaymentVO getByCode(String code) {
        log.debug("根据支付单号查询支付单，code: {}", code);

        LambdaQueryWrapper<PaymentPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentPO::getCode, code);

        PaymentPO po = getBaseMapper().selectOne(wrapper);
        if (po == null) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND);
        }

        return paymentAssembler.toVO(po);
    }

    /**
     * 根据订单ID查询支付单列表
     *
     * @param orderId 订单ID
     * @return 支付单VO列表
     */
    public List<PaymentVO> getByOrderId(String orderId) {
        log.debug("根据订单ID查询支付单列表，orderId: {}", orderId);

        LambdaQueryWrapper<PaymentPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentPO::getOrderId, orderId);

        List<PaymentPO> pos = getBaseMapper().selectList(wrapper);

        return pos.stream()
                .map(po -> paymentAssembler.toVO(po))
                .collect(Collectors.toList());
    }

    /**
     * 根据经销商ID查询支付单列表
     *
     * @param resellerId 经销商ID
     * @return 支付单VO列表
     */
    public List<PaymentVO> getByResellerId(String resellerId) {
        log.debug("根据经销商ID查询支付单列表，resellerId: {}", resellerId);

        LambdaQueryWrapper<PaymentPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentPO::getResellerId, resellerId);

        List<PaymentPO> pos = getBaseMapper().selectList(wrapper);

        return pos.stream()
                .map(po -> paymentAssembler.toVO(po))
                .collect(Collectors.toList());
    }

    /**
     * 根据支付单ID查询流水列表
     *
     * @param paymentId 支付单ID
     * @return 支付流水VO列表
     */
    public List<PaymentTransactionVO> getTransactionsByPaymentId(String paymentId) {
        log.debug("根据支付单ID查询流水列表，paymentId: {}", paymentId);

        List<PaymentTransactionEntity> transactions = transactionRepository.findByPaymentId(paymentId);

        return transactions.stream()
                .map(paymentAssembler::toVO)
                .collect(Collectors.toList());
    }

    /**
     * 分页查询支付单列表
     *
     * @param page         分页参数
     * @param queryWrapper 查询条件
     * @return 分页支付单列表
     */
    public IPage<PaymentListVo> queryPaymentPage(Page<PaymentPO> page, MPJLambdaWrapperEx<PaymentPO> queryWrapper, Boolean isAdmin) {
        log.debug("分页查询支付单列表，page: {}, queryWrapper: {}", page, queryWrapper);

        buildWrapper(queryWrapper);

        buildAdminViewWrapper(queryWrapper, isAdmin);

        IPage<PaymentListVo> listVoPage = getBaseMapper().selectJoinPage(page, PaymentListVo.class, queryWrapper);

        // 计算待支付金额（支付金额 - 已支付金额）
        listVoPage.getRecords().forEach(this::convertView);

        return listVoPage;
    }


    private void convertView(PaymentListVo record) {
        // 类型为提货单并且业务id不为空
        record.setCanDetailPick(RelatedBusinessType.DELIVERY_ORDER == record.getRelatedBusinessType() && record.getRelatedBusinessId() != null);
        // 允许支付 未支付以及部分支付
        record.setCanPay(record.getPaymentType().isPayment() && record.getPaymentStatus().isPayable());

        if (record.getPaymentType().isPayment()) {
            record.setPendingAmount(Optional.ofNullable(record.getPaymentAmount()).orElse(BigDecimal.ZERO)
                    .subtract(Optional.ofNullable(record.getPaidAmount()).orElse(BigDecimal.ZERO)));
        }
    }

    /**
     * 查询支付单详情，包含支付单信息和流水记录
     *
     * @param paymentId 支付单ID
     * @return 支付单详情VO
     */
    public PaymentDetailVO queryDetail(String paymentId) {
        log.debug("查询支付单详情，paymentId: {}", paymentId);

        // 获取支付单信息
        PaymentVO payment = getById(paymentId);

        // 获取流水记录
        List<PaymentTransactionVO> transactions = getTransactionsByPaymentId(paymentId);

        // 组合成详情VO
        return PaymentDetailVO.builder()
                .payment(payment)
                .transactions(transactions)
                .build();
    }

    public List<PaymentListVo> queryPayList(String resellerCompanyId) {
        MPJLambdaWrapperEx<PaymentPO> queryWrapper = new MPJLambdaWrapperEx<>();
        queryWrapper.eq(PaymentPO::getResellerId, resellerCompanyId);
        buildWrapper(queryWrapper);
        queryWrapper
                .ne(PaymentPO::getPaymentType, PaymentType.REFUND)
                .in(PaymentPO::getPaymentStatus, PaymentStatus.PAYABLE_STATUSES);

        List<PaymentListVo> paymentListVos = getBaseMapper().selectJoinList(PaymentListVo.class, queryWrapper);
        paymentListVos.forEach(this::convertView);
        return paymentListVos;
    }

    private void buildWrapper(MPJLambdaWrapperEx<PaymentPO> queryWrapper) {
        queryWrapper
                .selectAll(PaymentPO.class)
                .selectAs(Order::getContractNumber, PaymentListVo::getContractNumber)
                .selectAs(Order::getDemanderNumber, PaymentListVo::getDemanderNumber)
                .selectAs(Order::getDemanderName, PaymentListVo::getDemanderName)
                .selectAs(Order::getDemanderAddress, PaymentListVo::getDemanderAddress)
                .innerJoin(Order.class, inner -> inner.eq(Order::getId, PaymentPO::getOrderId).eq(Order::getDelFlag, CommonConstant.DEL_FLAG_0))
                .orderByDesc(PaymentPO::getCreateTime)
        ;
    }

    private void buildAdminViewWrapper(MPJLambdaWrapperEx<PaymentPO> queryWrapper, Boolean isAdmin) {
        if (isAdmin)
            // 销售只能看到自己的订单，大区经理只能看到自己区域销售的订单
            if (!SecurityUtils.hasAuthority(AdminAuthConstants.Order.VIEW_ALL)) {
                boolean isRegionManager = SecurityUtils.hasRole(CmsSysUserConstants.RoleCode.REGION_SALES_MANAGER);
                boolean isOrderManager = SecurityUtils.hasRole(CmsSysUserConstants.RoleCode.ORDER_MANAGER);
                queryWrapper.and(wrapper -> {
                    wrapper.eq(Order::getSalesId, SecurityUtils.getLoginUser().getId());
                    if (isRegionManager) {
                        List<String> salesIds = sysUserService.getSalesIdsByRegionManager(SecurityUtils.getLoginUser().getId());
                        if (CollectionUtil.isNotEmpty(salesIds)) {
                            wrapper.or().in(Order::getSalesId, salesIds);
                        }
                    }
                    if (isOrderManager) {
                        Set<String> salesIds = sysUserService.getSalesIdsByOM(SecurityUtils.getLoginUser().getId());
                        if (CollectionUtil.isNotEmpty(salesIds)) {
                            wrapper.or().in(Order::getSalesId, salesIds);
                        }
                    }
                });
            }
    }
}