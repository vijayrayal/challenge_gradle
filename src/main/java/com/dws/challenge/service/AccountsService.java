package com.dws.challenge.service;

import java.math.BigDecimal;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dws.challenge.common.ChanllengeConstants;
import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.GenericChanllengeException;
import com.dws.challenge.model.TransferAccountModel;
import com.dws.challenge.repository.AccountsRepository;

import lombok.Getter;

@Service
public class AccountsService {

	@Getter
	private final AccountsRepository accountsRepository;

	private final NotificationService notificationService;

	@Autowired
	public AccountsService(AccountsRepository accountsRepository, NotificationService notificationService) {
		this.accountsRepository = accountsRepository;
		this.notificationService = notificationService;
	}

	public void createAccount(Account account) {
		this.accountsRepository.createAccount(account);
	}

	public Account getAccount(String accountId) {
		return this.accountsRepository.getAccount(accountId);
	}

	public Boolean transferAmountAndNotifyAccounts(TransferAccountModel model) {
		Account fromAccount = getAccount(model.getAccountFromId());
		Account toAccount = getAccount(model.getAccountToId());
		// Deducting amount from account and call update account.
		fromAccount.setBalance(getBalanceFromAccount(fromAccount).subtract(model.getAmount()));
		this.accountsRepository.updateAccount(fromAccount);
		// Deducting amount to account and call update account.
		toAccount.setBalance(getBalanceFromAccount(toAccount).add(model.getAmount()));
		this.accountsRepository.updateAccount(toAccount);
		//Send notifications between accounts.
		notifyAccounts(fromAccount, toAccount, model.getAmount());
		return Boolean.TRUE;
	}

	
	private BigDecimal getBalanceFromAccount(Account account) {
		BigDecimal balance = BigDecimal.ZERO;
		ReentrantLock lock = new ReentrantLock();
		lock.lock();
		try {
			balance = account.getBalance();
		} finally {
			lock.unlock();
		}
		return balance;
	}

	public void validateTransferModel(TransferAccountModel model) {
		// If Accounts are same then throw validation.
		if (StringUtils.equals(model.getAccountFromId(), model.getAccountToId())) {
			throw new GenericChanllengeException(ChanllengeConstants.SAME_ACCT);
		}
		// If Amount is greater or equal to zero then throw validation
		if (model.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
			throw new GenericChanllengeException(ChanllengeConstants.AMT_GR_THAN_ZERO);
		}
		// Get account details from service.
		Account fromAccount = getAccount(model.getAccountFromId());
		// If from account is not valid then throw validation.
		if (fromAccount == null) {
			throw new GenericChanllengeException("From " + ChanllengeConstants.NO_ACCT_EXISTS);
		}
		// Get destination account details from service.
		Account toAccount = getAccount(model.getAccountToId());
		// If destination account is not valid then throw validation.
		if (toAccount == null) {
			throw new GenericChanllengeException("To " + ChanllengeConstants.NO_ACCT_EXISTS);
		}
		// If source account end up with negative balance then throw validation.
		if (fromAccount.getBalance().subtract(model.getAmount()).compareTo(BigDecimal.ZERO) < 0) {
			throw new GenericChanllengeException(ChanllengeConstants.ACCT_END_UP_NEGATIVE);
		}
	}

	private void notifyAccounts(Account fromAccount, Account toAccount, BigDecimal amount) {
		//Send notification to source account
		notificationService.notifyAboutTransfer(fromAccount, amount + " Amount Transferred Sucessfully");
		//send notification to destination account
		notificationService.notifyAboutTransfer(toAccount, amount + " Amount Received");
	}

}
