package dev.portfolio.rtm;

import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.util.Map;

import static io.restassured.RestAssured.given;

/** Fresh ephemeral HTTP service for EVERY test: no dependency on run order or shared state. */
abstract class BaseApiTest {
    private OfferApiServer server;
    protected RequestSpecification specification;

    @BeforeEach
    void startServer() throws IOException {
        server = new OfferApiServer();
        server.start();
        specification = new RequestSpecBuilder()
                .setBaseUri("http://127.0.0.1")
                .setPort(server.port())
                .setBasePath("/api/v1/offers")
                .setContentType(ContentType.JSON)
                .addFilter(new AllureRestAssured())
                .build();
    }

    @AfterEach
    void stopServer() {
        if (server != null) server.close();
    }

    @Step("Создать предложение клиенту {clientId} через канал {channel}")
    protected Response createOffer(String clientId, String channel) {
        return given().spec(specification)
                .body(Map.of("clientId", clientId, "campaignId", "autumn-campaign", "channel", channel))
                .when().post();
    }

    @Step("Создать предложение и получить его ID")
    protected long existingOfferId() {
        return createOffer("client-101", "SMS")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }
}
