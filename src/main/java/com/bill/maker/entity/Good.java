package com.bill.maker.entity;

/**
 * Good 商品类
 *
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
    private int higestPrice;

    /**
     * @param lowestPrice 最低价格
     */
    private int lowestPrice;

    /**
     * @param goodWeight 权重
     */
    private int goodWeight;


    public Good(String goodName, int higestPrice, int lowestPrice, int goodWeight) {
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

    public void setLowestPrice(int lowestPrice) {
        this.lowestPrice = lowestPrice;
    }

    public double getHigestPrice() {
        return higestPrice;
    }

    public void setHigestPrice(int higestPrice) {
        this.higestPrice = higestPrice;
    }

    public String getGoodName() {
        return goodName;
    }

    public void setGoodName(String goodName) {
        this.goodName = goodName;
    }

    @Override
    public String toString() {
        return "Good{" +
                "goodName='" + goodName + '\'' +
                ", higestPrice=" + higestPrice +
                ", lowestPrice=" + lowestPrice +
                ", goodWeight=" + goodWeight +
                '}';
    }
}
