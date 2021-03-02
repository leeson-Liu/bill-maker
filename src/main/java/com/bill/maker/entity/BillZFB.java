package com.bill.maker.entity;

import com.bill.maker.utils.ExcelColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BillZFB {

    @ExcelColumn(value = "入账时间")
    private String payTime;

    @ExcelColumn(value = "对方名称")
    private String customerName;

    @ExcelColumn(value = "收入（+元）")
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
