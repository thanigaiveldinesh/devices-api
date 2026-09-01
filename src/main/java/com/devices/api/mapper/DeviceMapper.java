package com.devices.api.mapper;

import com.devices.api.domain.Device;
import com.devices.api.dto.DeviceRequest;
import com.devices.api.dto.DevicePatchRequest;
import com.devices.api.dto.DeviceResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DeviceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Device toEntity(DeviceRequest request);

    DeviceResponse toResponse(Device device);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromRequest(DeviceRequest request, @MappingTarget Device device);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void patchEntityFromRequest(DevicePatchRequest request, @MappingTarget Device device);
}
