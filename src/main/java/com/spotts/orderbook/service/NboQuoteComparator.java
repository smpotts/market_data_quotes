package com.spotts.orderbook.service;

import com.spotts.orderbook.model.Quote;

import java.util.Comparator;

/**
 * Comparator for comparing the askPrice between two Quotes.
 */
public class NboQuoteComparator implements Comparator<Quote> {
    @Override
    public int compare(Quote firstQuote, Quote secondQuote) {
        return firstQuote.getAskPrice().compareTo(secondQuote.getAskPrice());
    }
}
