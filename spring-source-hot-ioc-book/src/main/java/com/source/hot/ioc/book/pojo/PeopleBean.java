package com.source.hot.ioc.book.pojo;

import java.util.List;
import java.util.Map;

public class PeopleBean {
    private String name;

    private List<String> list;

    private Map<String, String> map;

    private PeopleBean pb;

    public PeopleBean(String name, PeopleBean pb) {
        this.name = name;
        this.pb = pb;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PeopleBean() {
		System.out.println();
    }

    public PeopleBean(String name) {
        this.name = name;
    }
}
