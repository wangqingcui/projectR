//package com.bytz.modules.cms.payment.interfaces;
//
//import com.bytz.modules.cms.payment.application.model.*;
//import com.bytz.modules.cms.payment.application.service.PaymentApplicationService;
//import com.bytz.modules.cms.payment.application.service.PaymentQueryService;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//
//import javax.validation.Valid;
//import java.math.BigDecimal;
//import java.util.List;
//
///**
// * 支付单控制器
// * Payment Controller
// *
// * <p>提供支付单相关的REST API接口</p>
// */
//@Api(tags = "支付单管理")
//@Slf4j
//@RestController
//@RequestMapping("/api/v1/payments")
//@RequiredArgsConstructor
//@Validated
//public class PaymentController {
//
//    private final PaymentApplicationService paymentApplicationService;
//    private final PaymentQueryService paymentQueryService;
//
//    // ==================== 写操作 ====================
//
//    /**
//     * 创建支付单
//     */
//    @ApiOperation("创建支付单")
//    @PostMapping
//    public PaymentVO createPayment(@Valid @RequestBody PaymentCreateRO ro) {
//        log.info("创建支付单，订单号: {}", ro.getOrderId());
//        return paymentApplicationService.createPayment(ro);
//    }
//
//    /**
//     * 关闭支付单
//     */
//    @ApiOperation("关闭支付单")
//    @PostMapping("/close")
//    public PaymentVO closePayment(@Valid @RequestBody ClosePaymentRO ro) {
//        log.info("关闭支付单，支付单ID: {}", ro.getPaymentId());
//        return paymentApplicationService.closePayment(ro);
//    }
//
//    /**
//     * 执行支付（创建支付流水）
//     */
//    @ApiOperation("执行支付")
//    @PostMapping("/execute")
//    public PaymentTransactionVO executePayment(@Valid @RequestBody ExecutePaymentRO ro) {
//        log.info("执行支付，支付单ID: {}", ro.getPaymentId());
//        return paymentApplicationService.executePayment(ro);
//    }
//
//    /**
//     * 处理支付完成回调
//     * 回调只提供channelTransactionId，可能对应多个支付单
//     */
//    @ApiOperation("支付完成回调")
//    @PostMapping("/complete")
//    public List<PaymentTransactionVO> handlePaymentComplete(@Valid @RequestBody CompletePaymentRO ro) {
//        log.info("处理支付完成回调，渠道交易记录ID: {}", ro.getChannelTransactionId());
//        return paymentApplicationService.handlePaymentComplete(ro);
//    }
//
//    /**
//     * 创建退款支付单
//     */
//    @ApiOperation("创建退款支付单")
//    @PostMapping("/refund")
//    public PaymentVO createRefundPayment(@Valid @RequestBody CreateRefundRO ro) {
//        log.info("创建退款支付单，原支付单ID: {}", ro.getOriginalPaymentId());
//        return paymentApplicationService.createRefund(ro);
//    }
//
//    /**
//     * 执行退款（创建退款流水）
//     * 在退款支付单创建后，由渠道调用创建退款流水
//     */
//    @ApiOperation("执行退款")
//    @PostMapping("/refund/execute")
//    public PaymentTransactionVO executeRefund(@Valid @RequestBody ExecuteRefundRO ro) {
//        log.info("执行退款，退款支付单ID: {}, 原流水ID: {}", ro.getRefundPaymentId(), ro.getOriginalTransactionId());
//        return paymentApplicationService.executeRefund(ro);
//    }
//
//    /**
//     * 退款回调（异步退款渠道）
//     * 接收异步退款渠道的回调通知
//     */
//    @ApiOperation("退款回调")
//    @PostMapping("/refund/callback")
//    public List<PaymentTransactionVO> handleRefundCallback(@Valid @RequestBody RefundCallbackRO ro) {
//        log.info("处理退款回调，支付渠道: {}, 渠道交易记录ID: {}", ro.getPaymentChannel(), ro.getChannelTransactionId());
//        return paymentApplicationService.handleRefundCallback(ro);
//    }
//
//    // ==================== 查询操作 ====================
//
//    /**
//     * 根据ID查询支付单
//     */
//    @ApiOperation("根据ID查询支付单")
//    @GetMapping("/{paymentId}")
//    public PaymentVO getById(@PathVariable String paymentId) {
//        log.info("根据ID查询支付单，paymentId: {}", paymentId);
//        return paymentQueryService.getById(paymentId);
//    }
//
//    /**
//     * 根据支付单号查询支付单
//     */
//    @ApiOperation("根据支付单号查询支付单")
//    @GetMapping("/code/{code}")
//    public PaymentVO getByCode(@PathVariable String code) {
//        log.info("根据支付单号查询支付单，code: {}", code);
//        return paymentQueryService.getByCode(code);
//    }
//
//    /**
//     * 根据订单ID查询支付单列表
//     */
//    @ApiOperation("根据订单ID查询支付单列表")
//    @GetMapping("/order/{orderId}")
//    public List<PaymentVO> getByOrderId(@PathVariable String orderId) {
//        log.info("根据订单ID查询支付单列表，orderId: {}", orderId);
//        return paymentQueryService.getByOrderId(orderId);
//    }
//
//    /**
//     * 根据经销商ID查询支付单列表
//     */
//    @ApiOperation("根据经销商ID查询支付单列表")
//    @GetMapping("/reseller/{resellerId}")
//    public List<PaymentVO> getByResellerId(@PathVariable String resellerId) {
//        log.info("根据经销商ID查询支付单列表，resellerId: {}", resellerId);
//        return paymentQueryService.getByResellerId(resellerId);
//    }
//
//    /**
//     * 查询退款状态
//     */
//    @ApiOperation("查询退款状态")
//    @GetMapping("/{paymentId}/refunds")
//    public List<PaymentVO> queryRefundStatus(@PathVariable String paymentId) {
//        log.info("查询退款状态，原支付单ID: {}", paymentId);
//        return paymentApplicationService.queryRefundStatus(paymentId);
//    }
//
//    /**
//     * 查询特定流水的可退款金额
//     */
//    @ApiOperation("查询流水可退款金额")
//    @GetMapping("/{paymentId}/transactions/{transactionId}/refundable-amount")
//    public BigDecimal getTransactionRefundableAmount(
//            @PathVariable String paymentId,
//            @PathVariable String transactionId) {
//        log.info("查询流水可退款金额，原支付单ID: {}, 原流水ID: {}", paymentId, transactionId);
//        return paymentApplicationService.getTransactionRefundableAmount(paymentId, transactionId);
//    }
//
//    /**
//     * 验证流水级别的退款金额
//     */
//    @ApiOperation("验证流水退款金额")
//    @GetMapping("/{paymentId}/transactions/{transactionId}/validate-refund/{amount}")
//    public boolean validateTransactionRefundAmount(
//            @PathVariable String paymentId,
//            @PathVariable String transactionId,
//            @PathVariable BigDecimal amount) {
//        log.info("验证流水退款金额，原支付单ID: {}, 原流水ID: {}, 申请金额: {}", paymentId, transactionId, amount);
//        return paymentApplicationService.validateTransactionRefundAmount(paymentId, transactionId, amount);
//    }
//
//    /**
//     * 根据支付单ID查询流水列表
//     */
//    @ApiOperation("根据支付单ID查询流水列表")
//    @GetMapping("/{paymentId}/transactions")
//    public List<PaymentTransactionVO> getTransactionsByPaymentId(@PathVariable String paymentId) {
//        log.info("根据支付单ID查询流水列表，paymentId: {}", paymentId);
//        return paymentQueryService.getTransactionsByPaymentId(paymentId);
//    }
//}