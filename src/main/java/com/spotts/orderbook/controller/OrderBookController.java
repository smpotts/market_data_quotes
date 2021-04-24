package com.spotts.orderbook.controller;

import com.spotts.orderbook.service.OrderBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/")
public class OrderBookController {

    @Autowired
    OrderBookService orderBookService;

    @GetMapping
    public String results() throws ParseException {
        return orderBookService.pointInTimeResults("AAPL", "2021-02-18T10:22:37.381Z");
    }
}
