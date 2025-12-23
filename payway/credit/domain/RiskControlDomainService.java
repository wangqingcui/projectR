package com.bytz.modules.cms.payway.credit.domain;

import com.bytz.modules.cms.payway.credit.domain.command.FreezeWalletCommand;
import com.bytz.modules.cms.payway.credit.domain.entity.CreditBillEntity;
import com.bytz.modules.cms.payway.credit.domain.model.CreditWalletAggregate;
import com.bytz.modules.cms.payway.credit.domain.repository.ICreditWalletRepository;
import com.bytz.modules.cms.payway.credit.shared.event.WalletFrozenEvent;
import com.bytz.modules.cms.payway.credit.shared.exception.CreditWalletErrorCode;
import com.bytz.modules.cms.payway.credit.shared.exception.CreditWalletException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 风控领域服务
 * Risk Control Domain Service
 * 
 * <p>负责逾期检测和自动冻结处理。</p>
 * <p>用例来源：UC-CW-030, UC-CW-031</p>
 * <p>需求来源：T20, T21</p>
 * <p>职责：
 *   - 检测逾期账单（UNPAID或REPAYING状态，dueDate < currentDate）
 *   - 自动冻结有逾期账单的钱包
 *   - 触发逾期通知
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskControlDomainService {
    
    private final ICreditWalletRepository creditWalletRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 逾期信息DTO
     */
    public static class OverdueInfo {
        private final String walletId;
        private final String resellerId;
        private final List<CreditBillEntity> overdueBills;
        private final BigDecimal totalOverdueAmount;
        
        public OverdueInfo(String walletId, String resellerId, List<CreditBillEntity> overdueBills) {
            this.walletId = walletId;
            this.resellerId = resellerId;
            this.overdueBills = overdueBills;
            this.totalOverdueAmount = overdueBills.stream()
                    .map(CreditBillEntity::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        
        public String getWalletId() {
            return walletId;
        }
        
        public String getResellerId() {
            return resellerId;
        }
        
        public List<CreditBillEntity> getOverdueBills() {
            return overdueBills;
        }
        
        public BigDecimal getTotalOverdueAmount() {
            return totalOverdueAmount;
        }
    }
    
    /**
     * 检测逾期账单
     * 用例来源：UC-CW-030
     * 需求来源：T20
     * 
     * @return 逾期信息列表
     */
    public List<OverdueInfo> detectOverdueBills() {
        // TODO: Implement detectOverdueBills orchestration logic
        // Requirements:
        // - 直接通过仓储查询逾期账单（UNPAID或REPAYING状态，dueDate < currentDate）
        // - 按钱包ID分组，统计逾期金额
        
        log.info("Detecting overdue bills");
        
        LocalDate currentDate = LocalDate.now();
        List<CreditBillEntity> overdueBills = creditWalletRepository.findOverdueBills(currentDate);
        
        // 按钱包ID分组
        Map<String, List<CreditBillEntity>> billsByWallet = overdueBills.stream()
                .collect(Collectors.groupingBy(CreditBillEntity::getCreditWalletId));
        
        List<OverdueInfo> overdueInfos = billsByWallet.entrySet().stream()
                .map(entry -> {
                    String walletId = entry.getKey();
                    List<CreditBillEntity> bills = entry.getValue();
                    // 获取resellerId（从钱包获取）
                    String resellerId = creditWalletRepository.findById(walletId, false)
                            .map(CreditWalletAggregate::getResellerId)
                            .orElse(null);
                    return new OverdueInfo(walletId, resellerId, bills);
                })
                .collect(Collectors.toList());
        
        log.info("Detected {} wallets with overdue bills", overdueInfos.size());
        return overdueInfos;
    }
    
    /**
     * 自动冻结逾期钱包
     * 用例来源：UC-CW-031
     * 需求来源：T21
     * 
     * @param overdueInfos 逾期信息列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void autoFreezeOverdueWallets(List<OverdueInfo> overdueInfos) {
        // TODO: Implement autoFreezeOverdueWallets orchestration logic
        // Requirements:
        // - 对有逾期账单的钱包调用freeze方法
        // - 记录冻结日志（含逾期账单详情）
        // - 发布WalletFrozenEvent
        // - 触发逾期通知
        
        log.info("Auto-freezing {} overdue wallets", overdueInfos.size());
        
        for (OverdueInfo overdueInfo : overdueInfos) {
            try {
                CreditWalletAggregate wallet = creditWalletRepository.findById(overdueInfo.getWalletId(), false)
                        .orElseThrow(() -> new CreditWalletException(CreditWalletErrorCode.WALLET_NOT_FOUND));
                
                // 已经冻结的钱包跳过
                if (Boolean.TRUE.equals(wallet.getFrozen())) {
                    log.debug("Wallet already frozen: {}", overdueInfo.getWalletId());
                    continue;
                }
                
                // 冻结钱包
                List<String> overdueBillCodes = overdueInfo.getOverdueBills().stream()
                        .map(CreditBillEntity::getCode)
                        .collect(Collectors.toList());
                
                FreezeWalletCommand command = FreezeWalletCommand.builder()
                        .operator("SYSTEM")
                        .reason("系统自动冻结：检测到逾期账单")
                        .build();
                
                wallet.freeze(command);
                
                // 补充afterState中的逾期信息
                if (!wallet.getNewManagerLogs().isEmpty()) {
                    com.bytz.modules.cms.payway.credit.domain.entity.ManagerLog latestLog = 
                            wallet.getNewManagerLogs().get(wallet.getNewManagerLogs().size() - 1);
                    Map<String, Object> afterState = latestLog.getAfterState();
                    if (afterState == null) {
                        afterState = new HashMap<>();
                        latestLog.setAfterState(afterState);
                    }
                    afterState.put("overdueBills", overdueBillCodes);
                    afterState.put("overdueAmount", overdueInfo.getTotalOverdueAmount());
                }
                
                creditWalletRepository.update(wallet);

                // 发布WalletFrozenEvent
                eventPublisher.publishEvent(WalletFrozenEvent.builder()
                        .walletId(wallet.getId())
                        .resellerId(wallet.getResellerId())
                        .frozenAt(LocalDateTime.now())
                        .reason("系统自动冻结：检测到逾期账单")
                        .operator("SYSTEM")
                        .build());

                log.info("Wallet auto-frozen due to overdue: {}", overdueInfo.getWalletId());
            } catch (Exception e) {
                log.error("Failed to auto-freeze wallet: {}", overdueInfo.getWalletId(), e);
            }
        }
        
        log.info("Auto-freeze overdue wallets completed");
    }
}