package com.bytz.modules.cms.payway.credit.domain;

import com.bytz.modules.cms.payway.credit.domain.command.CompleteRepaymentCommand;
import com.bytz.modules.cms.payway.credit.domain.command.CreateCreditWalletCommand;
import com.bytz.modules.cms.payway.credit.domain.model.CreditWalletAggregate;
import com.bytz.modules.cms.payway.credit.domain.repository.ICreditWalletRepository;
import com.bytz.modules.cms.payway.credit.shared.exception.CreditWalletErrorCode;
import com.bytz.modules.cms.payway.credit.shared.exception.CreditWalletException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.math.BigDecimal;

/**
 * 信用钱包领域服务
 * Credit Wallet Domain Service
 * 
 * <p>负责钱包创建流程和跨聚合的业务逻辑。</p>
 * <p>用例来源：UC-CW-001</p>
 * <p>需求来源：T01</p>
 * <p>职责：
 *   - 创建信用钱包：通过CreateCreditWalletCommand命令对象创建CreditWalletAggregate聚合根
 *   - 监听经销商创建事件（ResellerCreatedEvent）并触发钱包创建
 *   - 协调钱包创建流程的持久化
 *   - 提供钱包创建的统一入口
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreditWalletDomainService {
    
    private final ICreditWalletRepository creditWalletRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 创建信用钱包
     * 监听ResellerCreatedEvent事件触发
     * 
     * @param command 创建钱包命令
     * @return 创建的钱包聚合根
     */
    @Transactional(rollbackFor = Exception.class)
    public CreditWalletAggregate createWallet(@Valid CreateCreditWalletCommand command) {
        // Requirements:
        // - 验证经销商ID有效性
        // - 检查经销商是否已有钱包（幂等处理）
        // - 设置默认值：enabled=false, frozen=false, usedLimit=0
        // - 调用仓储接口持久化钱包
        
        log.info("Creating credit wallet for reseller: {}", command.getResellerId());
        
        // 检查经销商是否已有钱包（幂等处理）
        creditWalletRepository.findByResellerId(command.getResellerId(), false)
                .ifPresent(w -> {
                    throw new CreditWalletException(CreditWalletErrorCode.WALLET_ALREADY_EXISTS);
                });
        
        // 设置默认值
        BigDecimal totalLimit = command.getTotalLimit() != null ? command.getTotalLimit() : BigDecimal.ZERO;
        Integer termDays = command.getTermDays() != null ? command.getTermDays() : 0;
        
        // 创建钱包聚合根
        CreditWalletAggregate wallet = CreditWalletAggregate.builder()
                .resellerId(command.getResellerId())
                .totalLimit(totalLimit)
                .availableLimit(totalLimit)
                .usedLimit(BigDecimal.ZERO)
                .termDays(termDays)
                .enabled(false)  // 默认false
                .frozen(false)
                .prepaymentEnabled(command.getPrepaymentEnabled()) // 必填
                .currency("CNY")
                .createBy(command.getCreateBy())
                .createByName(command.getCreateByName())
                .build();
        
        // 持久化
        creditWalletRepository.insert(wallet);

        
        log.info("Credit wallet created successfully for reseller: {}", command.getResellerId());
        return wallet;
    }


    /**
     * 完成信用还款
     * 处理信用账单的还款完成逻辑
     * 
     * @param command 还款完成命令，包含账单ID和还款时间
     * @throws CreditWalletException 当账单不存在或处理失败时抛出
     * 
     * 业务逻辑：
     * 1. 根据账单ID查询关联的信用钱包聚合根
     * 2. 调用聚合根的还款完成方法，更新内部状态
     * 3. 持久化更新后的聚合根状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeRepayment(CompleteRepaymentCommand command) {
        log.info("[信用钱包领域服务] 开始处理信用还款完成 - 账单ID: {}, 还款时间: {}", 
                command.getBillId(), command.getRepaymentTime());
        
        // 1. 根据账单ID查询关联的信用钱包聚合根
        log.debug("[信用钱包领域服务] 查询关联的信用钱包聚合根 - 账单ID: {}", command.getBillId());
        CreditWalletAggregate creditWalletAggregate = creditWalletRepository.findByBillId(command.getBillId())
                .orElseThrow(() -> {
                    log.error("[信用钱包领域服务] 未找到关联的信用钱包 - 账单ID: {}", command.getBillId());
                    return new CreditWalletException(CreditWalletErrorCode.TRANSACTION_NOT_FOUND);
                });
        log.debug("[信用钱包领域服务] 查询到信用钱包聚合根 - 钱包ID: {}, 账单ID: {}", 
                creditWalletAggregate.getId(), command.getBillId());

        // 2. 调用聚合根的还款完成方法，更新内部状态
        log.debug("[信用钱包领域服务] 调用聚合根完成还款 - 钱包ID: {}, 账单ID: {}", 
                creditWalletAggregate.getId(), command.getBillId());
        creditWalletAggregate.completeRepayment(command);
        log.debug("[信用钱包领域服务] 聚合根还款完成 - 钱包ID: {}, 账单ID: {}", 
                creditWalletAggregate.getId(), command.getBillId());

        // 3. 持久化更新后的聚合根状态
        log.debug("[信用钱包领域服务] 持久化更新后的信用钱包状态 - 钱包ID: {}, 账单ID: {}", 
                creditWalletAggregate.getId(), command.getBillId());
        creditWalletRepository.update(creditWalletAggregate);
        log.info("[信用钱包领域服务] 信用还款完成处理成功 - 账单ID: {}", command.getBillId());
    }
}