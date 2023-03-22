package com.dws.challenge.web;

import java.math.BigDecimal;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dws.challenge.common.ChanllengeConstants;
import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.GenericChanllengeException;
import com.dws.challenge.model.TransferAccountModel;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.NotificationService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

	private static final Logger log = LoggerFactory.getLogger(AccountsController.class);

	private final AccountsService accountsService;
	private final NotificationService notificationService;

	@Autowired
	public AccountsController(AccountsService accountsService, NotificationService notificationService) {
		this.accountsService = accountsService;
		this.notificationService = notificationService;
	}

	@PostMapping(value = "/createAccount")
	public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
		log.info("Creating account {}", account);

		try {
			this.accountsService.createAccount(account);
		} catch (DuplicateAccountIdException daie) {
			return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@GetMapping(path = "/getAccount/{accountId}")
	public Account getAccount(@PathVariable String accountId) {
		log.info("Retrieving account for id {}", accountId);
		return this.accountsService.getAccount(accountId);
	}

	@PostMapping(value = "/transfer")
	public ResponseEntity<Boolean> transferAmount(@RequestBody TransferAccountModel model) {
		log.info("Transfer Accounts. {}", model);
		// If Accounts are same then throw validation.
		if (StringUtils.equals(model.getAccountFromId(), model.getAccountToId())) {
			throw new GenericChanllengeException(ChanllengeConstants.SAME_ACCT);
		}
		// If Amount is greater or equal to zero then throw validation
		if (model.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
			throw new GenericChanllengeException(ChanllengeConstants.AMT_GR_THAN_ZERO);
		}
		// Get account details from service.
		Account fromAccount = accountsService.getAccount(model.getAccountFromId());
		// If from account is not valid then throw validation.
		if (fromAccount == null) {
			throw new GenericChanllengeException("From " + ChanllengeConstants.NO_ACCT_EXISTS);
		}
		// Get destination account details from service.
		Account toAccount = accountsService.getAccount(model.getAccountToId());
		// If destination account is not valid then throw validation.
		if (toAccount == null) {
			throw new GenericChanllengeException("To " + ChanllengeConstants.NO_ACCT_EXISTS);
		}
		// If source account end up with negative balance then throw validation.
		if (fromAccount.getBalance().subtract(model.getAmount()).compareTo(BigDecimal.ZERO) < 0) {
			throw new GenericChanllengeException(ChanllengeConstants.ACCT_END_UP_NEGATIVE);
		}
		// call transfer amount service
		if (accountsService.transferAmountBetweenAccounts(fromAccount, toAccount, model.getAmount())) {
			// Sending notification to from Account and to account via notification service.
			notificationService.notifyAboutTransfer(fromAccount, model.getAmount() + " Amount Transferred Sucessfully");
			notificationService.notifyAboutTransfer(toAccount, model.getAmount() + " Amount Received");
			return new ResponseEntity<>(Boolean.TRUE, HttpStatus.OK);
		}
		return new ResponseEntity<>(Boolean.FALSE, HttpStatus.OK);

	}

}
