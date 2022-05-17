package com.test.coding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CurrencyConverter {
    static class CurrencyRate {
        String fromCurrency;
        String toCurrency;
        double rate;

        public CurrencyRate(String fromCurrency, String toCurrency, double rate) {
            this.fromCurrency = fromCurrency;
            this.toCurrency = toCurrency;
            this.rate = rate;
        }
    }

    private static class GraphEdge implements Comparable<GraphEdge> {
        String currency;
        String parent;
        double rate;

        public GraphEdge(String parent, String currency, double rate) {
            this.currency = currency;
            this.parent = parent;
            this.rate = rate;
        }

        @Override
        public int compareTo(GraphEdge o) {
            if (this.rate == o.rate) {
                return 1;
            } else {
                return Double.compare(this.rate, o.rate);
            }
        }
    }

    HashMap<String, HashMap<String, Double>> graph = new HashMap<>();

    List<String[]> csvDataLines = new ArrayList<>();

    boolean isInvalidCurrency = false;

    private static final Logger LOG = LoggerFactory.getLogger(CurrencyConverter.class);

    /**
     * Main method containing logic for best path of the currency conversion
     * @param currentRates
     * @param fromCurrency
     * @param toCurrency
     * @param currencyCountryMap
     * @return
     */
    public void getBestRates(List<CurrencyRate> currentRates, String fromCurrency, String toCurrency,
                             Map<String, String> currencyCountryMap, int amountOfCAD) {

        // First build the graph of the currency rates
        buildGraph(currentRates);

        if (!graph.containsKey(fromCurrency)) {
            LOG.info("Invalid currency fromCurrency : "+fromCurrency);
            isInvalidCurrency = true;
        }

        Queue<GraphEdge> queue = new PriorityQueue<>();

        // The From currency will always start with a conversion rate of 1.0 and there is no parent currency
        queue.add(new GraphEdge(null, fromCurrency, 1.0));

        Map<String, Double> conversionMap = new HashMap<>();

        // This map will store the mapping of currencies and their parent currencies
        Map<String, String> parentCurrencyMap = new HashMap<>();

        parentCurrencyMap.put(fromCurrency, fromCurrency);
        /* Logic for best conversion rate :
        * 1. Build the graph of the currencies along with their child currency rates
        * 2. Get the parent currency from the top of the queue.
        * 3. Insert the parent currency rate into the conversionMap and also the parent currency into the parentCurrencyMap
        * 4. Fetch the rates of those currencies from the graph that was built
        *    and compare the rates fetched from graph to the conversionMap rates for the destination currency.
        *    If the compared rates are higher, insert the destination currency along with parent mapping to the top of queue */
        while (!queue.isEmpty()) {

            GraphEdge graphEdge = queue.poll();
            String topCurrency = graphEdge.currency;

            if (conversionMap.containsKey(topCurrency) && conversionMap.get(topCurrency) < graphEdge.rate) {
                continue;
            }

            conversionMap.put(topCurrency, graphEdge.rate);
            parentCurrencyMap.put(topCurrency, graphEdge.parent);
            graph.get(fromCurrency).put(topCurrency, graphEdge.rate);

            for (Map.Entry<String, Double>  dest : graph.get(topCurrency).entrySet()) {
                double rate = graph.get(fromCurrency).get(topCurrency) * dest.getValue();

                if (conversionMap.containsKey(dest.getKey()) && conversionMap.get(dest.getKey()) >= rate || dest.getKey().equals(fromCurrency)) {
                    continue;
                }
                queue.add(new GraphEdge(topCurrency, dest.getKey(), rate));
            }
        }


        if (!graph.get(fromCurrency).containsKey(toCurrency)) {
            LOG.info("Invalid currency toCurrency : "+toCurrency);
            isInvalidCurrency = true;
        }

        if(!isInvalidCurrency) {
            String bestCurrencyConversionPath = getBestConversionPath(parentCurrencyMap, fromCurrency, toCurrency);

            double bestRate = graph.get(fromCurrency).get(toCurrency);

            double amountOfCurrency = bestRate * amountOfCAD;

            csvDataLines.add(new String[]{toCurrency, currencyCountryMap.get(toCurrency), String.valueOf(amountOfCurrency), bestCurrencyConversionPath});
        }
        else{
            LOG.info("There is an invalid currency input");
        }
    }

    /**
     * This method creates the path for the best possible conversion rate as per the specification in the problem description
     * @param parentCurrencyMap - this is the parent currency map
     * @param fromCurrency - this is the from currency
     * @param toCurrency - this is the to currency
     * @return String of the path traversed with pipe (|) delimited
     */
    private String getBestConversionPath(Map<String, String> parentCurrencyMap, String fromCurrency, String toCurrency) {
        String current = toCurrency;
        Stack<String> res = new Stack<>();
        res.add(toCurrency);
        while (!parentCurrencyMap.get(current).equals(fromCurrency)) {
            current = parentCurrencyMap.get(current);
            res.add(current);
        }

        StringBuilder stringBuilder = new StringBuilder();
        res.add(fromCurrency);

        while (res.size() != 0) {

            stringBuilder.append(res.pop());
            if (res.size() > 0) {
                stringBuilder.append(" | ");
            }
        }
        return stringBuilder.toString();
    }

    /**
     * This method is used to build the graph of the currencies and their child currencies with rates
     * @param rates - List of the CurrencyRate object
     */
    public void buildGraph(List<CurrencyRate> rates) {

        for (CurrencyRate rate : rates) {
            if (!graph.containsKey(rate.fromCurrency)) {
                graph.put(rate.fromCurrency, new HashMap<>());
            }

            graph.get(rate.fromCurrency).put(rate.fromCurrency, 1.0);

            graph.get(rate.fromCurrency).put(rate.toCurrency, rate.rate);
        }

        for (CurrencyRate rate : rates) {
            for (Map.Entry<String, Double> val: graph.get(rate.fromCurrency).entrySet()) {
                if (!graph.containsKey(val.getKey())) {
                    graph.put(val.getKey(), new HashMap<>());
                }

                graph.get(val.getKey()).put(val.getKey(), 1.0);

                if (!graph.get(val.getKey()).containsKey(rate.fromCurrency)) {
                    graph.get(val.getKey()).put(rate.fromCurrency, 1.0 / val.getValue());
                }
            }
        }
        LOG.info("After building graph : "+graph);
    }

    /**
     * Method to write to CSV file
     */
    public void writeToCsv(){

        File csvOutputFile = new File("D:\\NeoFinancial.csv");
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            csvDataLines.stream()
                        .map(this::convertToCSV)
                        .forEach(pw::println);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Method to convert String array to csv
     * @param data
     * @return
     */
    private String convertToCSV(String[] data) {
        return Stream.of(data)
                .collect(Collectors.joining(","));
    }
}