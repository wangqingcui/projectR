package com.bytz.modules.cms.payway.wallet.application.assembler;

import com.bytz.modules.cms.payway.wallet.application.model.RechargeWalletRO;
import com.bytz.modules.cms.payway.wallet.application.model.WalletTransactionVO;
import com.bytz.modules.cms.payway.wallet.application.model.WalletVO;
import com.bytz.modules.cms.payway.wallet.domain.command.RechargeWalletCommand;
import com.bytz.modules.cms.payway.wallet.domain.model.WalletAggregate;
import com.bytz.modules.cms.payway.wallet.domain.valueobject.WalletTransactionValueObject;
import com.bytz.modules.cms.payway.wallet.infrastructure.entity.WalletEntity;
import com.bytz.modules.cms.payway.wallet.infrastructure.entity.WalletTransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 钱包组装器
 * Wallet Assembler
 * 
 * 使用MapStruct进行RO/VO与Domain对象的转换，以及Entity与VO的直接转换
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WalletAssembler {
    
    /**
     * RechargeWalletRO转RechargeWalletCommand
     */
    RechargeWalletCommand toRechargeCommand(RechargeWalletRO ro);
    
    /**
     * WalletAggregate转WalletVO
     */
    WalletVO toVO(WalletAggregate aggregate);
    
    /**
     * WalletAggregate列表转WalletVO列表
     */
    List<WalletVO> toVOs(List<WalletAggregate> aggregates);
    
    /**
     * WalletEntity直接转WalletVO（不经过聚合根）
     */
    WalletVO entityToVO(WalletEntity entity);
    
    /**
     * WalletEntity列表直接转WalletVO列表
     */
    List<WalletVO> entitiesToVOs(List<WalletEntity> entities);
    
    /**
     * 领域WalletTransactionValueObject转应用层WalletTransactionVO
     */
    WalletTransactionVO toTransactionVO(WalletTransactionValueObject domainValueObject);
    
    /**
     * 领域WalletTransactionValueObject列表转应用层WalletTransactionVO列表
     */
    List<WalletTransactionVO> toTransactionVOs(List<WalletTransactionValueObject> domainValueObjects);
    
    /**
     * WalletTransactionEntity直接转应用层WalletTransactionVO（不经过领域层）
     */
    WalletTransactionVO transactionEntityToVO(WalletTransactionEntity entity);
    
    /**
     * WalletTransactionEntity列表直接转应用层WalletTransactionVO列表
     */
    List<WalletTransactionVO> transactionEntitiesToVOs(List<WalletTransactionEntity> entities);
}