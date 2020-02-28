package org.sourcehot.spring.el;

import java.util.List;
import java.util.Map;

public class EaBean {
    private String name;
    private EbBean ebBean;
    private int[] ints;
    private List<Integer> integerList;
    private Map<String, String> map;
    public EaBean() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getIntegerList() {
        return integerList;
    }

    public void setIntegerList(List<Integer> integerList) {
        this.integerList = integerList;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }

    public int[] getInts() {
        return ints;
    }

    public void setInts(int[] ints) {
        this.ints = ints;
    }

    public EbBean getEbBean() {
        return ebBean;
    }

    public void setEbBean(EbBean ebBean) {
        this.ebBean = ebBean;
    }


}
