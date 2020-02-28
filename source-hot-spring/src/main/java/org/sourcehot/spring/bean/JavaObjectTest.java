package org.sourcehot.spring.bean;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class JavaObjectTest {
    private List<String> list;
    private String[] array;
    private Set<String> set;
    private Map<String, String> map;

    public JavaObjectTest(List<String> list) {
        this.list = list;
    }

    public JavaObjectTest(String[] array) {
        this.array = array;
    }

    public JavaObjectTest(Set<String> set) {
        this.set = set;
    }

    public JavaObjectTest(Map<String, String> map) {
        this.map = map;
    }

    public JavaObjectTest() {
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public String[] getArray() {
        return array;
    }

    public void setArray(String[] array) {
        this.array = array;
    }

    public Set<String> getSet() {
        return set;
    }

    public void setSet(Set<String> set) {
        this.set = set;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }
}
