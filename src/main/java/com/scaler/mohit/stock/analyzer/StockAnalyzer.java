package com.scaler.mohit.stock.analyzer;

import com.scaler.mohit.stock.analyzer.pojo.AnnualizedReturn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaler.mohit.stock.analyzer.pojo.Candle;
import com.scaler.mohit.stock.analyzer.pojo.TiingoCandle;
import com.scaler.mohit.stock.analyzer.pojo.Trade;

/**
 * @author mohit@interviewbit.com on 09/11/20
 **/
public class StockAnalyzer {

    /*
     *  This method reads the file and return list of all stocks.
     *   @Param: Filename (placed in resources folder)
     *   @Returns: List of stocks present in the file
     */
    public static List<String> readStocksFromFile(String fileName) throws IOException {
        Trade[] tradeList = readTradesFromFile(fileName);

        List<String> stocks = new ArrayList<>();
        for (Trade trade : tradeList)
            stocks.add(trade.getSymbol());

        return stocks;
    }

    /*
     *  This method read the file and return list of all
     *   stocks in asc order of prices on the given date.
     *   @Param: Filename (placed in resources folder)
     *   @Returns: List of stocks present in the file
     *
     *  NOTE: Please create a free account on Tiingo
     *      (https://api.tiingo.com/documentation/end-of-day)
     *      and use their API to find out the closing price
     */
    public static List<String> getStockSymbolsInAscPrice(String fileName, String endDate) throws IOException {
        Trade[] tradeList = readTradesFromFile(fileName);

        List<TiingoCandle> tiingoCandleList = new ArrayList<>();

        for (Trade trade : tradeList) {
            TiingoCandle tiingoCandle = getStockPriceFromAPI(trade.getSymbol(), endDate);
            tiingoCandle.setTicker(trade.getSymbol());
            tiingoCandleList.add(tiingoCandle);
        }

        Collections.sort(tiingoCandleList, Comparator.comparing(Candle::getClose));

        List<String> stocks = new ArrayList<>();
        for (TiingoCandle tiingoCandle : tiingoCandleList) {
            stocks.add(tiingoCandle.getTicker());
        }

        return stocks;
    }

    /*
     *  This method reads the file and return
     *   Annualized Return on each stock present in the file. Sort in desc order of gain.
     *   @Param: Filename (placed in resources folder)
     *   @Returns: List of stocks present in the file
     *
     *  Read about annualised return here: https://scripbox.com/mf/annualized-return/
     *  NOTE: Use Tiingo API for stock prices on specific date.
     *      If the price is not available on given endDate, consider price for a day before endDate.
     */
    public static List<AnnualizedReturn> getAnnualisedReturnsOnStocks(String fileName, String endDate) throws IOException {

        int years = 1;
        Trade[] tradeList = readTradesFromFile(fileName);

        List<AnnualizedReturn> annualisedReturns = new ArrayList<>();

        for (Trade trade : tradeList) {
            TiingoCandle tiingoCandle1 = getStockPriceFromAPI(trade.getSymbol(), subtractDaysFromDate(endDate, 365 * years));
            TiingoCandle tiingoCandle2 = getStockPriceFromAPI(trade.getSymbol(), endDate);

            double purchasePrice = tiingoCandle1.getClose();
            double currentPrice = tiingoCandle2.getClose();

            double simpleReturn = (currentPrice - purchasePrice) / purchasePrice;
            double annualReturn = Math.pow((1 + simpleReturn), years) - 1;

            annualisedReturns.add(new AnnualizedReturn(trade.getSymbol(), simpleReturn, annualReturn));
        }

        Collections.sort(annualisedReturns, Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed());

        return annualisedReturns;
    }

    public static void main(String[] args) {

    }

    private static Trade[] readTradesFromFile(String fileName) throws IOException {
        byte[] jsonData = Files.readAllBytes(Paths.get("src/main/resources/" + fileName));

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonData, Trade[].class);
    }

    private static TiingoCandle getStockPriceFromAPI(String stock, String date) throws IOException {

        StringBuilder urlBuilder = new StringBuilder("https://api.tiingo.com/tiingo/daily/");
        urlBuilder
                .append(stock)
                .append("/prices?startDate=").append(subtractDaysFromDate(date, 3))
                .append("&endDate=").append(date)
                .append("&token=")
                .append("770b8b195130ad5c76405b84651f6455d31f464b");

//        System.out.println("Hitting API with URL:: " + urlBuilder);

        URL url = new URL(urlBuilder.toString());

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.connect();

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder responseBuilder = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            responseBuilder.append(inputLine);
        }
        in.close();

        ObjectMapper mapper = new ObjectMapper();
        TiingoCandle[] tiingoCandle = mapper.readValue(responseBuilder.toString(), TiingoCandle[].class);

        return tiingoCandle[tiingoCandle.length - 1];
    }

    private static String subtractDaysFromDate(String strDate, int days) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        try {
            cal.setTime(sdf.parse(strDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        cal.add(Calendar.DATE, -1 * days);
        return sdf.format(cal.getTime());
    }
}
