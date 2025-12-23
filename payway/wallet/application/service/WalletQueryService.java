package com.bytz.modules.cms.payway.wallet.application.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytz.common.constant.BooleanConstant;
import com.bytz.common.system.query.MPJLambdaWrapperEx;
import com.bytz.modules.cms.order.entity.Order;
import com.bytz.modules.cms.payment.infrastructure.entity.PaymentPO;
import com.bytz.modules.cms.payment.infrastructure.entity.PaymentTransactionPO;
import com.bytz.modules.cms.payway.constant.PayWayConstants;
import com.bytz.modules.cms.payway.model.PayWayCanUseContext;
import com.bytz.modules.cms.payway.service.PayWayCanUseService;
import com.bytz.modules.cms.payway.wallet.application.assembler.WalletAssembler;
import com.bytz.modules.cms.payway.wallet.application.model.WalletListVO;
import com.bytz.modules.cms.payway.wallet.application.model.WalletTransactionListVO;
import com.bytz.modules.cms.payway.wallet.application.model.WalletVO;
import com.bytz.modules.cms.payway.wallet.domain.enums.WalletStatus;
import com.bytz.modules.cms.payway.wallet.infrastructure.entity.WalletEntity;
import com.bytz.modules.cms.payway.wallet.infrastructure.entity.WalletTransactionEntity;
import com.bytz.modules.cms.payway.wallet.infrastructure.mapper.WalletMapper;
import com.bytz.modules.cms.payway.wallet.infrastructure.mapper.WalletTransactionMapper;
import com.bytz.modules.cms.reseller.entity.Reseller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 钱包查询服务
 * Wallet Query Service
 * <p>
 * 实现CQRS模式，查询服务直通数据库层，继承MyBatis-Plus的ServiceImpl
 * 使用lambda链式查询，直接将Entity转换为VO
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletQueryService extends ServiceImpl<WalletMapper, WalletEntity> implements PayWayCanUseService {

    private final WalletTransactionMapper walletTransactionMapper;
    private final WalletMapper walletMapper;
    private final WalletAssembler walletAssembler;

    /**
     * 根据钱包ID查询钱包信息
     */
    public WalletVO queryWalletById(String walletId) {
        log.info("查询钱包信息：钱包ID={}", walletId);

        // 使用lambda链式查询
        WalletEntity entity = lambdaQuery()
                .eq(WalletEntity::getId, walletId)
                .one();

        if (entity == null) {
            return null;
        }

        // 直接转换Entity为VO
        return walletAssembler.entityToVO(entity);
    }

    /**
     * 根据经销商ID查询钱包信息
     */
    public WalletVO queryWalletByResellerId(String resellerId) {
        log.info("查询钱包信息：经销商ID={}", resellerId);

        // 使用lambda链式查询
        WalletEntity entity = lambdaQuery()
                .eq(WalletEntity::getResellerId, resellerId)
                .one();

        if (entity == null) {
            return null;
        }

        // 直接转换Entity为VO
        return walletAssembler.entityToVO(entity);
    }

    /**
     * 判断经销商钱包是否启用
     *
     * @param resellerId 经销商ID
     * @return 钱包是否启用，如果钱包不存在返回false
     */
    public boolean isWalletEnabled(String resellerId) {
        log.info("判断经销商钱包是否启用：经销商ID={}", resellerId);

        WalletVO walletVO = queryWalletByResellerId(resellerId);
        return walletVO != null && WalletStatus.ENABLED.equals(walletVO.getStatus());
    }

    /**
     * 查询钱包交易记录

     public List<WalletTransactionVO> queryTransactionsByWalletId(
     String walletId, WalletTransactionType transactionType, WalletTransactionStatus transactionStatus,
     LocalDateTime startDate, LocalDateTime endDate) {
     log.info("查询钱包交易记录：钱包ID={}", walletId);

     // 使用LambdaQueryWrapper构建动态条件
     LambdaQueryWrapper<WalletTransactionEntity> wrapper = new LambdaQueryWrapper<>();
     wrapper.eq(WalletTransactionEntity::getWalletId, walletId)
     .eq(transactionType != null, WalletTransactionEntity::getTransactionType, transactionType)
     .eq(transactionStatus != null, WalletTransactionEntity::getTransactionStatus, transactionStatus)
     .ge(startDate != null, WalletTransactionEntity::getCompletedTime, startDate)
     .le(endDate != null, WalletTransactionEntity::getCompletedTime, endDate)
     .orderByDesc(WalletTransactionEntity::getCompletedTime);

     List<WalletTransactionEntity> entities = walletTransactionMapper.selectList(wrapper);

     // 直接转换Entity为VO
     return walletAssembler.transactionEntitiesToVOs(entities);
     }
     */

    /**
     * 分页查询钱包主表信息
     */
    public IPage<WalletListVO> queryWalletPage(Page<WalletEntity> page, MPJLambdaWrapperEx<WalletEntity> wrapperEx) {
        log.info("分页查询钱包主表信息：页码={}, 每页条数={}", page.getCurrent(), page.getSize());

        // 内连接reseller表，条件为经销商ID相等且reseller未逻辑删除
        wrapperEx
                .selectAll(WalletEntity.class)
                .selectAs(Reseller::getResellerName, WalletListVO::getResellerName)
                .innerJoin(Reseller.class, on -> on.eq(Reseller::getId, WalletEntity::getResellerId).eq(Reseller::getDelFlag, 0))
                // 钱包逻辑删除条件
                .eq(WalletEntity::getDelFlag, 0)
                .orderByDesc(WalletEntity::getCreatedTime)
        ; // reseller逻辑删除条件

        // 按创建时间降序
        wrapperEx.orderByDesc(WalletEntity::getCreatedTime);

        return walletMapper.selectJoinPage(page, WalletListVO.class, wrapperEx);
    }

    /**
     * 分页查询钱包交易记录
     */
    public IPage<WalletTransactionListVO> queryTransactionPage(Page<WalletTransactionEntity> page,
                                                               MPJLambdaWrapperEx<WalletTransactionEntity> wrapper) {
        log.info("分页查询钱包交易记录：页码={}, 每页条数={}", page.getCurrent(), page.getSize());

        wrapper
                .selectAll(WalletTransactionEntity.class)
                .selectAs(Order::getId, WalletTransactionListVO::getOrderId)
                .selectAs(Order::getContractNumber, WalletTransactionListVO::getContractNumber)
                .selectAs(PaymentPO::getId, WalletTransactionListVO::getPaymentId)
                .selectAs(PaymentPO::getCode, WalletTransactionListVO::getPaymentCode)
                .leftJoin(PaymentTransactionPO.class, left -> left
                        .eq(WalletTransactionEntity::getId, PaymentTransactionPO::getChannelTransactionId)
                        .eq(PaymentTransactionPO::getDelFlag, BooleanConstant.INT_FALSE)
                )
                .leftJoin(PaymentPO.class, left -> left
                        .eq(PaymentPO::getId, PaymentTransactionPO::getPaymentId)
                        .eq(PaymentPO::getDelFlag, BooleanConstant.INT_FALSE)
                )
                .leftJoin(Order.class, left -> left
                        .eq(PaymentPO::getOrderId, Order::getId)
                        .eq(PaymentPO::getDelFlag, BooleanConstant.INT_FALSE)
                );


        wrapper.orderByDesc(WalletTransactionEntity::getCreateTime);
        wrapper.orderByDesc(WalletTransactionEntity::getCompletedTime);
        return walletTransactionMapper.selectJoinPage(page, WalletTransactionListVO.class, wrapper);
    }

    @Override
    public void getAvailablePayWays(PayWayCanUseContext payWayCanUseContext) {

        if (isWalletEnabled(payWayCanUseContext.getResellerId())) {
            payWayCanUseContext.getAvailablePayWays().add(PayWayConstants.WALLET);
        }
    }
}