package com.bytz.modules.cms.payway.credit.domain.repository;

import com.bytz.modules.cms.payway.credit.domain.entity.CreditBillEntity;
import com.bytz.modules.cms.payway.credit.domain.model.CreditWalletAggregate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 信用钱包仓储接口
 * Credit Wallet Repository Interface
 * 
 * <p>负责聚合根的持久化。</p>
 * <p>实现类：CreditWalletRepositoryImpl (infrastructure层)</p>
 */
public interface ICreditWalletRepository {
    
    /**
     * 新建钱包，批量持久化newBills和newManagerLogs。
     * 事务边界：钱包主记录 + newBills + newManagerLogs
     *
     * @param aggregate 信用钱包聚合根
     */
    void insert(CreditWalletAggregate aggregate);
    
    /**
     * 更新钱包，批量持久化newBills和newManagerLogs，更新已还款账单状态。
     * 事务边界：钱包主记录 + newBills + newManagerLogs + unpaidBills中已还款账单
     *
     * @param aggregate 信用钱包聚合根
     */
    void update(CreditWalletAggregate aggregate);
    
    /**
     * 按ID查询钱包，支持选择性加载unpaidBills。
     * 按业务需求选择性加载：不加载所有未还款账单，而是根据业务场景决定加载策略
     * 加载策略：
     *   - loadUnpaidBills=false：简单查询、状态变更场景（不加载账单）
     *   - loadUnpaidBills=true：一般不使用，还款操作应使用findBillById只加载所需账单
     * 注意：逾期检测应直接使用findOverdueBills查询逾期账单
     *
     * @param id 钱包ID
     * @param loadUnpaidBills 是否加载未还款账单
     * @return 信用钱包聚合根（可能为空）
     */
    Optional<CreditWalletAggregate> findById(String id, boolean loadUnpaidBills);
    
    /**
     * 按经销商ID查询钱包
     *
     * @param resellerId 经销商ID
     * @param loadUnpaidBills 是否加载未还款账单
     * @return 信用钱包聚合根（可能为空）
     */
    Optional<CreditWalletAggregate> findByResellerId(String resellerId, boolean loadUnpaidBills);



    /**
     * 按账单ID查询单条账单,构建信用支付对象
     * 按业务需求选择性加载：只加载本次还款所需的账单，而非加载钱包所有未还款账单
     *
     * @param billId 账单ID
     * @return 账单实体（可能为空）
     */
    Optional<CreditWalletAggregate> findByBillId( String billId);



    /**
     * 按账单ID列表查询多条账单,构建信用支付对象
     *
     * @param billIds 账单ID列表
     * @return 账单实体列表
     */
    Optional<CreditWalletAggregate> findByBillIds(List<String> billIds);

    /**
     * 按账单ID查询单条账单（用于还款操作等场景）
     * 按业务需求选择性加载：只加载本次还款所需的账单，而非加载钱包所有未还款账单
     *
     * @param walletId 钱包ID
     * @param billId 账单ID
     * @return 账单实体（可能为空）
     */
    Optional<CreditBillEntity> findBillById(String walletId, String billId);
    
    /**
     * 按账单ID列表查询多条账单（用于批量还款等场景）
     * 按业务需求选择性加载：一次可能有多个账单需要变动，只加载本次操作所需的账单
     * 用例：批量还款、批量状态更新等需要同时处理多个账单的场景
     *
     * @param walletId 钱包ID
     * @param billIds 账单ID列表
     * @return 账单实体列表
     */
    List<CreditBillEntity> findBillsByIds(String walletId, List<String> billIds);
    
    /**
     * 直接查询逾期账单（用于逾期检测定时任务）
     * 逾期检测不通过聚合根：直接通过仓储查询 dueDate < currentDate 且 repaymentStatus != REPAID 的账单
     * 返回账单列表，包含walletId信息，用于后续冻结钱包操作
     *
     * @param currentDate 当前日期
     * @return 逾期账单列表
     */
    List<CreditBillEntity> findOverdueBills(LocalDate currentDate);
    
    /**
     * 查询所有启用且未冻结的钱包
     *
     * @return 钱包列表
     */
    List<CreditWalletAggregate> findActiveWallets();
    
    /**
     * 删除信用钱包（逻辑删除）
     *
     * @param id 钱包ID
     */
    void deleteById(String id);
}