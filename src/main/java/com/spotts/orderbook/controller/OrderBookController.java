package com.spotts.orderbook.controller;

import com.spotts.orderbook.service.OrderBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

/**
 * The Controller.
 */
@RestController
@RequestMapping("/")
public class OrderBookController {

    @Autowired
    OrderBookService orderBookService;

    /**
     * Gets the point in time results from the order book.
     * @return a formatted String with the point in time results given the input symbol
     * and timestamp
     * @throws ParseException thrown when there is an issue parsing the timestamps
     */
    @GetMapping
    public String results() throws ParseException {
        return orderBookService.pointInTimeResults("AAPL", "2021-02-18T10:08:52.868Z");
    }
}
