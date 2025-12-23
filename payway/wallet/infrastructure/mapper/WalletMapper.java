package com.bytz.modules.cms.payway.wallet.infrastructure.mapper;

import com.bytz.modules.cms.payway.wallet.infrastructure.entity.WalletEntity;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 钱包Mapper接口
 * Wallet Mapper Interface
 */
@Mapper
public interface WalletMapper extends MPJBaseMapper<WalletEntity> {
}
