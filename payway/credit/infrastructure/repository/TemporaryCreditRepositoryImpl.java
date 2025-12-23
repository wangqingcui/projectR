package com.bytz.modules.cms.payway.credit.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bytz.modules.cms.payway.credit.domain.entity.CreditBillEntity;
import com.bytz.modules.cms.payway.credit.domain.enums.RepaymentStatus;
import com.bytz.modules.cms.payway.credit.domain.enums.TemporaryCreditStatus;
import com.bytz.modules.cms.payway.credit.domain.enums.TransactionType;
import com.bytz.modules.cms.payway.credit.domain.model.TemporaryCreditAggregate;
import com.bytz.modules.cms.payway.credit.domain.repository.ITemporaryCreditRepository;
import com.bytz.modules.cms.payway.credit.infrastructure.assembler.CreditInfrastructureAssembler;
import com.bytz.modules.cms.payway.credit.infrastructure.entity.CreditBillPO;
import com.bytz.modules.cms.payway.credit.infrastructure.entity.TemporaryCreditPO;
import com.bytz.modules.cms.payway.credit.infrastructure.mapper.CreditBillMapper;
import com.bytz.modules.cms.payway.credit.infrastructure.mapper.TemporaryCreditMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 临时授信仓储实现
 * Temporary Credit Repository Implementation
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class TemporaryCreditRepositoryImpl implements ITemporaryCreditRepository {

    private final TemporaryCreditMapper temporaryCreditMapper;
    private final CreditBillMapper creditBillMapper;
    private final CreditInfrastructureAssembler creditInfrastructureAssembler;

    @Override
    public void insert(TemporaryCreditAggregate aggregate) {
        // 插入临时授信主记录
        TemporaryCreditPO po = creditInfrastructureAssembler.temporaryCreditToPO(aggregate);
        temporaryCreditMapper.insert(po);

        // 同步持久化后的数据回聚合根
        creditInfrastructureAssembler.updateTemporaryCreditFromPO(aggregate, po);
    }

    @Override
    public void update(TemporaryCreditAggregate aggregate) {
        // 更新临时授信主记录
        TemporaryCreditPO po = creditInfrastructureAssembler.temporaryCreditToPO(aggregate);
        temporaryCreditMapper.updateById(po);

        // 批量插入新账单
        List<CreditBillEntity> newBills = aggregate.getNewBills();
        if (!newBills.isEmpty()) {
            for (CreditBillEntity newBill : newBills) {
                CreditBillPO billPO = creditInfrastructureAssembler.billEntityToPO(newBill);
                creditBillMapper.insert(billPO);
                // 同步持久化后的数据回账单实体
                creditInfrastructureAssembler.updateBillEntityFromPO(newBill, billPO);

            }
        }
        List<CreditBillEntity> unpaidBills = aggregate.getUnpaidBills();
        for (CreditBillEntity bill : unpaidBills) {
            if (RepaymentStatus.needUpdate.contains(bill.getRepaymentStatus())) {
                CreditBillPO billPO = creditInfrastructureAssembler.billEntityToPO(bill);
                creditBillMapper.updateById(billPO);
                // 同步持久化后的数据回账单实体
                creditInfrastructureAssembler.updateBillEntityFromPO(bill, billPO);
            }
        }
        unpaidBills.addAll(newBills);
        aggregate.clearTempCollections();
    }

    @Override
    public Optional<TemporaryCreditAggregate> findById(String id, boolean loadUnpaidBills) {
        TemporaryCreditPO po = temporaryCreditMapper.selectById(id);
        if (po == null) {
            return Optional.empty();
        }

        TemporaryCreditAggregate aggregate = creditInfrastructureAssembler.poToTemporaryCredit(po);

        if (loadUnpaidBills) {
            List<CreditBillEntity> bills = loadUnpaidBillsByTemporaryCreditId(id);
            aggregate.setUnpaidBills(bills);
        }

        return Optional.of(aggregate);
    }

    @Override
    public Optional<CreditBillEntity> findBillById(String temporaryCreditId, String billId) {
        LambdaQueryWrapper<CreditBillPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CreditBillPO::getId, billId)
                .eq(CreditBillPO::getTemporaryCreditId, temporaryCreditId);

        CreditBillPO po = creditBillMapper.selectOne(wrapper);
        return Optional.ofNullable(po)
                .map(creditInfrastructureAssembler::poToBillEntity);
    }

    @Override
    public List<CreditBillEntity> findBillsByIds(String temporaryCreditId, List<String> billIds) {
        LambdaQueryWrapper<CreditBillPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(CreditBillPO::getId, billIds)
                .eq(CreditBillPO::getTemporaryCreditId, temporaryCreditId);

        List<CreditBillPO> pos = creditBillMapper.selectList(wrapper);
        return creditInfrastructureAssembler.posToBillEntities(pos);
    }

    @Override
    public Optional<TemporaryCreditAggregate> findByApprovalId(String approvalId) {
        LambdaQueryWrapper<TemporaryCreditPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemporaryCreditPO::getApprovalId, approvalId);

        TemporaryCreditPO po = temporaryCreditMapper.selectOne(wrapper);
        return Optional.ofNullable(po)
                .map(creditInfrastructureAssembler::poToTemporaryCredit);
    }

    @Override
    public List<TemporaryCreditAggregate> findActiveByResellerId(String resellerId) {
        LambdaQueryWrapper<TemporaryCreditPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemporaryCreditPO::getResellerId, resellerId)
                .in(TemporaryCreditPO::getStatus,
                        TemporaryCreditStatus.APPROVED,
                        TemporaryCreditStatus.IN_USE);

        List<TemporaryCreditPO> pos = temporaryCreditMapper.selectList(wrapper);
        return creditInfrastructureAssembler.posToTemporaryCredits(pos);
    }

    @Override
    public List<TemporaryCreditAggregate> findExpiredCredits(LocalDate currentDate) {
        LambdaQueryWrapper<TemporaryCreditPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(TemporaryCreditPO::getExpiryDate, currentDate)
                .in(TemporaryCreditPO::getStatus,
                        TemporaryCreditStatus.APPROVED,
                        TemporaryCreditStatus.IN_USE);

        List<TemporaryCreditPO> pos = temporaryCreditMapper.selectList(wrapper);
        return creditInfrastructureAssembler.posToTemporaryCredits(pos);
    }

    @Override
    public List<CreditBillEntity> findOverdueBills(LocalDate currentDate) {
        LambdaQueryWrapper<CreditBillPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(CreditBillPO::getDueDate, currentDate)
                .ne(CreditBillPO::getRepaymentStatus, RepaymentStatus.REPAID)
                .eq(CreditBillPO::getTransactionType, TransactionType.TEMPORARY_CREDIT_PAY);

        List<CreditBillPO> pos = creditBillMapper.selectList(wrapper);
        return creditInfrastructureAssembler.posToBillEntities(pos);
    }

    @Override
    public void deleteById(String id) {
        temporaryCreditMapper.deleteById(id);
    }

    @Override
    public void updateWalletId(String resellerId, String walletId) {
        LambdaUpdateWrapper<TemporaryCreditPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(TemporaryCreditPO::getResellerId, resellerId)
//                .set(TemporaryCreditPO::getCreditWalletId, walletId)
        ;
        TemporaryCreditPO temporaryCreditPO = TemporaryCreditPO.builder().creditWalletId(walletId).build();
        int update = temporaryCreditMapper.update(temporaryCreditPO, wrapper);
        log.info("更新临时授信钱包ID，更新数量：{}", update);
    }

    private List<CreditBillEntity> loadUnpaidBillsByTemporaryCreditId(String temporaryCreditId) {
        LambdaQueryWrapper<CreditBillPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CreditBillPO::getTemporaryCreditId, temporaryCreditId)
                .ne(CreditBillPO::getRepaymentStatus, RepaymentStatus.REPAID);

        List<CreditBillPO> pos = creditBillMapper.selectList(wrapper);
        return creditInfrastructureAssembler.posToBillEntities(pos);
    }
}