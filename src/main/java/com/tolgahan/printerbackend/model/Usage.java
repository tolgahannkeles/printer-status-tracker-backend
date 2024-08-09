package com.tolgahan.printerbackend.model;

public class Usage {
    private String date;
    private Integer blackWhite;
    private Integer color;

    public Usage(String date, Integer blackWhite, Integer color) {
        this.date = date;
        this.blackWhite = blackWhite;
        this.color = color;
    }
    public Usage(String date, Integer blackWhite) {
        this.date = date;
        this.blackWhite = blackWhite;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getBlackWhite() {
        return blackWhite;
    }

    public void setBlackWhite(Integer blackWhite) {
        this.blackWhite = blackWhite;
    }

    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
    }
}
