package com.dws.challenge.common;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class ChallengerResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
	private LocalDateTime timestamp = null;
	private int status;
	private String message;

	/**
	 * @param timestamp
	 * @param status
	 * @param error
	 */
	public ChallengerResponse(Integer status, String error) {
		super();
		this.status = status;
	}

	public ChallengerResponse() {
		// TODO Auto-generated constructor stub
	}

}
