//package com.source.hot.mvc.ctr;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.ResponseBody;
//import org.springframework.web.bind.annotation.ResponseStatus;
//
//@ControllerAdvice
//public class CustomExceptionHandler {
//	@ResponseBody
//	@ExceptionHandler(value = Exception.class)
//	public Map<String, Object> errorHandler(Exception ex) {
//		Map<String, Object> map = new HashMap<>();
//		map.put("code", 400);
//		map.put("msg", ex.getMessage());
//		return map;
//	}
//}
