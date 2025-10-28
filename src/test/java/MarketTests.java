import Modal.Trades;

import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import services.MainService;
import services.MarketServices;
import specifications.ResponseSpec;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MarketTests extends MainService {
    private MainService mainService;
    private MarketServices marketServices;

    @BeforeEach
    public void setup() {
        marketServices = new MarketServices();
        mainService = new MainService();
    }

    @Test
    public void testGetPingResponseValidation() {
        Map<String, Object> params = new HashMap<>();
        Response response = mainService.getPing(params, ResponseSpec.checkStatusCodeOk());
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
    public void testGetServerTimeValidation() {
        Map<String, Object> params = new HashMap<>();
        Response response = marketServices.getServerTime(params, ResponseSpec.checkStatusCodeOk());
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
    public void testAvgPriceControl(){
        Response response = marketServices.getAvgPrice("BNBUSDT", ResponseSpec.checkStatusCodeOk());

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
    public void testGetLastTradesSuccessfully(){
        Random random = new Random();
        int limit = random.nextInt(5) + 2;

        Response response = marketServices.getTrades("BNBUSDT", limit);
        List<Trades> trades = response.as(new TypeRef<>() {
        });
        Trades firstTrade = trades.get(0);
        Trades secondTrade = trades.get(1);
        double firstTradePrice = Double.parseDouble(firstTrade.price);
        double firstTradeQty = Double.parseDouble(firstTrade.qty);
        double firstTradeQuoteQty = Double.parseDouble(firstTrade.quoteQty);
        long localTimeStamp = System.currentTimeMillis();

        Assertions.assertEquals(response.getStatusCode(),SC_OK);
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
    public void testGetLastTradesWithInvalidSymbol() throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = marketServices.getTradesForWrongRequest("A",1);
        CloseableHttpResponse response = httpClient.execute(request);

        int statusCode = response.getStatusLine().getStatusCode();
        String body = EntityUtils.toString(response.getEntity());

        assertEquals(400, statusCode);
        assertTrue(body.contains("Invalid symbol"));
        response.close();
        httpClient.close();
    }
    @Test
    public void testGetLastTradesWithLimitEqualZero() throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = marketServices.getTradesForWrongRequest("BNBUSDT", 0);
        CloseableHttpResponse response = httpClient.execute(request);

        int statusCode = response.getStatusLine().getStatusCode();
        String body = EntityUtils.toString(response.getEntity());

        assertEquals(400, statusCode);
        assertTrue(body.contains("Mandatory parameter 'limit' was not sent, was empty/null, or malformed."));

        response.close();
        httpClient.close();
    }
    @Test
    public void testGetLastTradesWithNegativeLimit() throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = marketServices.getTradesForWrongRequest("BNBUSDT", -1);
        CloseableHttpResponse response = httpClient.execute(request);

        int statusCode = response.getStatusLine().getStatusCode();
        String body = EntityUtils.toString(response.getEntity());

        assertEquals(400, statusCode);
        assertTrue(body.contains("Illegal characters found in parameter 'limit'; legal range is '^[0-9]{1,20}$'."));

        response.close();
        httpClient.close();
    }
    @Test
    public void testGetLastTradesWithLowerSymbolChars() throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = marketServices.getTradesForWrongRequest("abc", 1);
        CloseableHttpResponse response = httpClient.execute(request);

        int statusCode = response.getStatusLine().getStatusCode();
        String body = EntityUtils.toString(response.getEntity());

        assertEquals(400, statusCode);
        System.out.println(body);
        assertTrue(body.contains("Illegal characters found in parameter 'symbol'; legal range is '^[A-Z0-9-_.]{1,20}$'."));

        response.close();
        httpClient.close();
    }
}
