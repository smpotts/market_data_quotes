package com.spotts.orderbook;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "book")
public class OrderBookContext {
    public String symbol;
    public String pointInTime;
    public int topResults;
}
