package com.devices.api.dto;

import com.devices.api.domain.DeviceState;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request payload for creating a device")
public class DeviceRequest {

    @NotBlank(message = "Name is required")
    @Schema(description = "Device name", example = "iPhone 15 Pro")
    private String name;

    @NotBlank(message = "Brand is required")
    @Schema(description = "Device brand", example = "Apple")
    private String brand;

    @NotNull(message = "State is required")
    @Schema(description = "Device state", example = "AVAILABLE")
    private DeviceState state;
}
