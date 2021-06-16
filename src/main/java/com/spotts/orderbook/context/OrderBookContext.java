package com.spotts.orderbook.context;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The Context for the OrderBook.
 */
@Data
@ConfigurationProperties(prefix = "book")
public class OrderBookContext {
    private int resultLimit;
    private final String filePath = "src/main/resources/quotes_2021-02-18.csv";
    public static final String[] HEADERS = {"symbol", "marketCenter", "bidQuantity",
            "askQuantity", "bidPrice", "askPrice", "startTime", "endTime",
            "quoteConditions","sipfeedSeq" ,"sipfeed"};
}
