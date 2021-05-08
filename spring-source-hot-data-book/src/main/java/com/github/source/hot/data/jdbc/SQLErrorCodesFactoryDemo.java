package com.github.source.hot.data.jdbc;

import org.springframework.jdbc.support.SQLErrorCodes;
import org.springframework.jdbc.support.SQLErrorCodesFactory;

public class SQLErrorCodesFactoryDemo {
	public static void main(String[] args) {
			SQLErrorCodesFactory instance = SQLErrorCodesFactory.getInstance();
		SQLErrorCodes h2 = instance.getErrorCodes("H2");
		String[] badSqlGrammarCodes = h2.getBadSqlGrammarCodes();
		System.out.println();
	}
}
