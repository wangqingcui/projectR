package com.bytz.modules.cms.payway.credit.infrastructure.assembler;

import com.bytz.modules.cms.payway.credit.domain.entity.CreditBillEntity;
import com.bytz.modules.cms.payway.credit.domain.entity.ManagerLog;
import com.bytz.modules.cms.payway.credit.domain.model.CreditWalletAggregate;
import com.bytz.modules.cms.payway.credit.domain.model.TemporaryCreditAggregate;
import com.bytz.modules.cms.payway.credit.infrastructure.entity.CreditBillPO;
import com.bytz.modules.cms.payway.credit.infrastructure.entity.CreditWalletPO;
import com.bytz.modules.cms.payway.credit.infrastructure.entity.ManagerLogPO;
import com.bytz.modules.cms.payway.credit.infrastructure.entity.TemporaryCreditPO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 基础设施层转换器
 * Infrastructure Assembler
 *
 * <p>负责Domain ↔ PO转换</p>
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CreditInfrastructureAssembler {

    // CreditWallet Aggregate <-> PO

    /**
     * 聚合根 -> PO
     */
    CreditWalletPO aggregateToPO(CreditWalletAggregate aggregate);

    /**
     * PO -> 聚合根
     */
    CreditWalletAggregate poToAggregate(CreditWalletPO po);

    /**
     * 将PO数据同步回聚合根（用于持久化后反向同步）
     */
    void updateAggregateFromPO(@MappingTarget CreditWalletAggregate aggregate, CreditWalletPO po);

    /**
     * 聚合根列表 -> PO列表
     */
    List<CreditWalletPO> aggregatesToPOs(List<CreditWalletAggregate> aggregates);

    /**
     * PO列表 -> 聚合根列表
     */
    List<CreditWalletAggregate> posToAggregates(List<CreditWalletPO> pos);

    // TemporaryCredit Aggregate <-> PO

    /**
     * 临时授信聚合根 -> PO
     */
    TemporaryCreditPO temporaryCreditToPO(TemporaryCreditAggregate aggregate);

    /**
     * PO -> 临时授信聚合根
     */
    TemporaryCreditAggregate poToTemporaryCredit(TemporaryCreditPO po);

    /**
     * 将PO数据同步回临时授信聚合根（用于持久化后反向同步）
     */
    void updateTemporaryCreditFromPO(@MappingTarget TemporaryCreditAggregate aggregate, TemporaryCreditPO po);

    /**
     * 临时授信聚合根列表 -> PO列表
     */
    List<TemporaryCreditPO> temporaryCreditsToPOs(List<TemporaryCreditAggregate> aggregates);

    /**
     * PO列表 -> 临时授信聚合根列表
     */
    List<TemporaryCreditAggregate> posToTemporaryCredits(List<TemporaryCreditPO> pos);

    // CreditBill Entity <-> PO

    /**
     * 账单实体 -> PO
     */
    CreditBillPO billEntityToPO(CreditBillEntity entity);

    /**
     * PO -> 账单实体
     */
    CreditBillEntity poToBillEntity(CreditBillPO po);

    /**
     * 将PO数据同步回账单实体（用于持久化后反向同步）
     */
    void updateBillEntityFromPO(@MappingTarget CreditBillEntity entity, CreditBillPO po);

    /**
     * 账单实体列表 -> PO列表
     */
    List<CreditBillPO> billEntitiesToPOs(List<CreditBillEntity> entities);

    /**
     * PO列表 -> 账单实体列表
     */
    List<CreditBillEntity> posToBillEntities(List<CreditBillPO> pos);

    // ManagerLog Entity <-> PO

    /**
     * 管理日志实体 -> PO
     */
    ManagerLogPO managerLogToPO(ManagerLog entity);

    /**
     * PO -> 管理日志实体
     */
    ManagerLog poToManagerLog(ManagerLogPO po);

    /**
     * 管理日志实体列表 -> PO列表
     */
    List<ManagerLogPO> managerLogsToPOs(List<ManagerLog> entities);

    /**
     * PO列表 -> 管理日志实体列表
     */
    List<ManagerLog> posToManagerLogs(List<ManagerLogPO> pos);
}