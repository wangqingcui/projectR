package com.bytz.modules.cms.payway.wallet.domain.repository;

import com.bytz.modules.cms.payway.wallet.domain.enums.WalletTransactionStatus;
import com.bytz.modules.cms.payway.wallet.domain.enums.WalletTransactionType;
import com.bytz.modules.cms.payway.wallet.domain.model.WalletAggregate;
import com.bytz.modules.cms.payway.wallet.domain.valueobject.WalletTransactionValueObject;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 钱包仓储接口
 * Wallet Repository Interface
 * 
 * 定义钱包聚合的持久化操作
 */
public interface IWalletRepository {
    
    /**
     * 保存钱包聚合（包含钱包主体和交易记录）
     */
    WalletAggregate save(WalletAggregate wallet);
    
    /**
     * 根据ID查询钱包
     */
    Optional<WalletAggregate> findById(String walletId);
    
    /**
     * 根据经销商ID查询钱包
     */
    Optional<WalletAggregate> findByResellerId(String resellerId);
    
    /**
     * 检查经销商是否已存在钱包
     */
    boolean existsByResellerId(String resellerId);
    
    /**
     * 更新钱包（包含钱包主体和交易记录）
     */
    WalletAggregate update(WalletAggregate wallet);
    
    /**
     * 根据ID查询钱包交易
     */
    Optional<WalletTransactionValueObject> findTransactionById(String transactionId);
    
    /**
     * 根据钱包ID查询交易记录
     */
    List<WalletTransactionValueObject> findTransactionsByWalletId(
            String walletId, 
            WalletTransactionType transactionType, 
            WalletTransactionStatus transactionStatus,
            LocalDateTime startDate, 
            LocalDateTime endDate);
}