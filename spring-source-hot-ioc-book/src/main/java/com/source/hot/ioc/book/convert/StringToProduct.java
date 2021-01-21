package com.source.hot.ioc.book.convert;

import java.math.BigDecimal;

import org.springframework.core.convert.converter.Converter;

public class StringToProduct implements Converter<String, Product> {
	@Override
	public Product convert(String source) {
		String[] split = source.split(";");
		return new Product(split[0],new BigDecimal(split[1]));
	}
}
