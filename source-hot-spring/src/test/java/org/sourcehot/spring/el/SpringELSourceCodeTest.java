package org.sourcehot.spring.el;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = {"classpath*:/EL-context.xml"})
public class SpringELSourceCodeTest {

	@Autowired
	StandardEvaluationContext evaluationContext;
	@Autowired
	ApplicationContext applicationContext;
	@Autowired
	private SpelExpressionParser parser;
	@Qualifier("eaBean")
	@Autowired
	private EaBean eaBean;

	/**
	 * 基础类型解析
	 */
	@Test
	public void t1() {
		String strValue = parser.parseExpression("\"huifer\"").getValue(String.class);
		Assert.assertEquals("huifer", strValue);
		Double doubleValue = parser.parseExpression("1.23").getValue(Double.class);
		Assert.assertEquals(1.23, doubleValue, 0.0);
		Integer intValue =
				parser.parseExpression("0x7fffffff").getValue(Integer.class);
		Assert.assertEquals(Integer.MAX_VALUE, (int) intValue);
		Boolean booleanValue =
				parser.parseExpression("false").getValue(Boolean.class);
		Assert.assertFalse(booleanValue);
		Object nullValue =
				parser.parseExpression("null").getValue(Object.class);
		Assert.assertNull(nullValue);
	}

	/**
	 * 对象操作
	 */
	@Test
	public void t2() {
		Expression expression = parser.parseExpression("ints[0]");
		Integer intValue = expression.getValue(eaBean, int.class);
		Assert.assertEquals(1, (int) intValue);

		Integer value = parser.parseExpression("integerList.get(0)").getValue(eaBean, Integer.class);
		Assert.assertTrue(1 == value);
		parser.parseExpression("integerList[0]").setValue(evaluationContext, eaBean, 222);
		value = parser.parseExpression("integerList.get(0)").getValue(eaBean, Integer.class);
		Assert.assertTrue(222 == value);

		expression = parser.parseExpression("map[0]");
		String value1 = expression.getValue(eaBean, String.class);
		Assert.assertTrue("0".equals(value1));
	}


	/**
	 * 对象操作
	 */
	@Test
	public void t3() {
		Expression expression =
				parser.parseExpression("{'0','1','2'}");
		List<?> list = expression.getValue(evaluationContext, List.class);
		System.out.println(list);
		Expression expression2 =
				parser.parseExpression("{0:'a',1:'b',2:'c'}");
		Map<?, ?> map = expression2.getValue(evaluationContext, Map.class);
		System.out.println(map);
		Expression expression3 = parser.parseExpression("new int[]{1,2,3,4,5}");
		int[] intarr = expression3.getValue(evaluationContext, int[].class);
		System.out.println(Arrays.toString(intarr));
	}

	/**
	 * 运算符
	 */
	@Test
	public void t4() {
		Expression expression = parser.parseExpression("2>3");
		Boolean value = expression.getValue(boolean.class);
		Assert.assertFalse(value);

		Expression expression2 = parser.parseExpression("true and false");
		Boolean value2 = expression2.getValue(boolean.class);
		Assert.assertFalse(value2);


		Expression expression3 = parser.parseExpression("1.0 + 1.0");
		Double value3 = expression3.getValue(double.class);
		Assert.assertTrue(2.0 == value3);
	}

	@Test
	public void t5() {
		// 在环境中新建一个变量.
		evaluationContext.setVariable("hc", "hc");
		// 使用该变量,#newname
		Expression expression = parser.parseExpression("name = #hc");
		expression.getValue(evaluationContext, eaBean);
		System.out.println(eaBean.getName());
	}
}