package com.bytz.modules.cms.payway.wallet.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bytz.modules.cms.payway.wallet.domain.enums.WalletTransactionStatus;
import com.bytz.modules.cms.payway.wallet.domain.enums.WalletTransactionType;
import com.bytz.modules.cms.payway.wallet.domain.model.WalletAggregate;
import com.bytz.modules.cms.payway.wallet.domain.repository.IWalletRepository;
import com.bytz.modules.cms.payway.wallet.domain.valueobject.WalletTransactionValueObject;
import com.bytz.modules.cms.payway.wallet.infrastructure.assembler.WalletInfrastructureAssembler;
import com.bytz.modules.cms.payway.wallet.infrastructure.entity.WalletEntity;
import com.bytz.modules.cms.payway.wallet.infrastructure.entity.WalletTransactionEntity;
import com.bytz.modules.cms.payway.wallet.infrastructure.mapper.WalletMapper;
import com.bytz.modules.cms.payway.wallet.infrastructure.mapper.WalletTransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 钱包仓储实现
 * Wallet Repository Implementation
 */
@Repository
@RequiredArgsConstructor
public class WalletRepositoryImpl implements IWalletRepository {

    private final WalletMapper walletMapper;
    private final WalletTransactionMapper walletTransactionMapper;
    private final WalletInfrastructureAssembler walletInfrastructureAssembler;

    @Override
    public WalletAggregate save(WalletAggregate wallet) {
        WalletEntity entity = walletInfrastructureAssembler.aggregateToEntity(wallet);
        walletMapper.insert(entity);
        return walletInfrastructureAssembler.entityToAggregate(entity);
    }

    @Override
    public Optional<WalletAggregate> findById(String walletId) {
        WalletEntity entity = walletMapper.selectById(walletId);
        return Optional.ofNullable(entity)
                .map(walletInfrastructureAssembler::entityToAggregate);
    }

    @Override
    public Optional<WalletAggregate> findByResellerId(String resellerId) {
        LambdaQueryWrapper<WalletEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WalletEntity::getResellerId, resellerId);
        WalletEntity entity = walletMapper.selectOne(wrapper);
        return Optional.ofNullable(entity)
                .map(walletInfrastructureAssembler::entityToAggregate);
    }

    @Override
    public boolean existsByResellerId(String resellerId) {
        LambdaQueryWrapper<WalletEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WalletEntity::getResellerId, resellerId);
        return walletMapper.exists(wrapper);
    }

    @Override
    public WalletAggregate update(WalletAggregate wallet) {
        // 更新钱包主体信息
        WalletEntity entity = walletInfrastructureAssembler.aggregateToEntity(wallet);
        walletMapper.updateById(entity);


        WalletAggregate aggregate = walletInfrastructureAssembler.entityToAggregate(entity);
        // 如果存在当前交易记录，同时保存交易记录
        if (wallet.getCurrentTransaction() != null) {
            WalletTransactionEntity transactionEntity = walletInfrastructureAssembler.transactionValueObjectToEntity(wallet.getCurrentTransaction());
            walletTransactionMapper.insert(transactionEntity);
            aggregate.setCurrentTransaction(walletInfrastructureAssembler.transactionEntityToValueObject(transactionEntity));
        }
        return aggregate;
    }

    @Override
    public Optional<WalletTransactionValueObject> findTransactionById(String transactionId) {
        WalletTransactionEntity entity = walletTransactionMapper.selectById(transactionId);
        return Optional.ofNullable(entity)
                .map(walletInfrastructureAssembler::transactionEntityToValueObject);
    }

    @Override
    public List<WalletTransactionValueObject> findTransactionsByWalletId(
            String walletId, WalletTransactionType transactionType, WalletTransactionStatus transactionStatus,
            LocalDateTime startDate, LocalDateTime endDate) {

        LambdaQueryWrapper<WalletTransactionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WalletTransactionEntity::getWalletId, walletId)
                .eq(transactionType != null, WalletTransactionEntity::getTransactionType, transactionType)
                .eq(transactionStatus != null, WalletTransactionEntity::getTransactionStatus, transactionStatus)
                .ge(startDate != null, WalletTransactionEntity::getCompletedTime, startDate)
                .le(endDate != null, WalletTransactionEntity::getCompletedTime, endDate);


        wrapper.orderByDesc(WalletTransactionEntity::getCompletedTime);

        List<WalletTransactionEntity> entities = walletTransactionMapper.selectList(wrapper);
        return walletInfrastructureAssembler.transactionEntitiesToValueObjects(entities);
    }
}