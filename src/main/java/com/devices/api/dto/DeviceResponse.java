package com.devices.api.dto;

import com.devices.api.domain.DeviceState;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Schema(description = "Device response payload")
public class DeviceResponse {

    @Schema(description = "Unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Device name", example = "iPhone 15 Pro")
    private String name;

    @Schema(description = "Device brand", example = "Apple")
    private String brand;

    @Schema(description = "Device state", example = "AVAILABLE")
    private DeviceState state;

    @Schema(description = "Creation timestamp")
    private Instant createdAt;
}
