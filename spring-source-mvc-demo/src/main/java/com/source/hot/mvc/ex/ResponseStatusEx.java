package com.source.hot.mvc.ex;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_GATEWAY, reason = "ResponseStatusEx")
public class ResponseStatusEx extends RuntimeException {

	private static final long serialVersionUID = 828516526031958140L;
}
