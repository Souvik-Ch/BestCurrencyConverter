package com.test.coding;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@SpringBootApplication
public class NeoFinancialCurrencyConversion implements CommandLineRunner{

	public static void main(String[] args) {
		SpringApplication.run(NeoFinancialCurrencyConversion.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		String fromCurrency = "CAD"; // from currency is always CAD

		int amountOfCAD = 100; // we are trying with CAD 100 for best conversion

		Map<String, String> currencyCountryMap = new HashMap<>();

		// Code to fetch the Json response from the NEO Financial API
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> json = restTemplate.getForEntity("https://api-coding-challenge.neofinancial.com/currency-conversion?seed=89474", String.class);
		ObjectMapper mapper = new ObjectMapper();
		List<ConversionRates> conversionRatesList = mapper.reader()
				                                          .forType(new TypeReference<List<ConversionRates>>() {})
				                                          .readValue(json.getBody());
		System.out.println(conversionRatesList);

		CurrencyConverter currencyConverter = new CurrencyConverter();

		List<CurrencyConverter.CurrencyRate> currentRates = new ArrayList<>();

		for(ConversionRates conversionRates : conversionRatesList){
			currentRates.add(new CurrencyConverter.CurrencyRate(conversionRates.getFromCurrencyCode(),
					                                            conversionRates.getToCurrencyCode(),
					                                            conversionRates.getExchangeRate()));

			if (conversionRates.getToCurrencyCode() != "CAD") {
				currencyCountryMap.put(conversionRates.getToCurrencyCode(), conversionRates.getToCurrencyName());
			}
		}

		// First build the graph of the currency rates
		currencyConverter.buildGraph(currentRates);

		for (String toCurrency : currencyCountryMap.keySet()){
			System.out.println("To currency code : "+toCurrency);
			currencyConverter.getBestRates(currentRates, fromCurrency, toCurrency, currencyCountryMap, amountOfCAD);
		}

		currencyConverter.writeToCsv();
	}
}
