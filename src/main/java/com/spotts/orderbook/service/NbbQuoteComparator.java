package com.spotts.orderbook.service;

import com.spotts.orderbook.model.Quote;

import java.util.Comparator;

/**
 * Comparator for comparing the bidPrice between two Quotes.
 */
public class NbbQuoteComparator implements Comparator<Quote> {

    @Override
    public int compare(Quote firstQuote, Quote secondQuote) {
        return firstQuote.getBidPrice().compareTo(secondQuote.getBidPrice());
    }
}
