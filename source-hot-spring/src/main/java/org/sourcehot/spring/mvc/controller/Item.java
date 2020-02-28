package org.sourcehot.spring.mvc.controller;


import java.util.Date;

/**
 * 描述:
 *
 * @author huifer
 * @date 2019-03-10
 */
public class Item {
    private String name;
    private Double price;
    private Date date;

    public Item(String name, Double price, Date date) {
        this.name = name;
        this.price = price;
        this.date = date;
    }

    public Item() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
