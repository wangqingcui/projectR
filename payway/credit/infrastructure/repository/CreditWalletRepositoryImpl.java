package com.bytz.modules.cms.payway.credit.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bytz.modules.cms.payway.credit.domain.entity.CreditBillEntity;
import com.bytz.modules.cms.payway.credit.domain.enums.RepaymentStatus;
import com.bytz.modules.cms.payway.credit.domain.enums.TransactionType;
import com.bytz.modules.cms.payway.credit.domain.model.CreditWalletAggregate;
import com.bytz.modules.cms.payway.credit.domain.repository.ICreditWalletRepository;
import com.bytz.modules.cms.payway.credit.infrastructure.assembler.CreditInfrastructureAssembler;
import com.bytz.modules.cms.payway.credit.infrastructure.entity.CreditBillPO;
import com.bytz.modules.cms.payway.credit.infrastructure.entity.CreditWalletPO;
import com.bytz.modules.cms.payway.credit.infrastructure.entity.ManagerLogPO;
import com.bytz.modules.cms.payway.credit.infrastructure.mapper.CreditBillMapper;
import com.bytz.modules.cms.payway.credit.infrastructure.mapper.CreditWalletMapper;
import com.bytz.modules.cms.payway.credit.infrastructure.mapper.ManagerLogMapper;
import com.bytz.modules.cms.payway.credit.shared.exception.CreditWalletErrorCode;
import com.bytz.modules.cms.payway.credit.shared.exception.CreditWalletException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 信用钱包仓储实现
 * Credit Wallet Repository Implementation
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class CreditWalletRepositoryImpl implements ICreditWalletRepository {

    private final CreditWalletMapper creditWalletMapper;
    private final CreditBillMapper creditBillMapper;
    private final ManagerLogMapper managerLogMapper;
    private final CreditInfrastructureAssembler creditInfrastructureAssembler;

    @Override
    public void insert(CreditWalletAggregate aggregate) {
        // 插入钱包主记录
        CreditWalletPO po = creditInfrastructureAssembler.aggregateToPO(aggregate);
        creditWalletMapper.insert(po);

        // 同步持久化后的数据回聚合根（包括数据库生成的ID、时间戳等）
        creditInfrastructureAssembler.updateAggregateFromPO(aggregate, po);

        // 批量插入管理日志
        if (!aggregate.getNewManagerLogs().isEmpty()) {
            List<ManagerLogPO> logPOs = creditInfrastructureAssembler.managerLogsToPOs(aggregate.getNewManagerLogs());
            for (ManagerLogPO logPO : logPOs) {
                managerLogMapper.insert(logPO);
            }
        }
        aggregate.clearTempCollections();
    }

    @Override
    public void update(CreditWalletAggregate aggregate) {
        // 更新钱包主记录
        CreditWalletPO po = creditInfrastructureAssembler.aggregateToPO(aggregate);
        creditWalletMapper.updateById(po);
        // 同步持久化后的数据回聚合根（包括更新时间等）
        creditInfrastructureAssembler.updateAggregateFromPO(aggregate, po);

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

        // 批量插入新管理日志
        if (!aggregate.getNewManagerLogs().isEmpty()) {
            List<ManagerLogPO> logPOs = creditInfrastructureAssembler.managerLogsToPOs(aggregate.getNewManagerLogs());
            for (ManagerLogPO logPO : logPOs) {
                managerLogMapper.insert(logPO);
            }
        }
        aggregate.clearTempCollections();        // 更新需要持久化状态变化的账单（REPAYING 或 REPAID 均需落库）
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
    public Optional<CreditWalletAggregate> findById(String id, boolean loadUnpaidBills) {
        CreditWalletPO po = creditWalletMapper.selectById(id);
        if (po == null) {
            return Optional.empty();
        }

        CreditWalletAggregate aggregate = creditInfrastructureAssembler.poToAggregate(po);

        if (loadUnpaidBills) {
            List<CreditBillEntity> bills = loadUnpaidBillsByWalletId(id);
            aggregate.setUnpaidBills(bills);
        }

        return Optional.of(aggregate);
    }

    @Override
    public Optional<CreditWalletAggregate> findByResellerId(String resellerId, boolean loadUnpaidBills) {
        LambdaQueryWrapper<CreditWalletPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CreditWalletPO::getResellerId, resellerId);

        CreditWalletPO po = creditWalletMapper.selectOne(wrapper);
        if (po == null) {
            return Optional.empty();
        }

        CreditWalletAggregate aggregate = creditInfrastructureAssembler.poToAggregate(po);

        if (loadUnpaidBills) {
            List<CreditBillEntity> bills = loadUnpaidBillsByWalletId(po.getId());
            aggregate.setUnpaidBills(bills);
        }

        return Optional.of(aggregate);
    }

    @Override
    public Optional<CreditWalletAggregate> findByBillId(String billId) {

        CreditBillPO creditBillPO = creditBillMapper.selectById(billId);
        if (creditBillPO == null) {
            return Optional.empty();
        }

        CreditWalletPO po = creditWalletMapper.selectById(creditBillPO.getCreditWalletId());
        if (po == null) {
            throw new CreditWalletException(CreditWalletErrorCode.WALLET_NOT_FOUND);
        }
        CreditWalletAggregate aggregate = creditInfrastructureAssembler.poToAggregate(po);

        aggregate.setUnpaidBills(Collections.singletonList(creditInfrastructureAssembler.poToBillEntity(creditBillPO)));
        return Optional.of(aggregate);
    }

    @Override
    public Optional<CreditWalletAggregate> findByBillIds(List<String> billIds) {
        List<CreditBillPO> creditBillPOS = creditBillMapper.selectBatchIds(billIds);
        if (creditBillPOS.isEmpty()) {
            return Optional.empty();
        }
        Set<String> collect = creditBillPOS.stream().map(CreditBillPO::getCreditWalletId).collect(Collectors.toSet());
        if (collect.size() != 1) {
            throw new CreditWalletException(CreditWalletErrorCode.WALLET_ERROR_CODE);
        }
        Optional<String> first = collect.stream().findFirst();
        String walletId = first.orElseThrow(() -> new CreditWalletException(CreditWalletErrorCode.WALLET_NOT_FOUND));
        CreditWalletPO po = creditWalletMapper.selectById(walletId);
        if (po == null) {
            throw new CreditWalletException(CreditWalletErrorCode.WALLET_NOT_FOUND);
        }
        CreditWalletAggregate aggregate = creditInfrastructureAssembler.poToAggregate(po);
        aggregate.setUnpaidBills(creditInfrastructureAssembler.posToBillEntities(creditBillPOS));

        return Optional.of(aggregate);
    }

    @Override
    public Optional<CreditBillEntity> findBillById(String walletId, String billId) {
        LambdaQueryWrapper<CreditBillPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CreditBillPO::getId, billId)
                .eq(CreditBillPO::getCreditWalletId, walletId);

        CreditBillPO po = creditBillMapper.selectOne(wrapper);
        return Optional.ofNullable(po)
                .map(creditInfrastructureAssembler::poToBillEntity);
    }

    @Override
    public List<CreditBillEntity> findBillsByIds(String walletId, List<String> billIds) {
        LambdaQueryWrapper<CreditBillPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(CreditBillPO::getId, billIds)
                .eq(CreditBillPO::getCreditWalletId, walletId);

        List<CreditBillPO> pos = creditBillMapper.selectList(wrapper);
        return creditInfrastructureAssembler.posToBillEntities(pos);
    }

    @Override
    public List<CreditBillEntity> findOverdueBills(LocalDate currentDate) {
        LambdaQueryWrapper<CreditBillPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(CreditBillPO::getDueDate, currentDate)
                .ne(CreditBillPO::getRepaymentStatus, RepaymentStatus.REPAID)
                .eq(CreditBillPO::getTransactionType, TransactionType.CREDIT_PAY);

        List<CreditBillPO> pos = creditBillMapper.selectList(wrapper);
        return creditInfrastructureAssembler.posToBillEntities(pos);
    }

    @Override
    public List<CreditWalletAggregate> findActiveWallets() {
        LambdaQueryWrapper<CreditWalletPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CreditWalletPO::getEnabled, true)
                .eq(CreditWalletPO::getFrozen, false);

        List<CreditWalletPO> pos = creditWalletMapper.selectList(wrapper);
        return creditInfrastructureAssembler.posToAggregates(pos);
    }

    @Override
    public void deleteById(String id) {
        creditWalletMapper.deleteById(id);
    }

    private List<CreditBillEntity> loadUnpaidBillsByWalletId(String walletId) {
        LambdaQueryWrapper<CreditBillPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CreditBillPO::getCreditWalletId, walletId)
                .ne(CreditBillPO::getRepaymentStatus, RepaymentStatus.REPAID);

        List<CreditBillPO> pos = creditBillMapper.selectList(wrapper);
        return creditInfrastructureAssembler.posToBillEntities(pos);
    }
}