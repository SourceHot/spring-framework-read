package com.source.hot.ioc.book.ioc;

import java.util.Map;

import com.source.hot.ioc.book.convert.AbcEnum;
import com.source.hot.ioc.book.convert.Product;
import org.junit.jupiter.api.Test;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.ConverterRegistry;

class ConvertTest {
	@Test
	void convertTest() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("/META-INF/convert.xml");
		ConversionService bean = context.getBean(ConversionService.class);
		Product convert = bean.convert("product;10.00", Product.class);

        ConversionServiceFactoryBean bean1 = context.getBean(ConversionServiceFactoryBean.class);
        Product convert1 = bean1.getObject().convert("product;10.00", Product.class);
        System.out.println();
	}

}
