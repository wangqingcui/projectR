package com.bytz.modules.cms.payway.wallet.infrastructure.assembler;

import com.bytz.modules.cms.payway.wallet.domain.model.WalletAggregate;
import com.bytz.modules.cms.payway.wallet.domain.valueobject.WalletTransactionValueObject;
import com.bytz.modules.cms.payway.wallet.infrastructure.entity.WalletEntity;
import com.bytz.modules.cms.payway.wallet.infrastructure.entity.WalletTransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 基础设施层组装器
 * Infrastructure Layer Assembler
 * <p>
 * 使用MapStruct进行Domain对象与Infrastructure实体的转换
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WalletInfrastructureAssembler {

    /**
     * WalletAggregate转WalletEntity（不包含currentTransaction，currency自动设置为CNY）
     */
    @Mapping(target = "currency", constant = "CNY")
    WalletEntity aggregateToEntity(WalletAggregate aggregate);

    /**
     * WalletEntity转WalletAggregate（不包含currentTransaction）
     */
    @Mapping(target = "currentTransaction", ignore = true)
    WalletAggregate entityToAggregate(WalletEntity entity);

    /**
     * WalletAggregate列表转WalletEntity列表
     */
    List<WalletEntity> aggregatesToEntities(List<WalletAggregate> aggregates);

    /**
     * WalletEntity列表转WalletAggregate列表
     */
    List<WalletAggregate> entitiesToAggregates(List<WalletEntity> entities);

    /**
     * WalletTransactionValueObject转WalletTransactionEntity
     */
    WalletTransactionEntity transactionValueObjectToEntity(WalletTransactionValueObject transactionValueObject);

    /**
     * WalletTransactionEntity转WalletTransactionValueObject
     */
    WalletTransactionValueObject transactionEntityToValueObject(WalletTransactionEntity entity);

    /**
     * WalletTransactionEntity列表转WalletTransactionValueObject列表
     */
    List<WalletTransactionValueObject> transactionEntitiesToValueObjects(List<WalletTransactionEntity> entities);

    /**
     * WalletTransactionValueObject列表转WalletTransactionEntity列表
     */
    List<WalletTransactionEntity> transactionValueObjectsToEntities(List<WalletTransactionValueObject> transactionValueObjects);
}