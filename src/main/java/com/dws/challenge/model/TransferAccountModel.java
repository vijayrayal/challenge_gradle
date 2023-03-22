package com.dws.challenge.model;

import java.math.BigDecimal;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class TransferAccountModel {

	@NotNull
	private String accountFromId;
	@NotNull
	private String accountToId;
	@NotBlank
	private BigDecimal amount;

}
