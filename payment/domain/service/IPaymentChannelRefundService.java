package com.bytz.modules.cms.payment.domain.service;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;

/**
 * 支付渠道退款服务接口
 * Payment Channel Refund Service Interface
 * 
 * <p>定义各支付渠道执行退款的统一接口</p>
 * <p>不同渠道有不同的退款处理方式，根据返回状态判断：</p>
 * <ul>
 *   <li>SUCCESS：同步退款，调用后立即完成</li>
 *   <li>PROCESSING：异步退款，需等待回调通知最终结果</li>
 *   <li>FAILED：退款失败</li>
 * </ul>
 */
public interface IPaymentChannelRefundService {
    
    /**
     * 获取支持的支付渠道类型
     * 
     * @return 支付渠道类型
     */
    PaymentChannel getSupportedChannel();
    
    /**
     * 执行退款
     * 根据返回状态判断同步/异步：
     * - SUCCESS: 同步退款，已完成
     * - PROCESSING: 异步退款，等待回调
     * - FAILED: 退款失败
     * 
     * @param request 退款请求
     * @return 退款响应
     */
    ChannelRefundResponse executeRefund(ChannelRefundRequest request);
}
