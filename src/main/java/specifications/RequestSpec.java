package specifications;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.apache.http.client.methods.HttpGet;

public class RequestSpec {
    RequestSpecification requestSpecification;

    public RequestSpec(String baseUrl){
        requestSpecification = new RequestSpecBuilder()
                .setBaseUri(baseUrl)
                .setContentType("application/json")
                .build();
    }

    public RequestSpecification getRequestSpecification() {
        return requestSpecification;
    }

    public HttpGet getTradesForWrongRequest(String symbol, int limit){
        return new HttpGet("https://api.binance.com/api/v3/trades?symbol="+symbol+"&limit="+limit);
    }
}
