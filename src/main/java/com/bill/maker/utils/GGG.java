//package com.bill.maker.utils;
//
//import lombok.Data;
//import org.springframework.util.StringUtils;
//
//@Data
//public class GGG {
//
//    @ExcelColumn(value = "身份证号")
//    private String cardId;
//    @ExcelColumn(value = "导出结果")
//    private String result;
//    @ExcelColumn(value = "所在人像库")
//    private String facedb;
//    public synchronized void addFacedb(String facedbName){
//        if(StringUtils.isEmpty(this.facedb)){
//            this.facedb=facedbName;
//            return;
//        }
//        this.facedb+=","+facedbName;
//    }
//}
