package com.source.hot.ioc.book.ann.three;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

@Configuration
@Import(ExtendConfiguration.class)
public class ThreeVarBeans {
}
