package com.bill.maker.service.impl;

import com.bill.maker.entity.Good;
import com.bill.maker.service.BillService;
import com.bill.maker.utils.WeightRandom;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    public List<Good> makeBill(Double money) {
        List<Good> result = new ArrayList<>();
        BigDecimal bigDecimalMoney = BigDecimal.valueOf(money);
        BigDecimal priceSum = BigDecimal.ZERO;
        BigDecimal remainingMoney = BigDecimal.ZERO;
        for (int i = 0; ; i++) {
            Good good = getRandomGood();
            BigDecimal price = getRandomPrice(good.getMaxPrice(), good.getMixPrice());
            good.setRealPrice(price);
            int num;
            if (result.size() == 0) {
                num = 1;
            } else {
                num = getNum();
            }
            good.setNum(num);
            BigDecimal totalPrice = price.multiply(BigDecimal.valueOf((double) num));
            good.setTotalPrice(totalPrice);
            if (totalPrice.add(priceSum).compareTo(bigDecimalMoney) <= 0) {
                result.add(good);
                priceSum = priceSum.add(totalPrice);
            }
            if (bigDecimalMoney.subtract(priceSum).compareTo(BigDecimal.valueOf(500D)) < 0) {
                remainingMoney = bigDecimalMoney.subtract(priceSum);
                break;
            }
        }

        //凑单
        if (remainingMoney.compareTo(ALL_GOOD_MIN_PRICE) >= 0) {
            for (; ; ) {
                Good good = getRandomGood();
                if (BigDecimal.valueOf(good.getMixPrice()).compareTo(remainingMoney) < 0 && remainingMoney.compareTo(BigDecimal.valueOf(Double.valueOf(good.getMaxPrice()))) < 0) {
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
            good.setTotalPrice(good.getTotalPrice().add(remainingMoney));
            good.setRealPrice(good.getRealPrice().add(remainingMoney));
            result.set(0, good);
            log.debug("凑单前 good:{}", result.get(0));
        }


        return result;
    }

    private BigDecimal getRandomPrice(Integer max, Integer min) {
        int result = (int) (Math.random() * (max - min) + min);
        return BigDecimal.valueOf((double) (result / 10 * 10));
    }

    private Integer getNum() {
        int max = 7;
        int min = 1;
        return (int) (Math.random() * (max - min) + min);
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


}
