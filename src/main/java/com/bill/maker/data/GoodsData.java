package com.bill.maker.data;

import com.bill.maker.entity.Good;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class GoodsData implements ApplicationRunner {


    public static final List<Good> GOOD_LIST = new ArrayList<>();
    public static final BigDecimal ALL_GOOD_MIN_PRICE = BigDecimal.valueOf(50D);
    public static Integer BILL_NO = 800;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        GOOD_LIST.add(Good.builder().id(1).name("靴").mixPrice(750).maxPrice(4000).weight(8).build());//鞋
        GOOD_LIST.add(Good.builder().id(2).name("洋服").mixPrice(500).maxPrice(4000).weight(8).build());//西服
        GOOD_LIST.add(Good.builder().id(3).name("婦人肌着").mixPrice(250).maxPrice(700).weight(6).build());//女士内衣
        GOOD_LIST.add(Good.builder().id(4).name("靴下").mixPrice(100).maxPrice(300).weight(7).build());//袜子
        GOOD_LIST.add(Good.builder().id(5).name("子供用品").mixPrice(200).maxPrice(2000).weight(5).build());//儿童用品
        GOOD_LIST.add(Good.builder().id(6).name("帽子類").mixPrice(600).maxPrice(3000).weight(6).build());//帽子
        GOOD_LIST.add(Good.builder().id(7).name("化粧品").mixPrice(100).maxPrice(4000).weight(8).build());//化妆品
        GOOD_LIST.add(Good.builder().id(8).name("バッグ").mixPrice(600).maxPrice(3000).weight(7).build());//包
        GOOD_LIST.add(Good.builder().id(9).name("サプリメント").mixPrice(200).maxPrice(1000).weight(6).build());//保健食品
        GOOD_LIST.add(Good.builder().id(10).name("装身具").mixPrice(300).maxPrice(2000).weight(6).build());//饰品
        GOOD_LIST.add(Good.builder().id(11).name("文房具").mixPrice(100).maxPrice(800).weight(2).build());//文具
        GOOD_LIST.add(Good.builder().id(12).name("電気製品").mixPrice(500).maxPrice(1000).weight(3).build());//电器
        GOOD_LIST.add(Good.builder().id(13).name("生活雑貨").mixPrice(100).maxPrice(1000).weight(8).build());//生活杂货
        GOOD_LIST.add(Good.builder().id(14).name("食品").mixPrice(50).maxPrice(300).weight(3).build());//食品
        log.info("good data init success! size:{}", GOOD_LIST.size());
    }

}
