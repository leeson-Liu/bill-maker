package com.bill.maker.entity;

/**
 * Good 商品类
 * @version 1.0.0
 */
public class Good {

    /**
     * @param goodName 品名
     */
    private String goodName;

    /**
     * @param higestPrice 最高价格
     */
    private double higestPrice;

    /**
     * @param lowestPrice 最低价格
     */
    private double lowestPrice;

    /**
     * @param goodWeight 权重
     */
    private int goodWeight;



    public Good(String goodName, double higestPrice, double lowestPrice, int goodWeight) {
        this.goodName = goodName;
        this.higestPrice = higestPrice;
        this.lowestPrice = lowestPrice;
        this.goodWeight = goodWeight;
    }

    public Good() {
    }

    public int getGoodWeight() {
        return goodWeight;
    }

    public void setGoodWeight(int goodWeight) {
        this.goodWeight = goodWeight;
    }

    public double getLowestPrice() {
        return lowestPrice;
    }

    public void setLowestPrice(double lowestPrice) {
        this.lowestPrice = lowestPrice;
    }

    public double getHigestPrice() {
        return higestPrice;
    }

    public String getGoodName() {
        return goodName;
    }

    public void setGoodName(String goodName) {
        this.goodName = goodName;
    }
}
