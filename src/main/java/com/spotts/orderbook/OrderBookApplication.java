package com.spotts.orderbook;

import com.spotts.orderbook.book.OrderBook;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(OrderBookContext.class)
public class OrderBookApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderBookApplication.class, args);
		// create a new order book
		OrderBook orderBook = new OrderBook();

	}
}
