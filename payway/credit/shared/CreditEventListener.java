package com.bytz.modules.cms.payway.credit.shared;

import com.bytz.modules.cms.payway.credit.domain.repository.ITemporaryCreditRepository;
import com.bytz.modules.cms.payway.credit.shared.event.WalletCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CreditEventListener {

    private  final ITemporaryCreditRepository temporaryCreditRepository;


    @EventListener
    public void handleCreditEvent(WalletCreatedEvent event) {
        log.info("信用钱包创建事件监听：{}", event);

        temporaryCreditRepository.updateWalletId(event.getResellerId(), event.getWalletId());
    }
}
