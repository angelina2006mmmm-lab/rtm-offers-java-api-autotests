package dev.portfolio.rtm;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("RTM marketing offers")
@Feature("Обновление предложения")
class UpdateOfferTests extends BaseApiTest {

    @Test
    @DisplayName("PUT изменяет статус, изменение сохраняется")
    void shouldUpdateAndPersistStatus() {
        long id = existingOfferId();
        given().spec(specification).pathParam("id", id)
                .body(Map.of("status", "PAUSED"))
                .when().put("/{id}")
                .then().statusCode(200).body("status", equalTo("PAUSED"));

        given().spec(specification).pathParam("id", id)
                .when().get("/{id}")
                .then().statusCode(200).body("status", equalTo("PAUSED"));
    }

    @Test
    @DisplayName("Неверный статус → 400 без изменения данных")
    void shouldRejectBadStatusWithoutUpdating() {
        long id = existingOfferId();
        given().spec(specification).pathParam("id", id)
                .body(Map.of("status", "ARCHIVED"))
                .when().put("/{id}")
                .then().statusCode(400).body("error", equalTo("Unsupported status"));

        given().spec(specification).pathParam("id", id)
                .when().get("/{id}")
                .then().statusCode(200).body("status", equalTo("ACTIVE"));
    }

    @Test
    @DisplayName("Обновление несуществующего предложения → 404")
    void shouldReturn404ForUnknownOffer() {
        given().spec(specification).body(Map.of("status", "PAUSED"))
                .when().put("/99999")
                .then().statusCode(404).body("error", equalTo("Offer not found"));
    }
}
