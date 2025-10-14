package services;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import specifications.RequestSpec;

import java.util.Map;

public class MarketServices extends RequestSpec {
    public MarketServices() {
        super("https://api.binance.com");
    }

    public Response getAvgPrice(String symbol, ResponseSpecification responseSpecification){
        return RestAssured.given()
                .spec(super.getRequestSpecification())
                .queryParams("symbol",symbol)
                .get("/api/v3/avgPrice")
                .then()
                .spec(responseSpecification)
                .extract()
                .response();
    }
    public Response getServerTime(Map<String,Object> params, ResponseSpecification responseSpecification){
        return RestAssured.given()
                .spec(super.getRequestSpecification())
                .queryParams(params)
                .get("/api/v3/time")
                .then()
                .spec(responseSpecification)
                .extract()
                .response();
    }
}
