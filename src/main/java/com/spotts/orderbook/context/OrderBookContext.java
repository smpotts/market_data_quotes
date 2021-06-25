package com.spotts.orderbook.context;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The Context for the OrderBook.
 */
@Data
@ConfigurationProperties(prefix = "book")
public class OrderBookContext {
    public static final String[] HEADERS = {"symbol", "marketCenter", "bidQuantity",
            "askQuantity", "bidPrice", "askPrice", "startTime", "endTime",
            "quoteConditions","sipfeedSeq" ,"sipfeed"};
    private int resultLimit;
    private String filePath;
}