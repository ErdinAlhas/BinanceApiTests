import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import services.MainService;
import services.MarketServices;

import java.util.HashMap;
import java.util.Map;

public class MarketTests extends MainService {
    private MainService mainService;
    private ResponseSpecification responseSpec;
    private MarketServices marketServices;

    @BeforeEach
    public void setup() {
        mainService = new MainService();
        marketServices = new MarketServices();
        responseSpec = new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectContentType("application/json")
                .build();
    }

    @Test
    public void testGetPingResponseValidation() {
        Map<String, Object> params = new HashMap<>();
        Response response = mainService.getPing(params, responseSpec);
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody().asString());

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
        Response response = marketServices.getServerTime(params, responseSpec);
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody().asString());
        assertThat(response.getStatusCode(), is(200));

        long binanceTime = response.jsonPath().getLong("serverTime");
        long localTimeStamp = System.currentTimeMillis();
        long tolerance = 500;

        Assertions.assertTrue(Math.abs(binanceTime-localTimeStamp) <= tolerance ,
                "Binance server saati local server dan farklı: " + (binanceTime - (localTimeStamp + tolerance)) + "ms");
    }

    @Test
    public void avgPriceControl(){
        Response response = marketServices.getAvgPrice("BNBUSDT", responseSpec);

        String price = response.jsonPath().getString("price");
        int mins = response.jsonPath().getInt("mins");
        long closeTime = response.jsonPath().getLong("closeTime");

        Assertions.assertNotNull("Price null olmamalı", price);
        Assertions.assertTrue(Double.parseDouble(price) > 0, "Price pozitif olmalı");
        Assertions.assertTrue(price.matches("\\d+\\.\\d+"),
                String.format("Price formatı hatalı! Gelen: '%s'", price));
        Assertions.assertEquals(5, mins,"Hatali deger" );
        Assertions.assertTrue(closeTime > 0, "CloseTime geçerli olmalı");

        long localTimeStamp = System.currentTimeMillis();
        Assertions.assertTrue(closeTime <= localTimeStamp, "Hatali, "+"close time: "+closeTime+" local time: "+localTimeStamp);

        response.then().log().body();
    }


}
