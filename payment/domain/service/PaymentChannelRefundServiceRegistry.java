package com.bytz.modules.cms.payment.domain.service;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.shared.exception.PaymentErrorCode;
import com.bytz.modules.cms.payment.shared.exception.PaymentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付渠道退款服务注册表
 * Payment Channel Refund Service Registry
 * 
 * <p>管理各支付渠道的退款服务实现</p>
 * <p>Spring容器启动时自动注册所有实现了 {@link IPaymentChannelRefundService} 接口的服务</p>
 */
@Slf4j
@Component
public class PaymentChannelRefundServiceRegistry {
    
    private final List<IPaymentChannelRefundService> refundServices;
    private final Map<PaymentChannel, IPaymentChannelRefundService> serviceMap = new HashMap<>();
    
    public PaymentChannelRefundServiceRegistry(List<IPaymentChannelRefundService> refundServices) {
        this.refundServices = refundServices;
    }
    
    @PostConstruct
    public void init() {
        for (IPaymentChannelRefundService service : refundServices) {
            PaymentChannel channel = service.getSupportedChannel();
            if (serviceMap.containsKey(channel)) {
                log.warn("支付渠道 {} 的退款服务已存在，将被覆盖", channel);
            }
            serviceMap.put(channel, service);
            log.info("注册支付渠道退款服务: {} -> {}", channel, service.getClass().getSimpleName());
        }
    }
    
    /**
     * 获取指定渠道的退款服务
     * 
     * @param channel 支付渠道
     * @return 退款服务
     * @throws PaymentException 如果渠道不支持退款
     */
    public IPaymentChannelRefundService getRefundService(PaymentChannel channel) {
        IPaymentChannelRefundService service = serviceMap.get(channel);
        if (service == null) {
            throw new PaymentException(PaymentErrorCode.CHANNEL_NOT_SUPPORTED,
                    String.format("支付渠道 %s 不支持退款操作", channel.getDescription()));
        }
        return service;
    }
    
    /**
     * 检查渠道是否支持退款
     * 
     * @param channel 支付渠道
     * @return true-支持，false-不支持
     */
    public boolean isRefundSupported(PaymentChannel channel) {
        return serviceMap.containsKey(channel);
    }
}
