package com.bytz.modules.cms.payway.credit.application.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytz.common.constant.CommonConstant;
import com.bytz.common.system.query.MPJLambdaWrapperEx;
import com.bytz.modules.cms.payway.constant.PayWayConstants;
import com.bytz.modules.cms.payway.credit.application.assembler.CreditAssembler;
import com.bytz.modules.cms.payway.credit.application.model.CreditWalletListVo;
import com.bytz.modules.cms.payway.credit.application.model.CreditWalletVO;
import com.bytz.modules.cms.payway.credit.domain.model.CreditWalletAggregate;
import com.bytz.modules.cms.payway.credit.domain.repository.ICreditWalletRepository;
import com.bytz.modules.cms.payway.credit.infrastructure.entity.CreditWalletPO;
import com.bytz.modules.cms.payway.credit.infrastructure.mapper.CreditWalletMapper;
import com.bytz.modules.cms.payway.credit.shared.exception.CreditWalletErrorCode;
import com.bytz.modules.cms.payway.credit.shared.exception.CreditWalletException;
import com.bytz.modules.cms.payway.model.PayWayCanUseContext;
import com.bytz.modules.cms.payway.service.PayWayCanUseService;
import com.bytz.modules.cms.reseller.entity.Reseller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * 信用钱包查询服务
 * Credit Wallet Query Service
 *
 * <p>处理钱包查询操作（CQRS中的Query端）</p>
 * <p>职责：
 * <ul>
 *   <li>钱包查询</li>
 *   <li>返回VO对象</li>
 *   <li>继承ServiceImpl以便利用MyBatis-Plus的查询能力</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@Order(10)
public class CreditWalletQueryService extends ServiceImpl<CreditWalletMapper, CreditWalletPO> implements PayWayCanUseService {

    private final ICreditWalletRepository creditWalletRepository;
    private final CreditAssembler creditAssembler;

    public CreditWalletQueryService(ICreditWalletRepository creditWalletRepository,
                                    CreditAssembler creditAssembler) {
        this.creditWalletRepository = creditWalletRepository;
        this.creditAssembler = creditAssembler;
    }

    /**
     * 根据经销商ID查询钱包
     *
     * @param resellerId 经销商ID
     * @return 信用钱包VO
     */
    public CreditWalletVO getByResellerId(String resellerId) {
        log.info("Querying wallet for reseller: {}", resellerId);

        CreditWalletAggregate wallet = creditWalletRepository.findByResellerId(resellerId, false)
                .orElseThrow(() -> new CreditWalletException(CreditWalletErrorCode.WALLET_NOT_FOUND));

        return creditAssembler.toVO(wallet);
    }

    public String creditWalletId(String resellerId) {

        return creditWalletRepository.findByResellerId(resellerId, false)
                .filter(CreditWalletAggregate::getEnabled)
                .map(CreditWalletAggregate::getId)
                .orElse(null);
    }

    /**
     * 根据钱包ID查询钱包
     *
     * @param walletId 钱包ID
     * @return 信用钱包VO
     */
    public CreditWalletVO getById(String walletId) {
        log.info("Querying wallet by ID: {}", walletId);

        CreditWalletAggregate wallet = creditWalletRepository.findById(walletId, false)
                .orElseThrow(() -> new CreditWalletException(CreditWalletErrorCode.WALLET_NOT_FOUND));

        return creditAssembler.toVO(wallet);
    }

    /**
     * 分页查询信用钱包列表
     *
     * @param page      分页参数
     * @param wrapperEx 查询条件
     * @return 分页信用钱包列表
     */
    public IPage<CreditWalletListVo> queryWalletPage(Page<CreditWalletPO> page, MPJLambdaWrapperEx<CreditWalletPO> wrapperEx) {

        wrapperEx
                .selectAll(CreditWalletPO.class)
                .selectAs(Reseller::getResellerName, CreditWalletListVo::getResellerName)
                .leftJoin(Reseller.class, left -> left.eq(CreditWalletPO::getResellerId, Reseller::getId).eq(Reseller::getDelFlag, CommonConstant.DEL_FLAG_0))
        ;

        IPage<CreditWalletListVo> poPage = baseMapper.selectJoinPage(page, CreditWalletListVo.class, wrapperEx);

        return poPage;
    }

    @Override
    public void getAvailablePayWays(PayWayCanUseContext payWayCanUseContext) {
        creditWalletRepository.findByResellerId(payWayCanUseContext.getResellerId(), false)
                .filter(CreditWalletAggregate::getEnabled)
                .filter(item -> !item.getFrozen())
                .ifPresent(item -> {
                    payWayCanUseContext.getAvailablePayWays().add(PayWayConstants.CREDIT_WALLET);
                    payWayCanUseContext.setCreditWalletId(item.getId());
                });

    }
}