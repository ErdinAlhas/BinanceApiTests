package services;

import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import specifications.RequestSpec;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class MarketServices extends RequestSpec {
    public MarketServices() {
        super("https://api.binance.com");
    }

    public Response getAvgPrice(String symbol, ResponseSpecification responseSpecification){
        return given()
                .spec(super.getRequestSpecification())
                .queryParams("symbol",symbol)
                .get("/api/v3/avgPrice")
                .then()
                .spec(responseSpecification)
                .extract()
                .response();
    }
    public Response getServerTime(Map<String,Object> params, ResponseSpecification responseSpecification){
        return given()
                .spec(super.getRequestSpecification())
                .queryParams(params)
                .get("/api/v3/time")
                .then()
                .spec(responseSpecification)
                .extract()
                .response();
    }

    public Response getTrades(String symbol, int limit){
        return given()
                .spec(super.getRequestSpecification())
                .queryParams("symbol",symbol)
                .queryParams("limit",limit)
                .when()
                .get("/api/v3/trades")
                .then()
                .extract()
                .response();
    }

}
