package dev.portfolio.rtm;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("RTM marketing offers")
@Feature("Создание предложения")
class CreateOfferTests extends BaseApiTest {

    @Test
    @Story("Позитивный сценарий")
    @DisplayName("POST создаёт предложение и возвращает 201 с данными")
    void shouldCreateOffer() {
        createOffer("client-101", "PUSH")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("id", greaterThan(0),
                        "clientId", equalTo("client-101"),
                        "campaignId", equalTo("autumn-campaign"),
                        "channel", equalTo("PUSH"),
                        "status", equalTo("ACTIVE"));
    }

    static Stream<Arguments> invalidRequests() {
        return Stream.of(
                Arguments.of("нет clientId", Map.of("campaignId", "autumn", "channel", "SMS")),
                Arguments.of("пустой clientId", Map.of("clientId", "  ", "campaignId", "autumn", "channel", "SMS")),
                Arguments.of("нет campaignId", Map.of("clientId", "c1", "channel", "SMS")),
                Arguments.of("неизвестный channel", Map.of("clientId", "c1", "campaignId", "autumn", "channel", "FAX"))
        );
    }

    @ParameterizedTest(name = "Негативный сценарий: {0}")
    @MethodSource("invalidRequests")
    @Story("Валидация данных")
    void shouldRejectInvalidRequest(String caseName, Map<String, String> request) {
        given().spec(specification).body(request)
                .when().post()
                .then().statusCode(400).body("error", not(emptyOrNullString()));
    }

    @Test
    @DisplayName("Некорректный JSON → 400")
    void shouldRejectMalformedJson() {
        given().spec(specification).body("{not valid JSON")
                .when().post()
                .then().statusCode(400).body("error", equalTo("Malformed JSON"));
    }

    @Test
    @DisplayName("Неверный Content-Type → 415")
    void shouldRejectUnsupportedContentType() {
        given().spec(specification).contentType("text/plain").body("plain text")
                .when().post()
                .then().statusCode(415).body("error", containsString("Content-Type"));
    }
}
