package com.bytz.modules.cms.payway.service;

import com.bytz.modules.cms.payway.model.PayWayCanUseContext;

public interface PayWayCanUseService {


    void getAvailablePayWays(PayWayCanUseContext payWayCanUseContext);
}
