package com.bill.maker.service.impl;

import com.bill.maker.entity.Good;
import com.bill.maker.service.BillService;
import com.bill.maker.utils.WeightRandom;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.bill.maker.data.GoodsData.GOOD_LIST;

/**
 * @author liubin
 * @create 2021-02-24 19:09
 * @desc
 **/
@Service
@Slf4j
public class BillServiceImpl implements BillService {


    @Override
    public String makeBill(Integer money) {
        WeightRandom<Good, Integer> weightRandom = new WeightRandom(buildWeightRandom());
        Good good = weightRandom.random();
        return good.getName();
    }


    private List<Pair<Good, Integer>> buildWeightRandom() {
        List<Pair<Good, Integer>> pairList = new ArrayList<>();
        for (Good good : GOOD_LIST) {
            Pair<Good, Integer> pair = new ImmutablePair<>(good, good.getWeight());
            pairList.add(pair);
        }
        return pairList;
    }

    @Override
    public String helloWord(String word) {

        return "ok " + word;
    }


}
