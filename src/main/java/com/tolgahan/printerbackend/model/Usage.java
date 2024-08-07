package com.tolgahan.printerbackend.model;

public class Usage {
    private String date;
    private Integer usage;

    public Usage(String date, Integer usage) {
        this.date = date;
        this.usage = usage;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getUsage() {
        return usage;
    }

    public void setUsage(Integer usage) {
        this.usage = usage;
    }
}
