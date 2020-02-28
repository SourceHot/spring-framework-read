package org.sourcehot.spring;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;

public class TestMain {
    public static void main(String[] args) {
//        BeanExpressionException test = new BeanExpressionException("中文测试");
//        System.out.println(test.getMessage());

        Map<String, String> map = new HashMap<>();

        map.put("1", "213");
        MultiValueMap<String, String> m = new LinkedMultiValueMap<>();
        map.forEach(m::add);
        System.out.println(m);
    }
}
