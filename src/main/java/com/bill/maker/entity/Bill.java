package com.bill.maker.entity;

import java.util.List;

/**
 * Bill 账单类
 *
 * @version 1.0.0
 */
public class Bill {

    /**
     * @param gustName 请求先名称
     */
    private String gustName;

    /**
     * @param address 地址
     */
    private String address;

    /**
     * @param postalCode 邮编号码
     */
    private String postalCode;

    /**
     * @param representName 代表人名
     */
    private String representName;

    /**
     * @param telNo 电话号码
     */
    private String telNo;

    /**
     * @param requestedAmount 请求金额
     */
    private int requestedAmount;

    /**
     * @param goodList 商品列表
     */
    private List<Good> goodList;


    public Bill(String gustName, String address, String postalCode, String representName, String telNo, int requestedAmount, List<Good> goodList) {
        this.gustName = gustName;
        this.address = address;
        this.postalCode = postalCode;
        this.representName = representName;
        this.telNo = telNo;
        this.requestedAmount = requestedAmount;
        this.goodList = goodList;
    }

    public Bill() {
    }


    public String getGustName() {
        return gustName;
    }

    public void setGustName(String gustName) {
        this.gustName = gustName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getRepresentName() {
        return representName;
    }

    public void setRepresentName(String representName) {
        this.representName = representName;
    }

    public String getTelNo() {
        return telNo;
    }

    public void setTelNo(String telNo) {
        this.telNo = telNo;
    }

    public int getRequestedAmount() {
        return requestedAmount;
    }

    public void setRequestedAmount(int requestedAmount) {
        this.requestedAmount = requestedAmount;
    }

    public List<Good> getGoodList() {
        return goodList;
    }

    public void setGoodList(List<Good> goodList) {
        this.goodList = goodList;
    }
}
