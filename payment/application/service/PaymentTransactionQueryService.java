package com.bytz.modules.cms.payment.application.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytz.common.constant.BooleanConstant;
import com.bytz.common.exception.BytzBootException;
import com.bytz.common.system.query.MPJLambdaWrapperEx;
import com.bytz.modules.cms.common.util.ExcelUtil;
import com.bytz.modules.cms.order.constant.BillConstants;
import com.bytz.modules.cms.order.entity.Order;
import com.bytz.modules.cms.payment.application.model.PaymentTransactionListExcel;
import com.bytz.modules.cms.payment.application.model.PaymentTransactionListVO;
import com.bytz.modules.cms.payment.domain.enums.TransactionStatus;
import com.bytz.modules.cms.payment.infrastructure.entity.PaymentPO;
import com.bytz.modules.cms.payment.infrastructure.entity.PaymentTransactionPO;
import com.bytz.modules.cms.payment.infrastructure.mapper.PaymentTransactionMapper;
import com.bytz.modules.cms.reseller.entity.Reseller;
import com.bytz.modules.system.message.MsgSendService;
import com.bytz.modules.system.message.build.BuildMessageContext;
import com.bytz.modules.system.message.build.MessageBuild;
import com.bytz.modules.system.message.constant.MsgTemplateCode;
import com.bytz.modules.system.message.model.MsgContext;
import com.bytz.modules.system.model.SmsFileModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

/**
 * 支付流水查询服务
 * Payment Transaction Query Service
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentTransactionQueryService extends ServiceImpl<PaymentTransactionMapper, PaymentTransactionPO> {

    @Autowired
    private MessageBuild messageBuild;
    @Autowired
    private MsgSendService msgSendService;
    @Autowired
    private BuildMessageContext buildMessageContext;

    /**
     * 分页查询支付流水列表
     *
     * @param page         分页参数
     * @param queryWrapper 查询条件
     * @return 分页支付流水列表
     */
    public IPage<PaymentTransactionListVO> queryTransactionPage(Page<PaymentTransactionPO> page, MPJLambdaWrapperEx<PaymentTransactionPO> queryWrapper, String turnoverStatus) {
        log.debug("分页查询支付流水列表，page: {}, queryWrapper: {}", page, queryWrapper);

        if (StringUtils.isNotBlank(turnoverStatus)) {
            queryWrapper.in(PaymentTransactionPO::getTransactionStatus, Arrays.asList(turnoverStatus.split(",")));
        }
        buildWrapper(queryWrapper);

        return this.baseMapper.selectJoinPage(page, PaymentTransactionListVO.class, queryWrapper);
    }

    /**
     * 构建查询条件
     *
     * @param queryWrapper 查询条件
     */
    private void buildWrapper(MPJLambdaWrapperEx<PaymentTransactionPO> queryWrapper) {
        queryWrapper
                .selectAll(PaymentTransactionPO.class)
                // 订单id
                .selectAs(PaymentPO::getOrderId, PaymentTransactionListVO::getOrderId)
                // 订单类别
                .selectAs(Order::getOrderType, PaymentTransactionListVO::getOrderType)
                // 经销商Id
                .selectAs(PaymentPO::getResellerId, PaymentTransactionListVO::getResellerId)
                // 经销商名字
                .selectAs(Reseller::getResellerName, PaymentTransactionListVO::getResellerName)
                // 订单合同号
                .selectAs(Order::getContractNumber, PaymentTransactionListVO::getContractNumber)
                // 支付类型
                .selectAs(PaymentPO::getPaymentType, PaymentTransactionListVO::getPaymentType)
                // 客户编码
                .selectAs(Order::getDemanderNumber, PaymentTransactionListVO::getDemanderNumber)
                // 客户名称
                .selectAs(Order::getDemanderName, PaymentTransactionListVO::getDemanderName)
                // 销售外部编号
                .selectAs(Order::getSalesId, PaymentTransactionListVO::getSalesExternalId)
                // 销售名字
                .selectAs(Order::getSalesName, PaymentTransactionListVO::getSalesName)
                //支付单Id
                .selectAs(PaymentPO::getId, PaymentTransactionListVO::getPaymentId)
                //TODO差一个银联的付款银行
                .leftJoin(PaymentPO.class, left -> left.eq(PaymentTransactionPO::getPaymentId, PaymentPO::getId).eq(PaymentPO::getDelFlag, BooleanConstant.INT_FALSE))
                .leftJoin(Order.class, left -> left.eq(PaymentPO::getOrderId, Order::getId).eq(Order::getDelFlag, BooleanConstant.INT_FALSE))
                .leftJoin(Reseller.class, left -> left.eq(PaymentPO::getResellerId, Reseller::getId).eq(Reseller::getDelFlag, BooleanConstant.INT_FALSE))
                .orderByDesc(PaymentTransactionPO::getCreateTime)
                .orderByDesc(PaymentTransactionPO::getId);
    }

    /**
     * 根据id查询支付流水
     *
     * @param id 支付流水id
     * @return 支付流水
     */
    public PaymentTransactionListVO queryById(String id) {
        MPJLambdaWrapperEx<PaymentTransactionPO> queryWrapper = new MPJLambdaWrapperEx<>();
        buildWrapper(queryWrapper);
        queryWrapper.eq(PaymentTransactionPO::getId, id);
        return this.baseMapper.selectJoinOne(PaymentTransactionListVO.class, queryWrapper);
    }

    /**
     * 发送日结账单
     **/
    public void sendDailyBill(@NotNull LocalDateTime date) {
        MPJLambdaWrapperEx<PaymentTransactionPO> queryWrapper = new MPJLambdaWrapperEx<>();
        buildWrapper(queryWrapper);
        //构建查询时间的范围
        LocalDateTime startTime = date.toLocalDate().atTime(LocalTime.MIN);
        LocalDateTime endTime = date.toLocalDate().atTime(LocalTime.MAX);

        List<PaymentTransactionListVO> transactionListVoS = baseMapper.selectJoinList(PaymentTransactionListVO.class, queryWrapper
                .eq(PaymentTransactionPO::getTransactionStatus, TransactionStatus.SUCCESS)
                .between(PaymentTransactionPO::getCompletedTime, startTime, endTime));

        log.debug("查询日结账单，date: {}, transactionListVoS: {}", date, transactionListVoS);
        //转换成excel
        List<PaymentTransactionListExcel> listExcels = BeanUtil.copyToList(transactionListVoS, PaymentTransactionListExcel.class);
        String strDate = LocalDateTimeUtil.format(date, "yyyy年MM月dd日");
        SmsFileModel smsFileModel = messageBuild.buildFile(String.format(BillConstants.Email.ATTACH_NAME, strDate));
        try {
            ExcelUtil.exportToOutputStream(BillConstants.Email.ATTACH_SHEET_NAME, PaymentTransactionListExcel.class, listExcels, FileUtil.getOutputStream(smsFileModel.getFile()));

            MsgContext msgContext = buildMessageContext.build();
            msgSendService.sendWithoutTry(msgContext, MsgTemplateCode.OtherNode.SEND_BILL_NODE, map -> {
                map.put("billDate", strDate);
                map.put("date", date);
            }, smsFileModel);
        } catch (Exception e) {
            log.error("发送邮件失败", e);
            throw new BytzBootException(e);
        }
    }

    /**
     * 导出账单
     *
     * @param response        响应
     * @param lambdaWrapperEx 查询参数
     * @param ids             ids
     * @throws Exception 异常
     */
    public void exportXls(HttpServletResponse response, MPJLambdaWrapperEx<PaymentTransactionPO> lambdaWrapperEx, String ids) throws Exception {
        if (StringUtils.isNotBlank(ids)) {
            lambdaWrapperEx.in(PaymentTransactionPO::getId, Arrays.asList(StringUtils.split(ids, ",")));
        }
        buildWrapper(lambdaWrapperEx);
        List<PaymentTransactionListVO> transactionListVoS = baseMapper.selectJoinList(PaymentTransactionListVO.class, lambdaWrapperEx);
        String format = LocalDateTimeUtil.format(LocalDateTime.now(), "yyyy年MM月dd日");
        List<PaymentTransactionListExcel> listExcels = BeanUtil.copyToList(transactionListVoS, PaymentTransactionListExcel.class);

        ExcelUtil.export(
                String.format(BillConstants.Email.ATTACH_NAME, format),
                PaymentTransactionListExcel.class,
                response,
                listExcels
        );
    }
}
