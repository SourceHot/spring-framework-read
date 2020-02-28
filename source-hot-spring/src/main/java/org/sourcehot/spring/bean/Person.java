package org.sourcehot.spring.bean;

public class Person {
    private String name;
    private Apple apple;
    private Integer age;

    public void dis() {
        System.out.println("dis");
    }

    public Person() {
    }

    public Person(Integer age) {
        this.age = age;
    }


    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Apple getApple() {
        return apple;
    }

    public void setApple(Apple apple) {
        this.apple = apple;
    }
}
