package com.bytz.modules.cms.payway.credit.domain.repository;

import com.bytz.modules.cms.payway.credit.domain.entity.CreditBillEntity;
import com.bytz.modules.cms.payway.credit.domain.model.TemporaryCreditAggregate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 临时授信仓储接口
 * Temporary Credit Repository Interface
 *
 * <p>负责聚合根的持久化。</p>
 * <p>实现类：TemporaryCreditRepositoryImpl (infrastructure层)</p>
 */
public interface ITemporaryCreditRepository {

    /**
     * 新建临时授信，批量持久化newBills
     *
     * @param aggregate 临时授信聚合根
     */
    void insert(TemporaryCreditAggregate aggregate);

    /**
     * 更新临时授信，批量持久化newBills，更新已还款账单状态
     *
     * @param aggregate 临时授信聚合根
     */
    void update(TemporaryCreditAggregate aggregate);

    /**
     * 按ID查询临时授信，支持选择性加载unpaidBills
     * 按业务需求选择性加载：不加载所有未还款账单，而是根据业务场景决定加载策略
     * 加载策略：
     * - loadUnpaidBills=false：简单查询、状态变更场景（不加载账单）
     * - loadUnpaidBills=true：一般不使用，还款操作应使用findBillById只加载所需账单
     * 注意：逾期检测应直接使用findOverdueBills查询逾期账单
     *
     * @param id              临时授信ID
     * @param loadUnpaidBills 是否加载未还款账单
     * @return 临时授信聚合根（可能为空）
     */
    Optional<TemporaryCreditAggregate> findById(String id, boolean loadUnpaidBills);

    /**
     * 按账单ID查询单条账单（用于还款操作等场景）
     * 按业务需求选择性加载：只加载本次还款所需的账单，而非加载临时授信所有未还款账单
     *
     * @param temporaryCreditId 临时授信ID
     * @param billId            账单ID
     * @return 账单实体（可能为空）
     */
    Optional<CreditBillEntity> findBillById(String temporaryCreditId, String billId);

    /**
     * 按账单ID列表查询多条账单（用于批量还款等场景）
     * 按业务需求选择性加载：一次可能有多个账单需要变动，只加载本次操作所需的账单
     * 用例：批量还款、批量状态更新等需要同时处理多个账单的场景
     *
     * @param temporaryCreditId 临时授信ID
     * @param billIds           账单ID列表
     * @return 账单实体列表
     */
    List<CreditBillEntity> findBillsByIds(String temporaryCreditId, List<String> billIds);

    /**
     * 按审批ID查询临时授信（用于幂等性检查）
     *
     * @param approvalId PowerApps审批ID
     * @return 临时授信聚合根（可能为空）
     */
    Optional<TemporaryCreditAggregate> findByApprovalId(String approvalId);

    /**
     * 查询经销商的有效临时授信（APPROVED或IN_USE状态）
     *
     * @param resellerId 经销商ID
     * @return 临时授信列表
     */
    List<TemporaryCreditAggregate> findActiveByResellerId(String resellerId);

    /**
     * 查询过期的临时授信（用于过期检测定时任务）
     *
     * @param currentDate 当前日期
     * @return 过期的临时授信列表
     */
    List<TemporaryCreditAggregate> findExpiredCredits(LocalDate currentDate);

    /**
     * 直接查询临时授信的逾期账单（用于逾期检测定时任务）
     * 逾期检测不通过聚合根：直接通过仓储查询 dueDate < currentDate 且 repaymentStatus != REPAID 的账单
     * 返回账单列表，包含temporaryCreditId信息
     *
     * @param currentDate 当前日期
     * @return 逾期账单列表
     */
    List<CreditBillEntity> findOverdueBills(LocalDate currentDate);

    /**
     * 删除临时授信（逻辑删除）
     *
     * @param id 临时授信ID
     */
    void deleteById(String id);

    /**
     * 更新经销商临时信用的钱包Id
     */
    void updateWalletId(String resellerId, String walletId);
}
