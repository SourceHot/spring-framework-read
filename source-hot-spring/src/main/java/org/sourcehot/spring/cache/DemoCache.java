package org.sourcehot.spring.cache;

public class DemoCache {
    private Integer id;

    public DemoCache(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return "DemoCache{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DemoCache() {
    }

    public DemoCache(String name) {
        this.name = name;
    }

}
