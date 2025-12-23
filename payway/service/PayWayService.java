package com.bytz.modules.cms.payway.service;

import com.bytz.modules.cms.payway.model.PayWayCanUseContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PayWayService {

    private final List<PayWayCanUseService> payWayCanUseServices;


    // 获取可用支付方式
    public List<String> getAvailablePayWays(String resellerId, Boolean isAdmin) {


        PayWayCanUseContext payWayCanUseContext = new PayWayCanUseContext(resellerId, isAdmin);

        payWayCanUseServices.forEach(item -> item.getAvailablePayWays(payWayCanUseContext));

        // 后台没有银联 todo


        return payWayCanUseContext.getAvailablePayWays();
    }
}
