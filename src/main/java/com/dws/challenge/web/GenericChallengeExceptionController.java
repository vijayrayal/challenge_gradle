package com.dws.challenge.web;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.dws.challenge.common.ChallengerResponse;
import com.dws.challenge.exception.GenericChanllengeException;

@ControllerAdvice
public class GenericChallengeExceptionController {
	@ExceptionHandler(value = GenericChanllengeException.class)
	public ResponseEntity<ChallengerResponse> exception(GenericChanllengeException exception) {
		ChallengerResponse response = new ChallengerResponse();
		response.setTimestamp(LocalDateTime.now());
		response.setMessage(exception.getMessage());
		response.setStatus(500);
		return new ResponseEntity<ChallengerResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
