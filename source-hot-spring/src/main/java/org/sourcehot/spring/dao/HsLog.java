package org.sourcehot.spring.dao;

public class HsLog {
    private Integer id;

    private String source;

    public HsLog() {
    }

    public HsLog(String source) {
        this.source = source;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
