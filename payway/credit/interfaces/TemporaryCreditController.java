//package com.bytz.modules.cms.payway.credit.interfaces;
//
//import com.bytz.modules.cms.payway.credit.application.model.*;
//import com.bytz.modules.cms.payway.credit.application.service.CreditApplicationService;
//import com.bytz.modules.cms.payway.credit.application.service.TemporaryCreditQueryService;
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
///**
// * 临时授信控制器
// * Temporary Credit Controller
// */
//@Api(tags = "临时授信管理")
//@Slf4j
//@RestController
//@RequestMapping("/payWay/credit/temporary")
//@RequiredArgsConstructor
//@Validated
//public class TemporaryCreditController {
//
//    private final CreditApplicationService creditApplicationService;
//    private final TemporaryCreditQueryService temporaryCreditQueryService;
//
//    /**
//     * 接收PowerApps临时授信
//     */
//    @ApiOperation("接收PowerApps临时授信")
//    @PostMapping("/receive")
//    public TemporaryCreditVO receiveTemporaryCredit(@Valid @RequestBody ReceiveTemporaryCreditRO ro) {
//        return creditApplicationService.receiveTemporaryCredit(ro);
//    }
//
//    /**
//     * 使用临时授信支付
//     */
//    @ApiOperation("使用临时授信支付")
//    @PostMapping("/pay")
//    public CreditBillVO payWithTemporaryCredit(@Valid @RequestBody PayTemporaryCreditRO ro) {
//        return creditApplicationService.payWithTemporaryCredit(ro);
//    }
//
//    /**
//     * 批量使用临时授信支付
//     */
//    @ApiOperation("批量使用临时授信支付")
//    @PostMapping("/batch-pay")
//    public List<CreditBillVO> batchPayWithTemporaryCredit(@Valid @RequestBody BatchPayTemporaryCreditRO ro) {
//        return creditApplicationService.batchPayWithTemporaryCredit(ro);
//    }
//
//    /**
//     * 根据临时授信ID查询
//     */
//    @ApiOperation("根据临时授信ID查询")
//    @GetMapping("/{temporaryCreditId}")
//    public TemporaryCreditVO getById(@PathVariable String temporaryCreditId) {
//        return temporaryCreditQueryService.getById(temporaryCreditId);
//    }
//
//    /**
//     * 根据经销商ID查询所有可用临时授信
//     */
//    @ApiOperation("根据经销商ID查询所有可用临时授信")
//    @GetMapping("/reseller/{resellerId}")
//    public List<TemporaryCreditVO> getByResellerId(@PathVariable String resellerId) {
//        return temporaryCreditQueryService.getByResellerId(resellerId);
//    }
//}