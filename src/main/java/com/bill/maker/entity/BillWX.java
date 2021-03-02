package com.bill.maker.entity;

import com.bill.maker.utils.ExcelColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Bill 账单类
 *
 * @version 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BillWX {

    @ExcelColumn(value = "交易时间")
    private String payTime;

    @ExcelColumn(value = "交易对方")
    private String customerName;

    @ExcelColumn(value = "收/支")
    private String billType;

    @ExcelColumn(value = "金额(元)")
    private String money;



   //番号
    private String requestNO;

    private List<Good> goodList;

    private String telNo;

    private String gustName;

    /**
     * @param address 地址
     */
    private String address;

    /**
     * @param postalCode 邮编号码
     */
    private String postalCode;
}
