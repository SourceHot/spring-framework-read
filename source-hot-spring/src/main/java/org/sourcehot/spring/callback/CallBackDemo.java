package org.sourcehot.spring.callback;

interface DemoAction {
    boolean h(Integer age);
}


public class CallBackDemo {
    public static boolean demo(DemoAction d, int i) {
        return d.h(i);
    }

    public static void main(String[] args) {
        System.out.println(demo(age -> age >= 1, 10));
    }

}