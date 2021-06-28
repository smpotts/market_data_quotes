package com.spotts.orderbook.service;

import com.spotts.orderbook.context.OrderBookContext;
import com.spotts.orderbook.model.Quote;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderBookServiceTest {
    public OrderBookContext context;

    @BeforeEach
    public void init() {
        context = new OrderBookContext();
        context.setFilePath("src/test/resources/quotes_subset.csv");
        context.setResultLimit(2);
    }

    @Test
    public void buildOrderBookTest() throws IOException, ParseException {
        OrderBookService bookService = new OrderBookService(context);
        bookService.buildOrderBook();
        Assertions.assertNotEquals(20, bookService.orderBook.getFullOrderBook().size());
    }

    @Test
    public void getLiveQuotesTest() throws ParseException, IOException {
        OrderBookService bookService = new OrderBookService(context);
        bookService.buildOrderBook();
        List<Quote> live = bookService.getLiveQuotes("AAPL", "2021-02-18T09:58:59.262Z");
        assertEquals(1, live.size());
        assertEquals(BigDecimal.valueOf(129.46), live.get(0).getBidPrice());
    }

    @Test
    public void noLiveQuotesTest() throws IOException, ParseException {
        OrderBookService bookService = new OrderBookService(context);
        bookService.buildOrderBook();
        List<Quote> live = bookService.getLiveQuotes("AAPL", "2021-02-18T09:50:59.262Z");
        assertEquals(0, live.size());
    }

    @Test
    public void testCaptureNbbQuotes() throws IOException, ParseException {
        OrderBookService bookService = new OrderBookService(context);
        bookService.buildOrderBook();
        bookService.captureNbbQuotes("AAPL", "2021-02-18T09:58:59.266Z");
        List<Quote> nbbQuotes = bookService.orderBook.getNbbQuotes();
        assertEquals(3, nbbQuotes.size());
        assertEquals("10000129", nbbQuotes.get(1).getSipFeedSeq());
    }

    @Test
    public void testCaptureNboQuotes() throws IOException, ParseException {
        OrderBookService bookService = new OrderBookService(context);
        bookService.buildOrderBook();
        bookService.captureNboQuotes("AAPL", "2021-02-18T09:58:59.279Z");
        List<Quote> nboQuotes = bookService.orderBook.getNbbQuotes();
        assertEquals(0, nboQuotes.size());
    }
}