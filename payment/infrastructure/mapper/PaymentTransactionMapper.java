package com.bytz.modules.cms.payment.infrastructure.mapper;

import com.bytz.modules.cms.payment.infrastructure.entity.PaymentTransactionPO;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付流水Mapper接口
 * Payment Transaction Mapper Interface
 * 
 * <p>继承MyBatis-Plus的BaseMapper，提供基本的CRUD操作</p>
 */
@Mapper
public interface PaymentTransactionMapper extends MPJBaseMapper<PaymentTransactionPO> {
}