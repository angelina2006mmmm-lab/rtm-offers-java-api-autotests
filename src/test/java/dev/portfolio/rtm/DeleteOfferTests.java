package dev.portfolio.rtm;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("RTM marketing offers")
@Feature("Удаление предложения")
class DeleteOfferTests extends BaseApiTest {

    @Test
    @DisplayName("DELETE возвращает 204, после удаления GET → 404")
    void shouldDeleteOffer() {
        long id = existingOfferId();
        given().spec(specification).pathParam("id", id)
                .when().delete("/{id}")
                .then().statusCode(204).body(is(emptyOrNullString()));

        given().spec(specification).pathParam("id", id)
                .when().get("/{id}")
                .then().statusCode(404);
    }

    @Test
    @DisplayName("Повторное удаление → 404")
    void shouldReturn404OnRepeatedDelete() {
        long id = existingOfferId();
        given().spec(specification).pathParam("id", id).when().delete("/{id}")
                .then().statusCode(204);
        given().spec(specification).pathParam("id", id).when().delete("/{id}")
                .then().statusCode(404).body("error", equalTo("Offer not found"));
    }

    @Test
    @DisplayName("Невалидный идентификатор → 400")
    void shouldRejectInvalidId() {
        given().spec(specification)
                .when().get("/not-a-number")
                .then().statusCode(400).body("error", equalTo("Invalid offer ID"));
    }
}
