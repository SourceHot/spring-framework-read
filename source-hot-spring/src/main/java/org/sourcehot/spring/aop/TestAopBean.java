package org.sourcehot.spring.aop;

public class TestAopBean {
    private String testStr = "TestAopBean";


    public String getTestStr() {
        return testStr;
    }

    public void setTestStr(String testStr) {
        this.testStr = testStr;
    }

    public void test() {
        System.out.println(testStr);
    }
}
