package com.bill.maker.service;

import com.bill.maker.entity.Good;

import java.util.List;

/**
 * @author liubin
 * @create 2021-02-24 19:08
 * @desc
 **/
public interface BillService {

    String helloWord(String word);

    List<Good> makeBill(Integer money);

}
