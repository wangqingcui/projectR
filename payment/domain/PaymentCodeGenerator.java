package com.bytz.modules.cms.payment.domain;

import com.bytz.modules.cms.shared.util.BusinessCodeGenerator;
import org.springframework.stereotype.Service;

/**
 * 支付单号生成服务
 * 负责生成唯一的支付单号
 */
@Service
public class PaymentCodeGenerator {

    /**
     * 生成唯一的支付单号
     *
     * @return 唯一的支付单号
     */
    public String generatePaymentCode() {
        return BusinessCodeGenerator.generateCode("PAY");
    }

    /**
     * 生成唯一的支付流水号
     *
     * @return 唯一的支付流水号
     */
    public String generateTransactionCode() {
        return BusinessCodeGenerator.generateCode("TXN");
    }
}