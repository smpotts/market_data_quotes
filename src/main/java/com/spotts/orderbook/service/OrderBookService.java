package com.spotts.orderbook.service;

import com.spotts.orderbook.config.OrderBookContext;
import com.spotts.orderbook.model.OrderBook;
import com.spotts.orderbook.model.Quote;
import com.spotts.orderbook.util.OrderBookUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderBookService {
    @Autowired
    private OrderBookContext context;

    private final OrderBookUtil bookUtil = new OrderBookUtil();
    private final OrderBook orderBook = new OrderBook();

    @PostConstruct
    public void setUp() {
        try {
            buildOrderBook();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Builds the full order book by parsing the quote input data and
     * adding it to the book.
     * @throws IOException thrown when there is an issue parsing the input data.
     */
    public void buildOrderBook() throws IOException, ParseException {
        List<Quote> quoteList = new ArrayList<>();
        // set up the reader to iterate through csv records with a header in the file
        Reader fileReader = new FileReader(context.getFilePath());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT
                .withHeader(context.getHeaders())
                .withFirstRecordAsHeader()
                .parse(fileReader);

        for (CSVRecord record : records) {
            // set all the attributes of the new quote from the record
            Quote newQuote = new Quote();
            newQuote.setSymbol(record.get("symbol"));
            newQuote.setMarketCenter(record.get("marketCenter"));
            newQuote.setBidQuantity(new BigInteger(record.get("bidQuantity")));
            newQuote.setAskQuantity(new BigInteger(record.get("askQuantity")));
            newQuote.setBidPrice(new BigDecimal(record.get("bidPrice")));
            newQuote.setAskPrice(new BigDecimal(record.get("askPrice")));
            newQuote.setStartTime(bookUtil.formatTimestamp(record.get("startTime")));
            newQuote.setEndTime(bookUtil.formatTimestamp(record.get("endTime")));
            newQuote.setQuoteConditions(record.get("quoteConditions"));
            newQuote.setSipFeedSeq(record.get("sipfeedSeq"));
            newQuote.setSipFeed(record.get("sipfeed"));

            // add the new quote to the quotes on the book
            quoteList.add(newQuote);
        }
        orderBook.setFullOrderBook(quoteList);
    }

    /**
     * Gets the quotes from the full order book for a symbol that were live
     * given the point in time.
     * @param symbol The symbol
     * @param timestampString The String timestamp point in time.
     * @return The quotes that were live on the book for that symbol at that
     * time.
     * @throws ParseException thrown when there is an issue parsing the quotes
     */
    private List<Quote> getLiveQuotes(String symbol, String timestampString) throws ParseException {
        // create a timestamp from the input string
        Timestamp pointInTime = bookUtil.formatTimestamp(timestampString);
        // return live quotes on the book for the given symbol
        return orderBook.getFullOrderBook()
                .stream()
                .filter(q -> symbol.equals(q.getSymbol())
                        && (pointInTime.equals(q.getStartTime()) || pointInTime.after(q.getStartTime()))
                        && (pointInTime.equals(q.getEndTime()) || pointInTime.before(q.getEndTime())))
                .collect(Collectors.toList());
    }

    /**
     * Gets the NBB quotes for a symbol at a point in time.
     * @param symbol The symbol
     * @param pointInTime The String timestamp of the point in time.
     * quotes first.
     * @throws ParseException thrown when there is an issue parsing the quotes
     */
    private void captureNbbQuotes(String symbol, String pointInTime) throws ParseException {
        NbbQuoteComparator comparator = new NbbQuoteComparator();
        List<Quote> nbbLiveQuotes = getLiveQuotes(symbol, pointInTime);

        // sort the quotes with best (highest) bids first
        if (nbbLiveQuotes != null) {
            nbbLiveQuotes.sort(comparator.reversed());
        }
        orderBook.setNbbQuotes(nbbLiveQuotes);
    }

    /**
     * Gets the NBO quotes for a symbol at a point in time.
     * @param symbol The symbol
     * @param pointInTime The String timestamp of the point in time
     * quote first.
     * @throws ParseException thrown when there is an issue parsing
     */
    private void captureNboQuotes(String symbol, String pointInTime) throws ParseException {
        NboQuoteComparator comparator = new NboQuoteComparator();
        List<Quote> nboLiveQuotes = getLiveQuotes(symbol, pointInTime);

        // sort the quotes with best (lowest) asks first
        if (nboLiveQuotes != null) {
            nboLiveQuotes.sort(comparator);
        }
        orderBook.setNboQuotes(nboLiveQuotes);
    }

    /**
     * Gets the point in time best bids and asks for a given timestamp and symbol.
     * @param symbol The symbol to analyze
     * @param pointInTime The point in time
     * @return The formatted String with the input data and best bids and asks
     * @throws ParseException thrown when there is an issue parsing
     */
    public String pointInTimeResults(String symbol, String pointInTime) throws ParseException {
        // get the top 5 nbb quotes from the ordered list
        captureNbbQuotes(symbol, pointInTime);
        List<Quote> topNbbQuotes = orderBook.getNbbQuotes().subList(0, context.getResultLimit());

        // get the top 5 nbo quotes from the ordered list
        captureNboQuotes(symbol, pointInTime);
        List<Quote> topNboQuotes = orderBook.getNboQuotes().subList(0, context.getResultLimit());

        return buildOutputString(symbol, pointInTime, topNbbQuotes, topNboQuotes);
    }

    private String buildOutputString(String symbol, String pointInTime, List<Quote> topNbbQuotes, List<Quote> topNboQuotes) {
        StringBuilder strBuilder = new StringBuilder();

        // append the symbol and time pieces to the string builder
        strBuilder.append("$").append(symbol).append(" (").append(pointInTime).append(")").append("<br />\n");
        // append the best bids
        strBuilder.append("Best Bids: ");

        for (Quote quote : topNbbQuotes) {
            strBuilder.append(quote.getBidPrice()).append("(").append(quote.getBidQuantity()).append("); ");
        }

        // append the best asks to the string
        strBuilder.append("<br />\n").append("Best Asks: ");
        for (Quote quote : topNboQuotes) {
            strBuilder.append(quote.getAskPrice()).append("(").append(quote.getAskQuantity()).append("); ");
        }
        return strBuilder.toString();
    }
}
