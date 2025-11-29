import Modals.OrderBookModal;
import Modals.RecentTradesListModal;

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
    public void testGetConnectivity() {
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
                "Binance server time is different than local time : " + (binanceTime - (localTimeStamp + tolerance)) + "ms");
    }

    @Test
    public void testAvgPriceControl(){
        Response response = marketServices.getAvgPrice("BNBUSDT", ResponseSpec.checkStatusCodeOk());

        String price = response.jsonPath().getString("price");
        int mins = response.jsonPath().getInt("mins");
        long closeTime = response.jsonPath().getLong("closeTime");

        assertTrue(Double.parseDouble(price) > 0, "Price must be positive");
        assertTrue(price.matches("\\d+\\.\\d+"),
                String.format("Invalid Price Format! Actual: '%s'", price));
        assertEquals(5, mins,"Invalid mins" );
        assertTrue(closeTime > 0, "CloseTime is wrong");

        long localTimeStamp = System.currentTimeMillis();
        assertTrue(closeTime <= localTimeStamp, "Different than expected, "+"close time: "+closeTime+" local time: "+localTimeStamp);
        //response.then().log().body();
    }

    //@RepeatedTest(10)
    @Test
    public void testGetRecentTradesSuccessfully(){
        Random random = new Random();
        int limit = random.nextInt(5) + 2;

        Response response = marketServices.getTrades("BNBUSDT", limit);
        List<RecentTradesListModal> trades = response.as(new TypeRef<>() {
        });
        RecentTradesListModal firstTrade = trades.get(0);
        RecentTradesListModal secondTrade = trades.get(1);
        double firstTradePrice = Double.parseDouble(firstTrade.price);
        double firstTradeQty = Double.parseDouble(firstTrade.qty);
        double firstTradeQuoteQty = Double.parseDouble(firstTrade.quoteQty);
        long localTimeStamp = System.currentTimeMillis();

        Assertions.assertEquals(response.getStatusCode(),SC_OK);
        Assertions.assertNotNull(trades);
        Assertions.assertNotNull(firstTrade);
        Assertions.assertNotNull(secondTrade);
        assertEquals(limit, trades.size(), "Actual data is different than expected. Expected: "+limit+" Actual: "+trades.size());
        Assertions.assertTrue(firstTrade.time < localTimeStamp, "Invalid timestamp: "+firstTrade.time+" : "+localTimeStamp);
        Assertions.assertTrue(firstTrade.id > 1000000000, "Invalid id: "+firstTrade.id);
        Assertions.assertTrue(firstTradePrice > 0.0, "Price greater than zero : "+firstTradePrice);
        Assertions.assertTrue(firstTradeQty > 0.0, "Amount greater than zero: "+firstTradePrice);
        Assertions.assertTrue(firstTradeQuoteQty >= firstTradeQty, "The amount cannot be greater than the depth. Amount: "+firstTradePrice + "Depth: "+firstTradeQuoteQty);
        Assertions.assertNotNull(firstTrade.isBestMatch);
        Assertions.assertNotNull(firstTrade.isBuyerMaker);
        Assertions.assertTrue(firstTrade.id < secondTrade.id, "Sorting is wrong, first: "+firstTrade.id+" second: "+secondTrade.id);
        Assertions.assertTrue(firstTrade.time <= secondTrade.time, "Sorting is wrong, first: "+firstTrade.time+" second: "+secondTrade.time);
        //response.then().log().body();
    }

    @Test
    public void testGetRecentTradesForInvalidSymbol() throws Exception {
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
    public void testGetRecentTradesWithLimitEqualZero() throws Exception {
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
    public void testGetRecentTradesWithNegativeLimit() throws Exception {
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
    public void testGetRecentTradesWithLowercaseSymbol() throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = marketServices.getTradesForWrongRequest("abc", 1);
        CloseableHttpResponse response = httpClient.execute(request);

        int statusCode = response.getStatusLine().getStatusCode();
        String body = EntityUtils.toString(response.getEntity());

        assertEquals(400, statusCode);
        assertTrue(body.contains("Illegal characters found in parameter 'symbol'; legal range is '^[A-Z0-9-_.]{1,20}$'."));
        response.close();
        httpClient.close();
    }

    @Test
    public void testGetDepthSuccessfully() throws Exception {
        Random random = new Random();
        int limit = random.nextInt(5) + 2;
        Response response = marketServices.getDepth("BNBUSDT", limit);

        OrderBookModal orderBook = response.as(OrderBookModal.class);

        List<String> firstBid = orderBook.getBids().get(0);
        List<String> secondBid = orderBook.getBids().get(1);
        double firstBidPrice = Double.parseDouble(firstBid.get(0));
        double firstBidQuantity = Double.parseDouble(firstBid.get(1));
        double secondBidPrice = Double.parseDouble(secondBid.get(0));
        double secondBidQuantity = Double.parseDouble(secondBid.get(1));

        List<String> firstAsk = orderBook.getAsks().get(0);
        List<String> secondAsk = orderBook.getAsks().get(1);
        double firstAskPrice = Double.parseDouble(firstAsk.get(0));
        double firstAskQuantity = Double.parseDouble(firstAsk.get(1));
        double secondAskPrice = Double.parseDouble(secondAsk.get(0));
        double secondAskQuantity = Double.parseDouble(secondAsk.get(1));

        assertTrue(orderBook.getLastUpdateId() > 0, "Invalid Last Update Id");
        assertEquals(limit, orderBook.getBids().size(), "Bid size should be limit value");
        assertEquals(limit, orderBook.getAsks().size(), "Ask size should be limit value");
        assertTrue(firstAskPrice>firstBidPrice, "Depth values is wrong");
        assertTrue(secondAskPrice>firstAskPrice, "Depth values is wrong");
        assertTrue(firstBidPrice>secondBidPrice, "Depth values is wrong");
        assertTrue(firstBidQuantity>0.0 && firstAskQuantity>0.0 && secondAskQuantity>0.0 && secondBidQuantity>0.0,"Depth should be great than zero");
    }

    @Test
    public void testGetDepthForInvalidSymbol() throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = marketServices.getDepthForWrongRequest("A",1);
        CloseableHttpResponse response = httpClient.execute(request);

        int statusCode = response.getStatusLine().getStatusCode();
        String body = EntityUtils.toString(response.getEntity());

        assertEquals(400, statusCode);
        assertTrue(body.contains("Invalid symbol"));
        response.close();
        httpClient.close();
    }

    @Test
    public void testGetDepthWithLimitEqualZero() throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = marketServices.getDepthForWrongRequest("BNBUSDT", 0);
        CloseableHttpResponse response = httpClient.execute(request);

        int statusCode = response.getStatusLine().getStatusCode();
        String body = EntityUtils.toString(response.getEntity());

        assertEquals(400, statusCode);
        assertTrue(body.contains("Mandatory parameter 'limit' was not sent, was empty/null, or malformed."));
        response.close();
        httpClient.close();
    }

    @Test
    public void testDepthWithNegativeLimit() throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = marketServices.getDepthForWrongRequest("BNBUSDT", -1);
        CloseableHttpResponse response = httpClient.execute(request);

        int statusCode = response.getStatusLine().getStatusCode();
        String body = EntityUtils.toString(response.getEntity());

        assertEquals(400, statusCode);
        assertTrue(body.contains("Illegal characters found in parameter 'limit'; legal range is '^[0-9]{1,20}$'."));
        response.close();
        httpClient.close();
    }

    @Test
    public void testGetDepthForLowercaseSymbol() throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = marketServices.getDepthForWrongRequest("abc", 1);
        CloseableHttpResponse response = httpClient.execute(request);

        int statusCode = response.getStatusLine().getStatusCode();
        String body = EntityUtils.toString(response.getEntity());

        assertEquals(400, statusCode);
        assertTrue(body.contains("Illegal characters found in parameter 'symbol'; legal range is '^[A-Z0-9-_.]{1,20}$'."));
        response.close();
        httpClient.close();
    }
}
