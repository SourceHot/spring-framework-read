# Spring StringUtils
- `org.springframework.util.StringUtils


## tokenizeToStringArray
- 切割字符串


```java
StringUtils.tokenizeToStringArray("a,b , ,c", ",")
````
- 这段代码将返回 a,b,c 三个字符

- 如果需要获得空字符串可以使用下面方法

```
    String[] sa = StringUtils.tokenizeToStringArray("a,b , ,c", ",", true, false);
```

- 根据最后两个参数可以得到不同的切分结果. 具体用例如下


```java
	@Test
	void tokenizeToStringArray() {
		String[] sa = StringUtils.tokenizeToStringArray("a,b , ,c", ",");
		assertThat(sa.length).isEqualTo(3);
		assertThat(sa[0].equals("a") && sa[1].equals("b") && sa[2].equals("c")).as("components are correct").isTrue();
	}

	@Test
	void tokenizeToStringArrayWithNotIgnoreEmptyTokens() {
		String[] sa = StringUtils.tokenizeToStringArray("a,b , ,c", ",", true, false);
		assertThat(sa.length).isEqualTo(4);
		assertThat(sa[0].equals("a") && sa[1].equals("b") && sa[2].equals("") && sa[3].equals("c")).as("components are correct").isTrue();
	}

	@Test
	void tokenizeToStringArrayWithNotTrimTokens() {
		String[] sa = StringUtils.tokenizeToStringArray("a,b ,c", ",", false, true);
		assertThat(sa.length).isEqualTo(3);
		assertThat(sa[0].equals("a") && sa[1].equals("b ") && sa[2].equals("c")).as("components are correct").isTrue();
	}
```