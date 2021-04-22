package com.spotts.orderbook.book;

import java.util.Comparator;

/**
 * Comparator for comparing the askPrice between two quotes.
 */
public class NboQuoteComparator implements Comparator<Quote> {
    @Override
    public int compare(Quote firstQuote, Quote secondQuote) {
        return firstQuote.getAskPrice().compareTo(secondQuote.getAskPrice());
    }
}
