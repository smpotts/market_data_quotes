package com.spotts.orderbook.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OrderBookUtil {
    private static final String inputTsPattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String outputTsPattern = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * Formats the timestamp String from the quotes file into a Timestamp object
     * @param timestampString the timestamp String
     * @return a Timestamp object created from the timestampString
     * @throws ParseException thrown when there is an issue parsing
     */
    public Timestamp formatTimestamp(String timestampString) throws ParseException {
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputTsPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputTsPattern);
        Date parsedDate = inputFormat.parse(timestampString);
        String formattedTime = outputFormat.format(parsedDate);
        return Timestamp.valueOf(formattedTime);
    }
}
