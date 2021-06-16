package com.spotts.orderbook.service;

import com.spotts.orderbook.context.OrderBookContext;
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

/**
 * The OrderBookService.
 */
@Component
public class OrderBookService {
    private final OrderBookUtil bookUtil = new OrderBookUtil();
    public final OrderBook orderBook = new OrderBook();

    @Autowired
    public OrderBookContext context;

    @PostConstruct
    public void setUp() {
        try {
            // build the order book
            buildOrderBook();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Builds the full order book by parsing the quote input data and
     * adding it to the book.
     * @throws IOException thrown when there is an issue parsing the input data.
     * @throws ParseException thrown when there is an issue parsing the timestamp String
     */
    public void buildOrderBook() throws IOException, ParseException {
        List<Quote> quoteList = new ArrayList<>();
        // set up the reader to iterate through csv records with a header in the file
        Reader fileReader = new FileReader(context.getFilePath());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT
                .withHeader(OrderBookContext.HEADERS)
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

            // add the new quote to the quote list
            quoteList.add(newQuote);
        }
        // put the quote list on the full order book
        orderBook.setFullOrderBook(quoteList);
    }

    /**
     * Gets the quotes from the full order book for a given symbol that were live
     * at that point in time.
     * @param symbol The symbol
     * @param timestampString The String timestamp point in time.
     * @return The quotes that were live on the book for that symbol at that
     * time.
     * @throws ParseException thrown when there is an issue parsing the timestamp String
     */
    private List<Quote> getLiveQuotes(String symbol, String timestampString) throws ParseException {
        // create a timestamp from the input string
        Timestamp pointInTime = bookUtil.formatTimestamp(timestampString);
        // return live quotes on the book for the given symbol and time period
        return orderBook.getFullOrderBook()
                .stream()
                .filter(q -> symbol.equals(q.getSymbol())
                        && (pointInTime.equals(q.getStartTime()) || pointInTime.after(q.getStartTime()))
                        && (pointInTime.equals(q.getEndTime()) || pointInTime.before(q.getEndTime())))
                .collect(Collectors.toList());
    }

    /**
     * Captures the National Best Bid (nbb) quotes on the order book given a
     * symbol and point in time.
     * @param symbol The symbol
     * @param pointInTime The String timestamp of the point in time.
     * @throws ParseException thrown when there is an issue parsing the pointInTime
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
     * Captures the National Best Offer (nbo) quotes on the order book given a
     * symbol and point in time.
     * @param symbol The symbol
     * @param pointInTime The String timestamp of the point in time.
     * @throws ParseException thrown when there is an issue parsing the pointInTime
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
     * @param symbol The symbol
     * @param pointInTime The String timestamp of the point in time.
     * @return The formatted String with the input data and best bids and asks
     * @throws ParseException thrown when there is an issue parsing
     */
    public String pointInTimeResults(String symbol, String pointInTime) throws ParseException {
        // get the top nbb quotes from the ordered list
        captureNbbQuotes(symbol, pointInTime);
        List<Quote> topNbbQuotes = orderBook.getNbbQuotes().subList(0, context.getResultLimit());

        // get the top nbo quotes from the ordered list
        captureNboQuotes(symbol, pointInTime);
        List<Quote> topNboQuotes = orderBook.getNboQuotes().subList(0, context.getResultLimit());

        // return the formatted output String
        return formatOutputString(symbol, pointInTime, topNbbQuotes, topNboQuotes);
    }

    private String formatOutputString(String symbol, String pointInTime,
                                      List<Quote> topNbbQuotes, List<Quote> topNboQuotes) {
        StringBuilder strBuilder = new StringBuilder();

        // append the symbol and time pieces to the string builder
        strBuilder.append("$").append(symbol).append(" (").append(pointInTime).append(")").append("<br />\n");
        // append the best bids
        strBuilder.append("Best Bids: ");

        // append the top nbb quotes to the output string
        for (Quote quote : topNbbQuotes) {
            strBuilder.append(quote.getBidPrice()).append("(").append(quote.getBidQuantity()).append("); ");
        }

        // append the best asks
        strBuilder.append("<br />\n").append("Best Asks: ");

        // append the top nbo quotes to the output string
        for (Quote quote : topNboQuotes) {
            strBuilder.append(quote.getAskPrice()).append("(").append(quote.getAskQuantity()).append("); ");
        }
        return strBuilder.toString();
    }
}
