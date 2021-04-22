package com.spotts.orderbook.book;

import java.util.Comparator;

/**
 * Comparator for comparing the bidPrice between two quotes.
 */
public class NbbQuoteComparator implements Comparator<Quote> {

    @Override
    public int compare(Quote firstQuote, Quote secondQuote) {
        return firstQuote.getBidPrice().compareTo(secondQuote.getBidPrice());
    }
}
