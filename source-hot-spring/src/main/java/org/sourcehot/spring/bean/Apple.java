package org.sourcehot.spring.bean;

import java.util.Date;

public class Apple {
    private String name;
    private Date date;

    public Apple() {
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "{\"Apple\":{"
                + "\"name\":\""
                + name + '\"'
                + ",\"date\":\""
                + date + '\"'
                + "}}";

    }
}
