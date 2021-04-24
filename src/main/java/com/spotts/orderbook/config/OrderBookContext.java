package com.spotts.orderbook.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "book")
public class OrderBookContext {
    private int resultLimit;
    private final String filePath = "src/main/resources/quotes_2021-02-18.csv";
    private final String[] headers = {"symbol", "marketCenter", "bidQuantity",
            "askQuantity", "bidPrice", "askPrice", "startTime", "endTime",
            "quoteConditions","sipfeedSeq" ,"sipfeed"};
}
