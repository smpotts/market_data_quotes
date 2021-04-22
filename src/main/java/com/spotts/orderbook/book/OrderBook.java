package com.spotts.orderbook.book;

import com.spotts.orderbook.OrderBookContext;
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
    private static final String[] headers = {"symbol", "marketCenter", "bidQuantity",
            "askQuantity", "bidPrice", "askPrice", "startTime", "endTime",
            "quoteConditions","sipfeedSeq" ,"sipfeed"};
    private static final String filePath = "src/main/resources/quotes_subset.csv";

    static List<Quote> fullOrderBook = new ArrayList<>();

    @Autowired
    private OrderBookContext context;

    @PostConstruct
    public void setUp() {
        try {
            System.out.println(formatTimestamp("2021-02-18T09:58:59.281Z"));
         } catch (ParseException e) {
            e.printStackTrace();
        }

        System.out.println(context.getTopResults());
        System.out.println(context.getPointInTime());
        System.out.println(context.getSymbol());
        try {
            buildOrderBook();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Builds the full order book by parsing the quote input data and
     * adding it to the book.
     *
     * @throws IOException
     *         thrown when there is an issue parsing the input data.
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
     *
     * @param symbol The symbol
     * @param timestampString The String timestamp point in time.
     * @return The quotes that were live on the book for that symbol at that
     * time.
     */
    private List<Quote> getLiveQuotes(String symbol, String timestampString) {
        // create a timestamp from the input string
        Timestamp pointInTime = Timestamp.valueOf(timestampString);
        // return live quotes on the book for the given symbol
        return fullOrderBook
                .stream()
                .filter(q -> symbol.equals(q.getSymbol())
                        && pointInTime.after(q.getStartTime())
                        && pointInTime.before(q.getEndTime()))
                .collect(Collectors.toList());
    }

    /**
     * Gets the NBB quotes for a symbol at a point in time.
     *
     * @param symbol The symbol
     * @param pointInTime The String timestamp of the point in time.
     * @return an ordered list of nbb quotes in descending order i.e. highest
     * quotes first.
     */
    private List<Quote> getNbbQuotes(String symbol, String pointInTime) {
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
     *
     * @param symbol The symbol
     * @param pointInTime The String timestamp of the point in time
     * @return an ordered list of nbo quotes in ascending order i.e. lowest
     * quote first.
     */
    private List<Quote> getNboQuotes(String symbol, String pointInTime) {
        NboQuoteComparator comparator = new NboQuoteComparator();
        List<Quote> nboRankedQuotes = getLiveQuotes(symbol, pointInTime);

        // sort the quotes with best (lowest) asks first
        if (nboRankedQuotes != null) {
            nboRankedQuotes.sort(comparator);
        }
        return nboRankedQuotes;
    }

    public void pointInTimeResults() {
        // grab context variables
        String symbol = context.getSymbol();
        String pointInTime = context.getPointInTime();
        int resultLimit = context.getTopResults();

        // get the nbb and nbo quotes
        List<Quote> nbbQuotes = getNbbQuotes(symbol, pointInTime);
        List<Quote> nboQuotes = getNboQuotes(symbol, pointInTime);

        System.out.println("$" + symbol + " (" + pointInTime + ")");
        System.out.print("Best Bids: ");
        int elementCount = 0;
        for (Quote quote : nbbQuotes) {
            System.out.print(quote.getBidPrice() + "(" + quote.getBidQuantity() + "); ");
            elementCount++;
            if (elementCount == resultLimit) {
                break;
            }
        }
        System.out.println();
        System.out.print("Best Asks: ");
        elementCount = 0;
        for (Quote quote : nboQuotes) {
            System.out.print(quote.getAskPrice() + "(" + quote.getAskQuantity() + "); ");
            elementCount++;
            if (elementCount == resultLimit) {
                break;
            }
        }
    }

    /**
     * Formats the timestamp String from the quotes file into a Timestamp object
     *
     * @param timestampString the timestamp String
     * @return a Timestamp object created from the timestampString
     * @throws ParseException
     *         thrown when there is an issue parsing the timestamp String
     */
    private static Timestamp formatTimestamp(String timestampString) throws ParseException {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date parsedDate = inputFormat.parse(timestampString);
        String formattedTime = outputFormat.format(parsedDate);
        return Timestamp.valueOf(formattedTime);
    }
}
