package com.devices.api.integration;

import com.devices.api.domain.Device;
import com.devices.api.domain.DeviceState;
import com.devices.api.repository.DeviceRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class DeviceControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private DeviceRepository deviceRepository;

    @AfterEach
    void cleanUp() {
        deviceRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /devices")
    class CreateDevice {

        @Test
        @DisplayName("should create device and return 201")
        void shouldCreate() {
            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("name", "iPhone 15", "brand", "Apple", "state", "AVAILABLE"))
            .when()
                    .post("/devices")
            .then()
                    .statusCode(201)
                    .body("id", notNullValue())
                    .body("name", equalTo("iPhone 15"))
                    .body("brand", equalTo("Apple"))
                    .body("state", equalTo("AVAILABLE"))
                    .body("createdAt", notNullValue());
        }

        @Test
        @DisplayName("should return 400 when name is missing")
        void shouldReturn400WhenNameMissing() {
            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("brand", "Apple", "state", "AVAILABLE"))
            .when()
                    .post("/devices")
            .then()
                    .statusCode(400)
                    .body("errors", hasSize(greaterThan(0)));
        }
    }

    @Nested
    @DisplayName("GET /devices/{id}")
    class GetDevice {

        @Test
        @DisplayName("should return device when exists")
        void shouldReturnDevice() {
            Device saved = deviceRepository.save(buildDevice("Pixel 8", "Google", DeviceState.AVAILABLE));

            given()
            .when()
                    .get("/devices/" + saved.getId())
            .then()
                    .statusCode(200)
                    .body("id", equalTo(saved.getId().toString()))
                    .body("name", equalTo("Pixel 8"));
        }

        @Test
        @DisplayName("should return 404 when device not found")
        void shouldReturn404() {
            given()
            .when()
                    .get("/devices/00000000-0000-0000-0000-000000000000")
            .then()
                    .statusCode(404);
        }
    }

    @Nested
    @DisplayName("GET /devices")
    class GetAllDevices {

        @Test
        @DisplayName("should return all devices paginated")
        void shouldReturnAll() {
            deviceRepository.save(buildDevice("iPhone 15", "Apple", DeviceState.AVAILABLE));
            deviceRepository.save(buildDevice("Galaxy S24", "Samsung", DeviceState.IN_USE));

            given()
            .when()
                    .get("/devices")
            .then()
                    .statusCode(200)
                    .body("content", hasSize(2))
                    .body("totalElements", equalTo(2))
                    .body("page", equalTo(0));
        }

        @Test
        @DisplayName("should filter by brand")
        void shouldFilterByBrand() {
            deviceRepository.save(buildDevice("iPhone 15", "Apple", DeviceState.AVAILABLE));
            deviceRepository.save(buildDevice("Galaxy S24", "Samsung", DeviceState.AVAILABLE));

            given()
                    .queryParam("brand", "Apple")
            .when()
                    .get("/devices")
            .then()
                    .statusCode(200)
                    .body("content", hasSize(1))
                    .body("content[0].brand", equalTo("Apple"));
        }

        @Test
        @DisplayName("should filter by state")
        void shouldFilterByState() {
            deviceRepository.save(buildDevice("iPhone 15", "Apple", DeviceState.AVAILABLE));
            deviceRepository.save(buildDevice("Galaxy S24", "Samsung", DeviceState.IN_USE));

            given()
                    .queryParam("state", "IN_USE")
            .when()
                    .get("/devices")
            .then()
                    .statusCode(200)
                    .body("content", hasSize(1))
                    .body("content[0].state", equalTo("IN_USE"));
        }

        @Test
        @DisplayName("should filter by brand and state combined")
        void shouldFilterByBrandAndState() {
            deviceRepository.save(buildDevice("iPhone 15", "Apple", DeviceState.AVAILABLE));
            deviceRepository.save(buildDevice("iPhone 14", "Apple", DeviceState.IN_USE));
            deviceRepository.save(buildDevice("Galaxy S24", "Samsung", DeviceState.AVAILABLE));

            given()
                    .queryParam("brand", "Apple")
                    .queryParam("state", "AVAILABLE")
            .when()
                    .get("/devices")
            .then()
                    .statusCode(200)
                    .body("content", hasSize(1))
                    .body("content[0].name", equalTo("iPhone 15"));
        }

        @Test
        @DisplayName("should respect pagination parameters")
        void shouldPaginate() {
            deviceRepository.save(buildDevice("iPhone 15", "Apple", DeviceState.AVAILABLE));
            deviceRepository.save(buildDevice("Galaxy S24", "Samsung", DeviceState.AVAILABLE));
            deviceRepository.save(buildDevice("Pixel 8", "Google", DeviceState.AVAILABLE));

            given()
                    .queryParam("page", "0")
                    .queryParam("size", "2")
            .when()
                    .get("/devices")
            .then()
                    .statusCode(200)
                    .body("content", hasSize(2))
                    .body("totalElements", equalTo(3))
                    .body("totalPages", equalTo(2))
                    .body("last", equalTo(false));
        }
    }

    @Nested
    @DisplayName("PUT /devices/{id}")
    class UpdateDevice {

        @Test
        @DisplayName("should fully update device")
        void shouldFullyUpdate() {
            Device saved = deviceRepository.save(buildDevice("Old Name", "Old Brand", DeviceState.AVAILABLE));

            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("name", "New Name", "brand", "New Brand", "state", "INACTIVE"))
            .when()
                    .put("/devices/" + saved.getId())
            .then()
                    .statusCode(200)
                    .body("name", equalTo("New Name"))
                    .body("brand", equalTo("New Brand"))
                    .body("state", equalTo("INACTIVE"));
        }

        @Test
        @DisplayName("should return 409 when updating name of in-use device")
        void shouldReturn409WhenInUse() {
            Device saved = deviceRepository.save(buildDevice("iPhone 15", "Apple", DeviceState.IN_USE));

            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("name", "Changed Name", "brand", "Apple", "state", "IN_USE"))
            .when()
                    .put("/devices/" + saved.getId())
            .then()
                    .statusCode(409);
        }

        @Test
        @DisplayName("should not update createdAt field")
        void shouldNotUpdateCreatedAt() {
            Device saved = deviceRepository.save(buildDevice("iPhone 15", "Apple", DeviceState.AVAILABLE));
            String originalCreatedAt = given()
                    .when().get("/devices/" + saved.getId())
                    .then().extract().path("createdAt");

            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("name", "iPhone 15 Pro", "brand", "Apple", "state", "AVAILABLE"))
            .when()
                    .put("/devices/" + saved.getId())
            .then()
                    .statusCode(200)
                    .body("createdAt", equalTo(originalCreatedAt));
        }
    }

    @Nested
    @DisplayName("PATCH /devices/{id}")
    class PatchDevice {

        @Test
        @DisplayName("should partially update device")
        void shouldPatch() {
            Device saved = deviceRepository.save(buildDevice("Pixel 8", "Google", DeviceState.AVAILABLE));

            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("state", "INACTIVE"))
            .when()
                    .patch("/devices/" + saved.getId())
            .then()
                    .statusCode(200)
                    .body("state", equalTo("INACTIVE"))
                    .body("name", equalTo("Pixel 8"))
                    .body("brand", equalTo("Google"));
        }

        @Test
        @DisplayName("should return 409 when patching name of in-use device")
        void shouldReturn409WhenInUse() {
            Device saved = deviceRepository.save(buildDevice("Pixel 8", "Google", DeviceState.IN_USE));

            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("name", "Pixel 9"))
            .when()
                    .patch("/devices/" + saved.getId())
            .then()
                    .statusCode(409);
        }
    }

    @Nested
    @DisplayName("DELETE /devices/{id}")
    class DeleteDevice {

        @Test
        @DisplayName("should delete device and return 204")
        void shouldDelete() {
            Device saved = deviceRepository.save(buildDevice("Pixel 8", "Google", DeviceState.AVAILABLE));

            given()
            .when()
                    .delete("/devices/" + saved.getId())
            .then()
                    .statusCode(204);
        }

        @Test
        @DisplayName("should return 409 when deleting in-use device")
        void shouldReturn409WhenInUse() {
            Device saved = deviceRepository.save(buildDevice("Pixel 8", "Google", DeviceState.IN_USE));

            given()
            .when()
                    .delete("/devices/" + saved.getId())
            .then()
                    .statusCode(409);
        }

        @Test
        @DisplayName("should return 404 when device not found")
        void shouldReturn404() {
            given()
            .when()
                    .delete("/devices/00000000-0000-0000-0000-000000000000")
            .then()
                    .statusCode(404);
        }
    }

    private Device buildDevice(String name, String brand, DeviceState state) {
        return Device.builder()
                .name(name)
                .brand(brand)
                .state(state)
                .build();
    }
}
