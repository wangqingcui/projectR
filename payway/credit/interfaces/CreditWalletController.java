//package com.bytz.modules.cms.payway.credit.interfaces;
//
//import com.bytz.modules.cms.payway.credit.application.model.*;
//import com.bytz.modules.cms.payway.credit.application.service.CreditApplicationService;
//import com.bytz.modules.cms.payway.credit.application.service.CreditWalletQueryService;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//
//import javax.validation.Valid;
//import java.util.List;
//
/// **
// * 信用钱包控制器
// * Credit Wallet Controller
// */
//@Api(tags = "信用钱包管理")
//@Slf4j
//@RestController
//@RequestMapping("/payWay/credit/wallet")
//@RequiredArgsConstructor
//@Validated
//public class CreditWalletController {
//
//    private final CreditApplicationService creditApplicationService;
//    private final CreditWalletQueryService creditWalletQueryService;
//
//    /**
//     * 创建信用钱包
//     */
//    @ApiOperation("创建信用钱包")
//    @PostMapping("/create")
//    public CreditWalletVO createWallet(@Valid @RequestBody CreateWalletRO ro) {
//        return creditApplicationService.createWallet(ro);
//    }
//
//    /**
//     * 信用支付
//     */
//    @ApiOperation("信用支付")
//    @PostMapping("/pay")
//    public CreditBillVO creditPay(@Valid @RequestBody PayCreditRO ro) {
//        return creditApplicationService.creditPay(ro);
//    }
//
//    /**
//     * 批量信用支付
//     */
//    @ApiOperation("批量信用支付")
//    @PostMapping("/batch-pay")
//    public List<CreditBillVO> batchCreditPay(@Valid @RequestBody BatchPayCreditRO ro) {
//        return creditApplicationService.batchCreditPay(ro);
//    }
//
//    /**
//     * 根据经销商ID查询钱包
//     */
//    @ApiOperation("根据经销商ID查询钱包")
//    @GetMapping("/reseller/{resellerId}")
//    public CreditWalletVO getByResellerId(@PathVariable String resellerId) {
//        return creditWalletQueryService.getByResellerId(resellerId);
//    }
//
//    /**
//     * 根据钱包ID查询钱包
//     */
//    @ApiOperation("根据钱包ID查询钱包")
//    @GetMapping("/{walletId}")
//    public CreditWalletVO getById(@PathVariable String walletId) {
//        return creditWalletQueryService.getById(walletId);
//    }
//
//    // ==================== 管理操作 ====================
//
//    /**
//     * 冻结钱包
//     */
//    @ApiOperation("冻结钱包")
//    @PostMapping("/freeze")
//    public void freezeWallet(@Valid @RequestBody FreezeWalletRO ro) {
//        creditApplicationService.freezeWallet(ro);
//    }
//
//    /**
//     * 解冻钱包
//     */
//    @ApiOperation("解冻钱包")
//    @PostMapping("/unfreeze")
//    public void unfreezeWallet(@Valid @RequestBody UnfreezeWalletRO ro) {
//        creditApplicationService.unfreezeWallet(ro);
//    }
//
//    /**
//     * 启用钱包
//     */
//    @ApiOperation("启用钱包")
//    @PostMapping("/enable")
//    public void enableWallet(@Valid @RequestBody EnableWalletRO ro) {
//        creditApplicationService.enableWallet(ro);
//    }
//
//    /**
//     * 停用钱包
//     */
//    @ApiOperation("停用钱包")
//    @PostMapping("/disable")
//    public void disableWallet(@Valid @RequestBody DisableWalletRO ro) {
//        creditApplicationService.disableWallet(ro);
//    }
//
//    /**
//     * 调整信用额度
//     */
//    @ApiOperation("调整信用额度")
//    @PostMapping("/adjust-limit")
//    public void adjustCreditLimit(@Valid @RequestBody AdjustCreditLimitRO ro) {
//        creditApplicationService.adjustCreditLimit(ro);
//    }
//
//    /**
//     * 更新账期天数
//     */
//    @ApiOperation("更新账期天数")
//    @PostMapping("/update-term-days")
//    public void updateTermDays(@Valid @RequestBody UpdateTermDaysRO ro) {
//        creditApplicationService.updateTermDays(ro);
//    }
//
//    // ==================== 还款操作 ====================
//
//    /**
//     * 发起还款
//     */
//    @ApiOperation("发起还款")
//    @PostMapping("/initiate-repayment")
//    public void initiateRepayment(@Valid @RequestBody InitiateRepaymentRO ro) {
//        creditApplicationService.initiateRepayment(ro);
//    }
//}