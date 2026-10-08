package dev.portfolio.rtm;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("RTM marketing offers")
@Feature("Получение предложений")
class GetOfferTests extends BaseApiTest {

    @Test
    @DisplayName("GET возвращает созданное предложение")
    void shouldGetExistingOffer() {
        long id = existingOfferId();
        given().spec(specification).pathParam("id", id)
                .when().get("/{id}")
                .then().statusCode(200)
                .body("id", equalTo((int) id),
                        "clientId", equalTo("client-101"),
                        "status", equalTo("ACTIVE"));
    }

    @Test
    @DisplayName("Несуществующее предложение → 404")
    void shouldReturn404ForUnknownOffer() {
        given().spec(specification)
                .when().get("/99999")
                .then().statusCode(404).body("error", equalTo("Offer not found"));
    }

    @Test
    @DisplayName("Фильтрация списка предложений по clientId")
    void shouldListOffersForSelectedClient() {
        createOffer("client-101", "SMS").then().statusCode(201);
        createOffer("client-202", "EMAIL").then().statusCode(201);
        createOffer("client-101", "PUSH").then().statusCode(201);

        given().spec(specification).queryParam("clientId", "client-101")
                .when().get()
                .then().statusCode(200)
                .body("size()", equalTo(2),
                        "clientId", everyItem(equalTo("client-101")),
                        "channel", containsInAnyOrder("SMS", "PUSH"));
    }

    @Test
    @DisplayName("Новая тестовая среда содержит пустой список")
    void shouldStartWithEmptyStore() {
        given().spec(specification).when().get()
                .then().statusCode(200).body("size()", equalTo(0));
    }
}
