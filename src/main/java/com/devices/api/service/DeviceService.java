package com.devices.api.service;

import com.devices.api.domain.Device;
import com.devices.api.domain.DeviceState;
import com.devices.api.dto.DevicePatchRequest;
import com.devices.api.dto.DeviceRequest;
import com.devices.api.dto.DeviceResponse;
import com.devices.api.dto.PageResponse;
import com.devices.api.exception.DeviceConflictException;
import com.devices.api.exception.DeviceNotFoundException;
import com.devices.api.mapper.DeviceMapper;
import com.devices.api.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;

    @Transactional
    public DeviceResponse create(DeviceRequest request) {
        log.info("Creating device with name={}, brand={}", request.getName(), request.getBrand());
        Device device = deviceMapper.toEntity(request);
        Device saved = deviceRepository.saveAndFlush(device);
        return deviceMapper.toResponse(saved);
    }

    public DeviceResponse findById(UUID id) {
        return deviceMapper.toResponse(getDeviceOrThrow(id));
    }

    public PageResponse<DeviceResponse> findAll(String brand, DeviceState state, Pageable pageable) {
        log.info("Fetching devices - brand={}, state={}, page={}, size={}",
                brand, state, pageable.getPageNumber(), pageable.getPageSize());
        Page<Device> page = deviceRepository.findByFilters(brand, state, pageable);
        return PageResponse.of(page, deviceMapper::toResponse);
    }

    @Transactional
    public DeviceResponse update(UUID id, DeviceRequest request) {
        log.info("Full update of device id={}", id);
        Device device = getDeviceOrThrow(id);
        assertNotInUseForNameBrandChange(device, request.getName(), request.getBrand());
        deviceMapper.updateEntityFromRequest(request, device);
        return deviceMapper.toResponse(device);
    }

    @Transactional
    public DeviceResponse patch(UUID id, DevicePatchRequest request) {
        log.info("Partial update of device id={}", id);
        Device device = getDeviceOrThrow(id);
        assertNotInUseForNameBrandChange(device, request.getName(), request.getBrand());
        deviceMapper.patchEntityFromRequest(request, device);
        return deviceMapper.toResponse(device);
    }

    @Transactional
    public void delete(UUID id) {
        log.info("Deleting device id={}", id);
        Device device = getDeviceOrThrow(id);
        if (DeviceState.IN_USE == device.getState()) {
            throw new DeviceConflictException("Cannot delete a device that is currently in use");
        }
        deviceRepository.delete(device);
    }

    // --- helpers ---

    private Device getDeviceOrThrow(UUID id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));
    }

    private void assertNotInUseForNameBrandChange(Device device, String newName, String newBrand) {
        if (DeviceState.IN_USE != device.getState()) return;

        boolean nameChanging = newName != null && !newName.equals(device.getName());
        boolean brandChanging = newBrand != null && !newBrand.equals(device.getBrand());

        if (nameChanging || brandChanging) {
            throw new DeviceConflictException("Cannot update name or brand of a device that is currently in use");
        }
    }
}
