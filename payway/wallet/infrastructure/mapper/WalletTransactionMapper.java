package com.bytz.modules.cms.payway.wallet.infrastructure.mapper;

import com.bytz.modules.cms.payway.wallet.infrastructure.entity.WalletTransactionEntity;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 钱包交易Mapper接口
 * Wallet Transaction Mapper Interface
 */
@Mapper
public interface WalletTransactionMapper extends MPJBaseMapper<WalletTransactionEntity> {
}
