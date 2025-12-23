package com.bytz.modules.cms.payway.wallet.domain.command;

import com.bytz.modules.cms.payway.wallet.domain.enums.WalletTransactionStatus;
import com.bytz.modules.cms.payway.wallet.domain.enums.WalletTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * 查询交易命令
 * Query Transactions Command
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryTransactionsCommand {
    
    /**
     * 钱包ID
     */
    @NotBlank(message = "钱包ID不能为空")
    private String walletId;
    
    /**
     * 交易类型（可选）
     */
    private WalletTransactionType transactionType;
    
    /**
     * 交易状态（可选）
     */
    private WalletTransactionStatus transactionStatus;
    
    /**
     * 开始日期（可选）
     */
    private LocalDateTime startDate;
    
    /**
     * 结束日期（可选）
     */
    private LocalDateTime endDate;
}
