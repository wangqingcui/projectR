package com.bytz.modules.cms.payway.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PayWayCanUseContext {

    // 经销商ID
    private final String resellerId;

    // 是否为后台操作
    private final Boolean isAdmin;
//
//    // 是否包含预支付类型支付单
//    private final Boolean prepayPayment;

    // 信用钱包ID
    private String creditWalletId;

    // 可用支付方式列表
    private List<String> availablePayWays = new ArrayList<>();


}
