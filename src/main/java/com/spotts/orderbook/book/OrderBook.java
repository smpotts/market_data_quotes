package com.spotts.orderbook.book;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The order book.
 */
@Component
public class OrderBook {
    private static final String inputTsPattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String outputTsPattern = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final String filePath = "src/main/resources/quotes_2021-02-18.csv";
    private static final String[] headers = {"symbol", "marketCenter", "bidQuantity",
            "askQuantity", "bidPrice", "askPrice", "startTime", "endTime",
            "quoteConditions","sipfeedSeq" ,"sipfeed"};

    static List<Quote> fullOrderBook = new ArrayList<>();

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
        // set up the reader to iterate through csv records with a header in the file
        Reader fileReader = new FileReader(filePath);
        Iterable<CSVRecord> records = CSVFormat.DEFAULT
                .withHeader(headers)
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
            newQuote.setStartTime(formatTimestamp(record.get("startTime")));
            newQuote.setEndTime(formatTimestamp(record.get("endTime")));
            newQuote.setQuoteConditions(record.get("quoteConditions"));
            newQuote.setSipFeedSeq(record.get("sipfeedSeq"));
            newQuote.setSipFeed(record.get("sipfeed"));

            // add the new quote to the quotes on the book
            fullOrderBook.add(newQuote);
        }
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
        Timestamp pointInTime = formatTimestamp(timestampString);
        // return live quotes on the book for the given symbol
        return fullOrderBook
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
     * @return an ordered list of nbb quotes in descending order i.e. highest
     * quotes first.
     * @throws ParseException thrown when there is an issue parsing the quotes
     */
    private List<Quote> getNbbQuotes(String symbol, String pointInTime) throws ParseException {
        NbbQuoteComparator comparator = new NbbQuoteComparator();
        List<Quote> nbbLiveQuotes = getLiveQuotes(symbol, pointInTime);

        // sort the quotes with best (highest) bids first
        if (nbbLiveQuotes != null) {
            nbbLiveQuotes.sort(comparator.reversed());
        }
        return nbbLiveQuotes;
    }

    /**
     * Gets the NBO quotes for a symbol at a point in time.
     * @param symbol The symbol
     * @param pointInTime The String timestamp of the point in time
     * @return an ordered list of nbo quotes in ascending order i.e. lowest
     * quote first.
     * @throws ParseException thrown when there is an issue parsing
     */
    private List<Quote> getNboQuotes(String symbol, String pointInTime) throws ParseException {
        NboQuoteComparator comparator = new NboQuoteComparator();
        List<Quote> nboRankedQuotes = getLiveQuotes(symbol, pointInTime);

        // sort the quotes with best (lowest) asks first
        if (nboRankedQuotes != null) {
            nboRankedQuotes.sort(comparator);
        }

        return nboRankedQuotes;
    }

    /**
     * Gets the point in time best bids and asks for a given timestamp and symbol.
     * @param symbol The symbol to analyze
     * @param pointInTime The point in time
     * @return The formatted String with the input data and best bids and asks
     * @throws ParseException thrown when there is an issue parsing
     */
    public String pointInTimeResults(String symbol, String pointInTime) throws ParseException {
        StringBuilder strBuilder = new StringBuilder();

        // get the top 5 nbb quotes from the ordered list
        List<Quote> nbbQuotes = getNbbQuotes(symbol, pointInTime);
        List<Quote> topNbbQuotes = nbbQuotes.subList(0, 5);

        // get the top 5 nbo quotes from the ordered list
        List<Quote> nboQuotes = getNboQuotes(symbol, pointInTime);
        List<Quote> topNboQuotes = nboQuotes.subList(0, 5);

        // append the symbol and time pieces to the string builder
        strBuilder.append("$").append(symbol).append(" (").append(pointInTime).append(")").append("\n");
        // append the best bids
        strBuilder.append("Best Bids: ");

        for (Quote quote : topNbbQuotes) {
            strBuilder.append(quote.getBidPrice()).append("(").append(quote.getBidQuantity()).append("); ");
        }

        // append the best asks to the string
        strBuilder.append("\n").append("Best Asks: ");
        for (Quote quote : topNboQuotes) {
            strBuilder.append(quote.getAskPrice()).append("(").append(quote.getAskQuantity()).append("); ");
        }
        System.out.println(strBuilder.toString());
        return strBuilder.toString().replace("\n", "<br />\n");
    }

    /**
     * Formats the timestamp String from the quotes file into a Timestamp object
     * @param timestampString the timestamp String
     * @return a Timestamp object created from the timestampString
     * @throws ParseException thrown when there is an issue parsing
     */
    private static Timestamp formatTimestamp(String timestampString) throws ParseException {
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputTsPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputTsPattern);
        Date parsedDate = inputFormat.parse(timestampString);
        String formattedTime = outputFormat.format(parsedDate);
        return Timestamp.valueOf(formattedTime);
    }
}
