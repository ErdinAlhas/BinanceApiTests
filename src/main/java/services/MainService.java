package services;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import specifications.RequestSpec;

import java.util.Map;

public class MainService extends RequestSpec {

    public MainService() {
        super("https://api.binance.com");
    }

    public Response get(Map<String,Object> params, ResponseSpecification responseSpecification){
        return RestAssured.given()
                .spec(super.getRequestSpecification())
                .queryParams(params)
                .get()
                .then()
                .spec(responseSpecification)
                .extract()
                .response();
    }

    public Response getPing(Map<String,Object> params, ResponseSpecification responseSpecification){
        return RestAssured.given()
                .spec(super.getRequestSpecification())
                .queryParams(params)
                .get("/api/v3/ping")
                .then()
                .spec(responseSpecification)
                .extract()
                .response();
    }
}
