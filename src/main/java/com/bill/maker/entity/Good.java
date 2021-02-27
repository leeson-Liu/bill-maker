package com.bill.maker.entity;

import com.bill.maker.utils.ExcelColumn;
import lombok.*;

/**
 * Good 商品类
 *
 * @version 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Good {

    private int id;

    @ExcelColumn(value = "品名")
    private String name;

    @ExcelColumn(value = "价格最高")
    private Integer maxPrice;

    @ExcelColumn(value = "价格最低")
    private Integer mixPrice;

    @ExcelColumn(value = "权重")
    private Integer weight;

    private Integer realPrice;

    private Integer num;

    private Integer totalPrice;
}
