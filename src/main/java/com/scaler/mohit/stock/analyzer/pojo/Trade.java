package com.scaler.mohit.stock.analyzer.pojo;

import lombok.Data;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author mohit@interviewbit.com on 09/11/20
 **/
@Data
public class Trade {
    public static enum TradeType {
        BUY,
        SELL
    }

    private String symbol;
    private int quantity;
    private TradeType tradeType;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date purchaseDate;
}
