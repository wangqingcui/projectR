package com.bytz.modules.cms.payment.infrastructure.mapper;

import com.bytz.modules.cms.payment.infrastructure.entity.PaymentPO;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付单Mapper接口
 * Payment Mapper Interface
 * 
 * <p>继承MyBatis-Plus的BaseMapper，提供基本的CRUD操作</p>
 */
@Mapper
public interface PaymentMapper extends MPJBaseMapper<PaymentPO> {
}