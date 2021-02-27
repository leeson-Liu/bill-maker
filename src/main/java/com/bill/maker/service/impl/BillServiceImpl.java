package com.bill.maker.service.impl;

import com.bill.maker.entity.Good;
import com.bill.maker.service.BillService;
import com.bill.maker.utils.WeightRandom;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.bill.maker.data.GoodsData.GOOD_LIST;
import static com.bill.maker.data.GoodsData.ALL_GOOD_MIN_PRICE;

/**
 * @author liubin
 * @create 2021-02-24 19:09
 * @desc
 **/
@Service
@Slf4j
public class BillServiceImpl implements BillService {

    /**
     * 我突然间想到了个问题。我有些一笔的转账金额能达到20万～30万。
     * 我之前说的品名希望3-4。个数希望1-7。 那遇见这种怎么能解决下
     * 这种单笔大额的转账次数一个月差不多有一次左右
     */
    @Override
    public List<Good> makeBill(Integer money) {
        List<Good> result = new ArrayList<>();

        Integer priceSum = 0;
        Integer remainingMoney = 0;
        for (int i = 0; ; i++) {
            Good good = getRandomGood();
            Integer price = getRandomPrice(good.getMaxPrice(), good.getMixPrice());
            good.setRealPrice(price);
            Integer num;
            if (result.size() == 0) {
                num = 1;
            } else {
                num = getNum();
            }
            good.setNum(num);
            Integer totalPrice = price * num;
            good.setTotalPrice(totalPrice);
            if (totalPrice + priceSum <= money) {
                result.add(good);
                priceSum = priceSum + totalPrice;
            }
            if (money - priceSum < 500) {
                remainingMoney = money - priceSum;
                break;
            }
        }

        //凑单
        if (remainingMoney >= ALL_GOOD_MIN_PRICE) {
            for (; ; ) {
                Good good = getRandomGood();
                if (good.getMixPrice() < remainingMoney && remainingMoney < good.getMaxPrice()) {
                    good.setRealPrice(remainingMoney);
                    good.setNum(1);
                    good.setTotalPrice(remainingMoney);
                    result.add(good);
                    break;
                }
            }
        } else {
            log.debug("凑单触发: remainingMoney:{}", remainingMoney);
            Good good = result.get(0);
            log.debug("凑单前 good:{}", result.get(0));
            good.setTotalPrice(good.getTotalPrice() + remainingMoney);
            good.setRealPrice(good.getRealPrice() + remainingMoney);
            result.set(0, good);
            log.debug("凑单前 good:{}", result.get(0));
        }


        return result;
    }

    private Integer getRandomPrice(Integer max, Integer min) {
        int result = (int) (Math.random() * (max - min) + min);
        return result/10*10;
    }

    private Integer getNum() {
        Integer max = 7;
        Integer min = 1;
        int result = (int) (Math.random() * (max - min) + min);
        return result;
    }

    private Good getRandomGood() {
        List<Pair<Good, Integer>> pairList = new ArrayList<>();
        for (Good good : GOOD_LIST) {
            Pair<Good, Integer> pair = new ImmutablePair<>(good, good.getWeight());
            pairList.add(pair);
        }
        WeightRandom<Good, Integer> weightRandom = new WeightRandom(pairList);
        Good randomGood = weightRandom.random();
        Good good = new Good();
        BeanUtils.copyProperties(randomGood, good);
        return good;
    }

    @Override
    public String helloWord(String word) {

        return "ok " + word;
    }


}
