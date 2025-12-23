package com.bytz.modules.cms.payway.credit.application.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytz.common.constant.CommonConstant;
import com.bytz.common.system.query.MPJLambdaWrapperEx;
import com.bytz.modules.cms.payway.constant.PayWayConstants;
import com.bytz.modules.cms.payway.credit.application.assembler.CreditAssembler;
import com.bytz.modules.cms.payway.credit.application.model.TemporaryCreditListVO;
import com.bytz.modules.cms.payway.credit.application.model.TemporaryCreditVO;
import com.bytz.modules.cms.payway.credit.domain.enums.TemporaryCreditStatus;
import com.bytz.modules.cms.payway.credit.domain.model.TemporaryCreditAggregate;
import com.bytz.modules.cms.payway.credit.domain.repository.ITemporaryCreditRepository;
import com.bytz.modules.cms.payway.credit.infrastructure.entity.TemporaryCreditPO;
import com.bytz.modules.cms.payway.credit.infrastructure.mapper.TemporaryCreditMapper;
import com.bytz.modules.cms.payway.credit.shared.exception.CreditWalletErrorCode;
import com.bytz.modules.cms.payway.credit.shared.exception.CreditWalletException;
import com.bytz.modules.cms.payway.model.PayWayCanUseContext;
import com.bytz.modules.cms.payway.service.PayWayCanUseService;
import com.bytz.modules.cms.reseller.entity.Reseller;
import com.bytz.modules.cms.reseller.model.response.ResellerListVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 临时授信查询服务
 * Temporary Credit Query Service
 *
 * <p>处理临时授信查询操作（CQRS中的Query端）</p>
 * <p>职责：
 * <ul>
 *   <li>临时授信查询</li>
 *   <li>返回VO对象</li>
 *   <li>继承ServiceImpl以便利用MyBatis-Plus的查询能力</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@Order(20)
public class TemporaryCreditQueryService extends ServiceImpl<TemporaryCreditMapper, TemporaryCreditPO> implements PayWayCanUseService {

    private final ITemporaryCreditRepository temporaryCreditRepository;
    private final CreditAssembler creditAssembler;

    public TemporaryCreditQueryService(ITemporaryCreditRepository temporaryCreditRepository,
                                       CreditAssembler creditAssembler) {
        this.temporaryCreditRepository = temporaryCreditRepository;
        this.creditAssembler = creditAssembler;
    }

    /**
     * 根据临时授信ID查询
     *
     * @param temporaryCreditId 临时授信ID
     * @return 临时授信VO
     */
    public TemporaryCreditVO getById(String temporaryCreditId) {
        log.info("Querying temporary credit by ID: {}", temporaryCreditId);

        TemporaryCreditAggregate temporaryCredit = temporaryCreditRepository.findById(temporaryCreditId, false)
                .orElseThrow(() -> new CreditWalletException(CreditWalletErrorCode.TEMPORARY_CREDIT_NOT_FOUND));

        return creditAssembler.toVO(temporaryCredit);
    }

    /**
     * 根据经销商ID查询所有活跃的临时授信
     *
     * @param resellerId 经销商ID
     * @return 临时授信VO列表
     */
    public List<TemporaryCreditVO> getByResellerId(String resellerId) {
        log.info("Querying active temporary credits for reseller: {}", resellerId);

        List<TemporaryCreditAggregate> temporaryCredits =
                temporaryCreditRepository.findActiveByResellerId(resellerId);

        return creditAssembler.toTemporaryCreditVOList(temporaryCredits);
    }

    public IPage<TemporaryCreditListVO> queryPage(Page<TemporaryCreditListVO> page, MPJLambdaWrapperEx<TemporaryCreditPO> lambdaWrapperEx) {

        lambdaWrapperEx
                .selectAll(TemporaryCreditPO.class)
                .selectAs(Reseller::getResellerName, ResellerListVo::getResellerName)
                .leftJoin(Reseller.class, left -> left.eq(Reseller::getId, TemporaryCreditPO::getResellerId).eq(Reseller::getDelFlag, CommonConstant.DEL_FLAG_0))
                .orderByDesc(TemporaryCreditPO::getCreateTime)
                .orderByDesc(TemporaryCreditPO::getId);

        IPage<TemporaryCreditListVO> listVOIPage = this.baseMapper.selectJoinPage(page, TemporaryCreditListVO.class, lambdaWrapperEx);
        return listVOIPage;
    }

    public boolean existTemCredit(String creditedWalletId) {

        LambdaUpdateWrapper<TemporaryCreditPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(TemporaryCreditPO::getCreditWalletId, creditedWalletId);
        boolean exists = this.baseMapper.exists(wrapper);
        return exists;
    }

    public Map<String, BigDecimal> queryTemporaryCreditBalance(String resellerId) {

        List<TemporaryCreditPO> list = this.lambdaQuery()
                .select(TemporaryCreditPO::getId, TemporaryCreditPO::getTotalAmount, TemporaryCreditPO::getRemainingAmount)
                .eq(TemporaryCreditPO::getResellerId, resellerId)
                .ge(TemporaryCreditPO::getExpiryDate, LocalDate.now())
                .ne(TemporaryCreditPO::getStatus, TemporaryCreditStatus.EXPIRED)
                .list();
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal remainingAmount = BigDecimal.ZERO;

        for (TemporaryCreditPO temporaryCreditPO : list) {
            totalAmount = totalAmount.add(temporaryCreditPO.getTotalAmount());
            remainingAmount = remainingAmount.add(temporaryCreditPO.getRemainingAmount());
        }
        HashMap<String, BigDecimal> hashMap = new HashMap<>();
        hashMap.put("totalAmount", totalAmount);
        hashMap.put("remainingAmount", remainingAmount);

        return hashMap;
    }

    @Override
    public void getAvailablePayWays(PayWayCanUseContext payWayCanUseContext) {
        // 把信用排在第一 临时信用放他后面
        if (CollectionUtils.isNotEmpty(payWayCanUseContext.getAvailablePayWays()) && payWayCanUseContext.getAvailablePayWays().contains(PayWayConstants.CREDIT_WALLET)) {
            if (existTemCredit(payWayCanUseContext.getCreditWalletId())) {
                payWayCanUseContext.getAvailablePayWays().add(PayWayConstants.TEMPORARY_CREDIT);
            }
        }
    }
}