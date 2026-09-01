package com.devices.api.dto;

import com.devices.api.domain.DeviceState;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request payload for partially updating a device")
public class DevicePatchRequest {

    @Schema(description = "Device name", example = "Galaxy S24")
    private String name;

    @Schema(description = "Device brand", example = "Samsung")
    private String brand;

    @Schema(description = "Device state", example = "IN_USE")
    private DeviceState state;
}
