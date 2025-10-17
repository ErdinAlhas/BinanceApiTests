import Modal.Trades;
import io.restassured.RestAssured;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.client.HttpResponseException;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import services.MainService;
import services.MarketServices;
import specifications.ResponseSpec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MarketTests extends MainService {
    private MainService mainService;
    private MarketServices marketServices;
    private ResponseSpecification successResponseSpec;
    private ResponseSpecification badRequestResponseSpec;

    @BeforeEach
    public void setup() {
        mainService = new MainService();
        marketServices = new MarketServices();

        successResponseSpec = ResponseSpec.checkStatusCodeOk();
        badRequestResponseSpec = ResponseSpec.checkBadRequest();
    }

    @Test
    public void testGetPingResponseValidation() {
        Map<String, Object> params = new HashMap<>();
        Response response = mainService.getPing(params, successResponseSpec);
        assertNotNull(response);
        assertNotNull(response.getBody().asString());

        response.then()
                .statusCode(200)
                .log().ifValidationFails()
                .time(lessThan(1000L))
                .header("Content-Type", containsString("application/json"));
    }

    //@RepeatedTest(10)
    @Test
    public void testGetTestResponseValidation() {
        Map<String, Object> params = new HashMap<>();
        Response response = marketServices.getServerTime(params, successResponseSpec);
        assertNotNull(response);
        assertNotNull(response.getBody().asString());
        assertThat(response.getStatusCode(), is(200));

        long binanceTime = response.jsonPath().getLong("serverTime");
        long localTimeStamp = System.currentTimeMillis();
        long tolerance = 500;

        assertTrue(Math.abs(binanceTime-localTimeStamp) <= tolerance ,
                "Binance server saati local server dan farklı: " + (binanceTime - (localTimeStamp + tolerance)) + "ms");
    }

    @Test
    public void avgPriceControl(){
        Response response = marketServices.getAvgPrice("BNBUSDT", successResponseSpec);

        String price = response.jsonPath().getString("price");
        int mins = response.jsonPath().getInt("mins");
        long closeTime = response.jsonPath().getLong("closeTime");

        assertNotNull("Price null olmamalı", price);
        assertTrue(Double.parseDouble(price) > 0, "Price pozitif olmalı");
        assertTrue(price.matches("\\d+\\.\\d+"),
                String.format("Price formatı hatalı! Gelen: '%s'", price));
        assertEquals(5, mins,"Hatali deger" );
        assertTrue(closeTime > 0, "CloseTime geçerli olmalı");

        long localTimeStamp = System.currentTimeMillis();
        assertTrue(closeTime <= localTimeStamp, "Hatali, "+"close time: "+closeTime+" local time: "+localTimeStamp);

        response.then().log().body();
    }

    //@RepeatedTest(10)
    @Test
    public void TestGetLastTrades(){
        Random random = new Random();
        int limit = random.nextInt(5) + 2;

        Response response = marketServices.getTrades("BNBUSDT", limit, successResponseSpec);
        List<Trades> trades = response.as(new TypeRef<>() {
        });
        Trades firstTrade = trades.get(0);
        Trades secondTrade = trades.get(1);
        double firstTradePrice = Double.parseDouble(firstTrade.price);
        double firstTradeQty = Double.parseDouble(firstTrade.qty);
        double firstTradeQuoteQty = Double.parseDouble(firstTrade.quoteQty);
        long localTimeStamp = System.currentTimeMillis();

        Assertions.assertNotNull(trades);
        Assertions.assertNotNull(firstTrade);
        Assertions.assertNotNull(secondTrade);
        assertEquals(limit, trades.size(), "Gelen veri beklenenden farklı. Beklenen: "+limit+" Gelen: "+trades.size());
        Assertions.assertTrue(firstTrade.time < localTimeStamp, "Hatalı zaman degeri: "+firstTrade.time+" : "+localTimeStamp);
        Assertions.assertTrue(firstTrade.id > 1000000000, "Hatalı id degeri: "+firstTrade.id);
        Assertions.assertTrue(firstTradePrice > 0.0, "Fiyat 0 dan büyük olmalı: "+firstTradePrice);
        Assertions.assertTrue(firstTradeQty > 0.0, "Miktar 0 dan büyük olmalı: "+firstTradePrice);
        Assertions.assertTrue(firstTradeQuoteQty >= firstTradeQty, "Miktar derinlikten büyük olamaz. Miktar: "+firstTradePrice + "Derinlik: "+firstTradeQuoteQty);
        Assertions.assertNotNull(firstTrade.isBestMatch);
        Assertions.assertNotNull(firstTrade.isBuyerMaker);
        Assertions.assertTrue(firstTrade.id < secondTrade.id, "Sıralama hatali, first: "+firstTrade.id+" second: "+secondTrade.id);
        Assertions.assertTrue(firstTrade.time <= secondTrade.time, "Sıralama hatali, first: "+firstTrade.time+" second: "+secondTrade.time);
        response.then().log().body();
    }

    @Test
    public void TestShouldNotLimitValuesIsLessOrEqualThanZero(){
        int limit = 0;
        //Bu yapı iyileştirilecek
        Throwable exception = assertThrows(Throwable.class, () -> {
            marketServices.getTrades("BNBUSDT", limit, successResponseSpec);
        });
        String message = exception.getMessage();
        assertTrue(message.contains("400") || message.contains("Bad Request"),
                "Exception 400 hatasını içermeli: " + message);
        System.out.println("✅ Beklenen exception fırlatıldı: " + message);
        int limit2 = -1;
        Throwable exception2 = assertThrows(Throwable.class, () -> {
            marketServices.getTrades("BNBUSDT", limit2, successResponseSpec);
        });
        String message2 = exception2.getMessage();
        assertTrue(message2.contains("400") || message2.contains("Bad Request"),
                "Exception 400 hatasını içermeli: " + message);
        System.out.println("✅ Beklenen exception fırlatıldı: " + message);
    }

}
