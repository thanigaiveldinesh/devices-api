package com.devices.api.controller;

import com.devices.api.domain.DeviceState;
import com.devices.api.dto.DevicePatchRequest;
import com.devices.api.dto.DeviceRequest;
import com.devices.api.dto.DeviceResponse;
import com.devices.api.dto.PageResponse;
import com.devices.api.exception.ErrorResponse;
import com.devices.api.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
@Tag(name = "Devices", description = "Device management API")
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new device",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Device created"),
                    @ApiResponse(responseCode = "400", description = "Invalid input",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    public ResponseEntity<DeviceResponse> create(@Valid @RequestBody DeviceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Fetch a device by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Device found"),
                    @ApiResponse(responseCode = "404", description = "Device not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    public DeviceResponse findById(@PathVariable UUID id) {
        return deviceService.findById(id);
    }

    @GetMapping
    @Operation(summary = "Fetch all devices with optional filters and pagination",
            parameters = {
                    @Parameter(name = "brand", description = "Filter by brand (case-insensitive)"),
                    @Parameter(name = "state", description = "Filter by state"),
                    @Parameter(name = "page", description = "Page number (0-based)", example = "0"),
                    @Parameter(name = "size", description = "Page size", example = "10"),
                    @Parameter(name = "sort", description = "Sort field and direction", example = "createdAt,desc")
            })
    public PageResponse<DeviceResponse> findAll(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) DeviceState state,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 &&
                sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

        return deviceService.findAll(brand, state, pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Fully update an existing device",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Device updated"),
                    @ApiResponse(responseCode = "404", description = "Device not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Update not allowed (device in use)",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    public DeviceResponse update(@PathVariable UUID id, @Valid @RequestBody DeviceRequest request) {
        return deviceService.update(id, request);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update an existing device",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Device patched"),
                    @ApiResponse(responseCode = "404", description = "Device not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Update not allowed (device in use)",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    public DeviceResponse patch(@PathVariable UUID id, @RequestBody DevicePatchRequest request) {
        return deviceService.patch(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a device",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Device deleted"),
                    @ApiResponse(responseCode = "404", description = "Device not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Cannot delete device in use",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deviceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
