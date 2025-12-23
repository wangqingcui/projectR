package com.bytz.modules.cms.payway.credit.application.assembler;

import com.bytz.modules.cms.payway.credit.application.model.*;
import com.bytz.modules.cms.payway.credit.domain.command.CreateCreditWalletCommand;
import com.bytz.modules.cms.payway.credit.domain.command.CreateTemporaryCreditCommand;
import com.bytz.modules.cms.payway.credit.domain.entity.CreditBillEntity;
import com.bytz.modules.cms.payway.credit.domain.entity.ManagerLog;
import com.bytz.modules.cms.payway.credit.domain.model.CreditWalletAggregate;
import com.bytz.modules.cms.payway.credit.domain.model.TemporaryCreditAggregate;
import com.bytz.modules.cms.payway.credit.infrastructure.entity.CreditBillPO;
import com.bytz.modules.cms.payway.credit.infrastructure.entity.ManagerLogPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 信用模块应用层转换器
 * Credit Module Application Assembler
 * 
 * <p>负责RO/VO ↔ Command/Aggregate转换</p>
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CreditAssembler {
    
    // RO -> Command conversions
    
    /**
     * CreateWalletRO -> CreateCreditWalletCommand
     */
    CreateCreditWalletCommand toCreateCommand(CreateWalletRO ro);

    /**
     * ReceiveTemporaryCreditRO -> CreateTemporaryCreditCommand
     */
    CreateTemporaryCreditCommand toCreateTemporaryCreditCommand(ReceiveTemporaryCreditRO ro);

    /**
     * PO -> 管理日志实体
     */
    ManagerLog poToManagerLog(ManagerLogPO po);

    
    // Aggregate -> VO conversions
    
    /**
     * CreditWalletAggregate -> CreditWalletVO
     */
    CreditWalletVO toVO(CreditWalletAggregate aggregate);
    
    /**
     * TemporaryCreditAggregate -> TemporaryCreditVO
     */
    TemporaryCreditVO toVO(TemporaryCreditAggregate aggregate);
    
    /**
     * CreditBillEntity -> CreditBillVO
     */
    CreditBillVO toVO(CreditBillEntity entity);

    /**
     * CreditBillPO -> CreditBillVO
     */
    @Mapping(target = "canCreateRepayment", expression = "java(org.apache.commons.lang3.StringUtils.isBlank(creditBillPO.getRepaymentPaymentId()))")
    CreditBillVO toVO(CreditBillPO creditBillPO);
    
    // List conversions
    
    /**
     * CreditWalletAggregate列表 -> CreditWalletVO列表
     */
    List<CreditWalletVO> toVOList(List<CreditWalletAggregate> aggregates);
    
    /**
     * TemporaryCreditAggregate列表 -> TemporaryCreditVO列表
     */
    List<TemporaryCreditVO> toTemporaryCreditVOList(List<TemporaryCreditAggregate> aggregates);
    
    /**
     * CreditBillEntity列表 -> CreditBillVO列表
     */
    List<CreditBillVO> toBillVOList(List<CreditBillEntity> entities);
}