package com.source.hot.ioc.book.pojo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class PeopleBeanTwo {
    @Autowired
    @Qualifier("p1")
    private PeopleBean peopleBean;

    public PeopleBean getPeopleBean() {
        return peopleBean;
    }

    public void setPeopleBean(PeopleBean peopleBean) {
        this.peopleBean = peopleBean;
    }
}
