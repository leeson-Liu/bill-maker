package com.bill.maker.entity;

import com.bill.maker.utils.ExcelColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GoodUpload {

    private int id;

    @ExcelColumn(value = "品名")
    private String name;

    @ExcelColumn(value = "价格最高")
    private String maxPrice;

    @ExcelColumn(value = "价格最低")
    private String minPrice;

    @ExcelColumn(value = "权重")
    private String weight;
}
