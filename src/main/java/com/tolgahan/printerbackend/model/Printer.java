package com.tolgahan.printerbackend.model;

public class Printer {
    private Integer id;
    private String ip;
    private String serialNo;
    private String model;
    private String name;

    public Printer(Integer id, String ip, String model) {
        this.id = id;
        this.ip = ip;
        this.model = model;
    }

    public Printer(Integer id, String ip, String serialNo, String model, String name) {
        this.id = id;
        this.ip = ip;
        this.serialNo = serialNo;
        this.model = model;
        this.name = name;
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

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
