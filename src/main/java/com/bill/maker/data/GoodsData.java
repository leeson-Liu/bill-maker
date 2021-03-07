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
    public static Integer BILL_NO = 0;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        GOOD_LIST.add(Good.builder().id(1).name("靴").mixPrice(750).maxPrice(4000).weight(8).build());
        GOOD_LIST.add(Good.builder().id(2).name("洋服").mixPrice(500).maxPrice(4000).weight(8).build());
        GOOD_LIST.add(Good.builder().id(3).name("ズボン").mixPrice(500).maxPrice(4000).weight(7).build());
        GOOD_LIST.add(Good.builder().id(4).name("婦人肌着").mixPrice(250).maxPrice(700).weight(6).build());
        GOOD_LIST.add(Good.builder().id(5).name("スカート").mixPrice(600).maxPrice(3000).weight(7).build());
        GOOD_LIST.add(Good.builder().id(6).name("パジャマ").mixPrice(400).maxPrice(1500).weight(5).build());
        GOOD_LIST.add(Good.builder().id(7).name("下着").mixPrice(200).maxPrice(400).weight(4).build());
        GOOD_LIST.add(Good.builder().id(8).name("靴下").mixPrice(100).maxPrice(300).weight(7).build());
        GOOD_LIST.add(Good.builder().id(9).name("子供用品").mixPrice(200).maxPrice(2000).weight(5).build());
        GOOD_LIST.add(Good.builder().id(10).name("子供靴").mixPrice(400).maxPrice(1200).weight(4).build());
        GOOD_LIST.add(Good.builder().id(11).name("育児雑貨").mixPrice(100).maxPrice(1000).weight(4).build());
        GOOD_LIST.add(Good.builder().id(12).name("ベビー洋品雑貨").mixPrice(200).maxPrice(500).weight(4).build());
        GOOD_LIST.add(Good.builder().id(13).name("帽子").mixPrice(600).maxPrice(3000).weight(6).build());
        GOOD_LIST.add(Good.builder().id(14).name("化粧品").mixPrice(400).maxPrice(4000).weight(8).build());
        GOOD_LIST.add(Good.builder().id(15).name("化粧品雑貨").mixPrice(200).maxPrice(500).weight(3).build());
        GOOD_LIST.add(Good.builder().id(16).name("ボディ化粧品").mixPrice(100).maxPrice(1000).weight(5).build());
        GOOD_LIST.add(Good.builder().id(17).name("手袋").mixPrice(350).maxPrice(1500).weight(4).build());
        GOOD_LIST.add(Good.builder().id(18).name("財布").mixPrice(500).maxPrice(2000).weight(4).build());
        GOOD_LIST.add(Good.builder().id(19).name("バック").mixPrice(600).maxPrice(3000).weight(7).build());
        GOOD_LIST.add(Good.builder().id(20).name("ポｰチ").mixPrice(400).maxPrice(800).weight(4).build());
        GOOD_LIST.add(Good.builder().id(21).name("チャ－ム").mixPrice(300).maxPrice(2000).weight(5).build());
        GOOD_LIST.add(Good.builder().id(22).name("キ－ホルダー").mixPrice(300).maxPrice(1000).weight(4).build());
        GOOD_LIST.add(Good.builder().id(23).name("サプリメント").mixPrice(200).maxPrice(1000).weight(6).build());
        GOOD_LIST.add(Good.builder().id(24).name("健康食品").mixPrice(200).maxPrice(500).weight(4).build());
        GOOD_LIST.add(Good.builder().id(25).name("雑誌").mixPrice(100).maxPrice(300).weight(4).build());
        GOOD_LIST.add(Good.builder().id(26).name("CD").mixPrice(100).maxPrice(400).weight(4).build());
        GOOD_LIST.add(Good.builder().id(27).name("絵").mixPrice(100).maxPrice(300).weight(2).build());
        GOOD_LIST.add(Good.builder().id(28).name("ネックレス").mixPrice(400).maxPrice(3000).weight(6).build());
        GOOD_LIST.add(Good.builder().id(29).name("リング").mixPrice(400).maxPrice(3000).weight(5).build());
        GOOD_LIST.add(Good.builder().id(30).name("婦人服飾品").mixPrice(300).maxPrice(500).weight(4).build());
        GOOD_LIST.add(Good.builder().id(31).name("腕時計").mixPrice(600).maxPrice(4000).weight(4).build());
        GOOD_LIST.add(Good.builder().id(32).name("ヘアアクセサリー").mixPrice(300).maxPrice(1000).weight(6).build());
        GOOD_LIST.add(Good.builder().id(33).name("文房具").mixPrice(100).maxPrice(800).weight(3).build());
        GOOD_LIST.add(Good.builder().id(34).name("寝具セット").mixPrice(400).maxPrice(2000).weight(2).build());
        GOOD_LIST.add(Good.builder().id(35).name("イソテリア").mixPrice(300).maxPrice(2000).weight(3).build());
        GOOD_LIST.add(Good.builder().id(36).name("ルームウェア").mixPrice(300).maxPrice(500).weight(2).build());
        GOOD_LIST.add(Good.builder().id(37).name("電歯ブラシ").mixPrice(500).maxPrice(1000).weight(2).build());
        GOOD_LIST.add(Good.builder().id(38).name("ヘアアイロン").mixPrice(500).maxPrice(1500).weight(3).build());
        GOOD_LIST.add(Good.builder().id(39).name("ゲーム機").mixPrice(2500).maxPrice(4000).weight(3).build());
        GOOD_LIST.add(Good.builder().id(40).name("ドライヤー").mixPrice(500).maxPrice(1500).weight(3).build());
        GOOD_LIST.add(Good.builder().id(41).name("ボトル").mixPrice(200).maxPrice(500).weight(3).build());
        GOOD_LIST.add(Good.builder().id(42).name("生理用品").mixPrice(50).maxPrice(200).weight(3).build());
        GOOD_LIST.add(Good.builder().id(43).name("衛生雑貨").mixPrice(50).maxPrice(300).weight(3).build());
        GOOD_LIST.add(Good.builder().id(44).name("生活雑貨").mixPrice(100).maxPrice(1000).weight(8).build());
        GOOD_LIST.add(Good.builder().id(45).name("香水").mixPrice(500).maxPrice(2000).weight(2).build());
        GOOD_LIST.add(Good.builder().id(46).name("メガネ").mixPrice(200).maxPrice(700).weight(3).build());
        GOOD_LIST.add(Good.builder().id(47).name("菓子").mixPrice(50).maxPrice(300).weight(4).build());
        GOOD_LIST.add(Good.builder().id(48).name("茶葉").mixPrice(150).maxPrice(300).weight(2).build());
        GOOD_LIST.add(Good.builder().id(49).name("マスク").mixPrice(50).maxPrice(300).weight(5).build());
        GOOD_LIST.add(Good.builder().id(50).name("IQOS").mixPrice(300).maxPrice(1000).weight(4).build());
        GOOD_LIST.add(Good.builder().id(51).name("キャラクター雑貨").mixPrice(200).maxPrice(500).weight(3).build());
        log.info("good data init success! size:{}", GOOD_LIST.size());
    }

}
