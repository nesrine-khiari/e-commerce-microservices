package com.accountservice.projection;

import com.accountservice.event.AccountCreateEvent;
import com.accountservice.event.DepositMoneyEvent;
import com.accountservice.event.WithdrawMoneyEvent;
import com.accountservice.model.AccountModel;
import com.accountservice.query.GetAccountsQuery;
import com.accountservice.repository.AccountProjectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ReplayStatus;
import org.axonframework.eventhandling.ResetHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountProjection {
    private final AccountProjectionRepository accountProjectionRepository;

    @EventHandler
    public void handle(AccountCreateEvent accountCreateEvent) {
        Optional<AccountModel> account = accountProjectionRepository.findByUserid(accountCreateEvent.getUserid());

        if (account.isPresent()) {
            return;
        }

        AccountModel accountModel = new AccountModel(
                accountCreateEvent.getId(),
                accountCreateEvent.getUserid(),
                accountCreateEvent.getBalance()
        );
        accountProjectionRepository.save(accountModel);
    }

    /*@DisallowReplay*/
    @EventHandler
    public void handle(DepositMoneyEvent event) {
        AccountModel account = accountProjectionRepository.findByUserid(event.getUserid()).orElse(new AccountModel(event.getId(), event.getUserid(), event.getAmount()));
        account.setBalance(account.getBalance().add(event.getAmount()));
        accountProjectionRepository.save(account);
    }

    @EventHandler
    public void handle(WithdrawMoneyEvent event, ReplayStatus replayStatus) throws Exception {
       /* if (replayStatus != ReplayStatus.REPLAY) {*/
            AccountModel account = accountProjectionRepository.findByUserid(event.getUserid()).orElseThrow(Exception::new);
            if (account.getBalance().subtract(event.getAmount()).compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalStateException();
            } else if (event.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException();
            }

            account.setBalance(account.getBalance().subtract(event.getAmount()));
            log.info("EVENT ----> {}", event.getTimestamp());
            accountProjectionRepository.save(account);
       /* }*/

    }

    @QueryHandler
    public List<AccountModel> handle(GetAccountsQuery getOrdersQuery) {
        return accountProjectionRepository.findAll();
    }

    @ResetHandler
    public void reset() {
        accountProjectionRepository.deleteAll();
    }

}
