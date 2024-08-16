package com.tolgahan.printerbackend.model;

import java.sql.Date;
import java.time.LocalDateTime;

public class StatusExcel {
    public Integer id;
    public String ip;
    public String name;
    public LocalDateTime date;
    public Long monthlyMono;
    public Long monthlyColor;
    public Long totalMono;
    public Long totalColor;

    public StatusExcel(Integer id, String ip, String name, LocalDateTime date, Long monthlyMono, Long monthlyColor, Long totalMono, Long totalColor) {
        this.id = id;
        this.ip = ip;
        this.name = name;
        this.date = date;
        this.monthlyMono = monthlyMono;
        this.monthlyColor = monthlyColor;
        this.totalMono = totalMono;
        this.totalColor = totalColor;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Long getMonthlyMono() {
        return monthlyMono;
    }

    public void setMonthlyMono(Long monthlyMono) {
        this.monthlyMono = monthlyMono;
    }

    public Long getMonthlyColor() {
        return monthlyColor;
    }

    public void setMonthlyColor(Long monthlyColor) {
        this.monthlyColor = monthlyColor;
    }

    public Long getTotalMono() {
        return totalMono;
    }

    public void setTotalMono(Long totalMono) {
        this.totalMono = totalMono;
    }

    public Long getTotalColor() {
        return totalColor;
    }

    public void setTotalColor(Long totalColor) {
        this.totalColor = totalColor;
    }
}
