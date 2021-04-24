package com.spotts.orderbook.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * The OrderBook: a list of buy and sell orders for a security
 * or instrument organized by price & time.
 */
@Data
public class OrderBook {
    private List<Quote> fullOrderBook = new ArrayList<>();
    private List<Quote> nbbQuotes = new ArrayList<>();
    private List<Quote> nboQuotes = new ArrayList<>();
}
