package com.scaler.mohit.stock.analyzer.pojo;

import lombok.Data;

import java.time.LocalDate;
import java.util.Comparator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author mohit@interviewbit.com on 09/11/20
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TiingoCandle implements Candle {
    private Double open;
    private Double close;
    private Double high;
    private Double low;
    private String ticker;
}
