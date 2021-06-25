package com.spotts.orderbook.service;

import com.spotts.orderbook.context.OrderBookContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.ParseException;

public class OrderBookServiceTest {
    public OrderBookContext context;

    @BeforeEach
    public void init() {
        context = new OrderBookContext();
    }

//    @Test
//    public void buildOrderBookTest() throws IOException, ParseException {
//        OrderBookService bookService = new OrderBookService();
//        bookService.buildOrderBook();
//        Assertions.assertNotEquals(0, bookService.orderBook.getFullOrderBook().size());
//    }
}
