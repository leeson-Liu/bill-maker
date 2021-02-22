package com.bill.maker.entity;

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

    private String name;

    private int maxPrice;

    private int mixPrice;

    private int weight;

}
