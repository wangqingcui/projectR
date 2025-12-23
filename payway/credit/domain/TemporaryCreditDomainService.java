package com.bytz.modules.cms.payway.credit.domain;

import com.bytz.modules.cms.payway.credit.domain.command.CreateTemporaryCreditCommand;
import com.bytz.modules.cms.payway.credit.domain.enums.TemporaryCreditStatus;
import com.bytz.modules.cms.payway.credit.domain.model.TemporaryCreditAggregate;
import com.bytz.modules.cms.payway.credit.domain.repository.ICreditWalletRepository;
import com.bytz.modules.cms.payway.credit.domain.repository.ITemporaryCreditRepository;
import com.bytz.modules.cms.payway.credit.shared.event.TemporaryCreditExpiredEvent;
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
import java.util.List;

/**
 * 临时授信领域服务
 * Temporary Credit Domain Service
 *
 * <p>负责临时授信创建流程。</p>
 * <p>用例来源：UC-CW-006, UC-CW-007</p>
 * <p>需求来源：T08, T10</p>
 * <p>职责：
 * - 创建临时授信：接收PowerApps审批信息，创建TemporaryCreditAggregate聚合根
 * - 验证approvalId唯一性（幂等处理）
 * - 检测并标记过期的临时授信
 * </p>
 * <p>关联关系：
 * - 通过resellerId与CreditWallet聚合根松耦合关联
 * - 创建时不校验钱包状态：临时授信创建不要求钱包存在或启用
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemporaryCreditDomainService {

    private final ITemporaryCreditRepository temporaryCreditRepository;
    private final ICreditWalletRepository creditWalletRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 接收PowerApps审批通过的临时授信（UC-CW-006）
     *
     * @param command 创建临时授信命令
     * @return 创建的临时授信聚合根
     */
    @Transactional(rollbackFor = Exception.class)
    public TemporaryCreditAggregate createTemporaryCredit(CreateTemporaryCreditCommand command) {
        // TODO: Implement createTemporaryCredit business logic
        // Requirements:
        // - 验证approvalId唯一性（幂等性处理）
        // - 创建TemporaryCredit聚合根（状态=APPROVED）
        // - 持久化临时授信聚合根
        // - 发布TemporaryCreditReceivedEvent

        log.info("Creating temporary credit for reseller: {}, approvalId: {}",
                command.getResellerId(), command.getApprovalId());

        // 验证approvalId唯一性（幂等性处理）
        temporaryCreditRepository.findByApprovalId(command.getApprovalId())
                .ifPresent(tc -> {
                    throw new CreditWalletException(CreditWalletErrorCode.APPROVAL_ID_DUPLICATE);
                });

        // 获取关联的信用钱包ID（如果存在）
        String creditWalletId = creditWalletRepository.findByResellerId(command.getResellerId(), false)
                .map(wallet -> wallet.getId())
                .orElse(null);

        // 创建临时授信聚合根
        TemporaryCreditAggregate temporaryCredit = TemporaryCreditAggregate.builder()
                .resellerId(command.getResellerId())
                .creditWalletId(creditWalletId)
                .approvalId(command.getApprovalId())
                .approvalTime(command.getApprovalTime())
                .totalAmount(command.getTotalAmount())
                .usedAmount(BigDecimal.ZERO)
                .remainingAmount(command.getTotalAmount())
                .repaidAmount(BigDecimal.ZERO)
                .expiryDate(command.getExpiryDate())
                .status(TemporaryCreditStatus.APPROVED)
                .remark(command.getRemark())
                .currency("CNY")
                .build();

        // 持久化
        temporaryCreditRepository.insert(temporaryCredit);

        // TODO: 发布TemporaryCreditReceivedEvent

        log.info("Temporary credit created successfully for reseller: {}", command.getResellerId());
        return temporaryCredit;
    }

    /**
     * 检测并标记已过期的临时授信（UC-CW-007）
     * 由定时任务调用
     */
    @Transactional(rollbackFor = Exception.class)
    public void detectExpiredTemporaryCredits() {
        // TODO: Implement detectExpiredTemporaryCredits orchestration logic

        log.info("Detecting expired temporary credits");

        LocalDate currentDate = LocalDate.now();
        List<TemporaryCreditAggregate> expiredCredits = temporaryCreditRepository.findExpiredCredits(currentDate);

        for (TemporaryCreditAggregate tc : expiredCredits) {

            if (tc.isExpired()) {
                tc.expire();
                temporaryCreditRepository.update(tc);
                log.info("Temporary credit marked as expired: {}", tc.getId());
                
                // 发布临时授信过期事件
                eventPublisher.publishEvent(TemporaryCreditExpiredEvent.builder()
                        .temporaryCreditId(tc.getId())
                        .resellerId(tc.getResellerId())
                        .totalAmount(tc.getTotalAmount())
                        .usedAmount(tc.getUsedAmount())
                        .remainingAmount(tc.getRemainingAmount())
                        .expiredAt(LocalDateTime.now())
                        .build());
            }
        }

        log.info("Expired temporary credits detection completed, count: {}", expiredCredits.size());
    }
}