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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceMapper deviceMapper;

    @InjectMocks
    private DeviceService deviceService;

    private UUID deviceId;
    private Device device;
    private DeviceResponse deviceResponse;

    @BeforeEach
    void setUp() {
        deviceId = UUID.randomUUID();
        device = Device.builder()
                .id(deviceId)
                .name("iPhone 15")
                .brand("Apple")
                .state(DeviceState.AVAILABLE)
                .createdAt(Instant.now())
                .build();

        deviceResponse = new DeviceResponse();
        deviceResponse.setId(deviceId);
        deviceResponse.setName("iPhone 15");
        deviceResponse.setBrand("Apple");
        deviceResponse.setState(DeviceState.AVAILABLE);
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should create device successfully")
        void shouldCreateDevice() {
            DeviceRequest request = new DeviceRequest();
            request.setName("iPhone 15");
            request.setBrand("Apple");
            request.setState(DeviceState.AVAILABLE);

            when(deviceMapper.toEntity(request)).thenReturn(device);
            when(deviceRepository.saveAndFlush(device)).thenReturn(device);  // changed from save to saveAndFlush
            when(deviceMapper.toResponse(device)).thenReturn(deviceResponse);

            DeviceResponse result = deviceService.create(request);

            assertThat(result).isEqualTo(deviceResponse);
            verify(deviceRepository).saveAndFlush(device);  // changed from save to saveAndFlush
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return device when found")
        void shouldReturnDevice() {
            when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
            when(deviceMapper.toResponse(device)).thenReturn(deviceResponse);

            DeviceResponse result = deviceService.findById(deviceId);

            assertThat(result).isEqualTo(deviceResponse);
        }

        @Test
        @DisplayName("should throw DeviceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> deviceService.findById(deviceId))
                    .isInstanceOf(DeviceNotFoundException.class)
                    .hasMessageContaining(deviceId.toString());
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("should return all devices paginated")
        void shouldReturnAll() {
            Pageable pageable = PageRequest.of(0, 10);
            when(deviceRepository.findByFilters(null, null, pageable))
                    .thenReturn(new PageImpl<>(List.of(device)));
            when(deviceMapper.toResponse(device)).thenReturn(deviceResponse);

            PageResponse<DeviceResponse> result = deviceService.findAll(null, null, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("should filter by brand")
        void shouldFilterByBrand() {
            Pageable pageable = PageRequest.of(0, 10);
            when(deviceRepository.findByFilters("Apple", null, pageable))
                    .thenReturn(new PageImpl<>(List.of(device)));
            when(deviceMapper.toResponse(device)).thenReturn(deviceResponse);

            PageResponse<DeviceResponse> result = deviceService.findAll("Apple", null, pageable);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("should filter by state")
        void shouldFilterByState() {
            Pageable pageable = PageRequest.of(0, 10);
            when(deviceRepository.findByFilters(null, DeviceState.AVAILABLE, pageable))
                    .thenReturn(new PageImpl<>(List.of(device)));
            when(deviceMapper.toResponse(device)).thenReturn(deviceResponse);

            PageResponse<DeviceResponse> result = deviceService.findAll(null, DeviceState.AVAILABLE, pageable);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("should filter by brand and state combined")
        void shouldFilterByBrandAndState() {
            Pageable pageable = PageRequest.of(0, 10);
            when(deviceRepository.findByFilters("Apple", DeviceState.AVAILABLE, pageable))
                    .thenReturn(new PageImpl<>(List.of(device)));
            when(deviceMapper.toResponse(device)).thenReturn(deviceResponse);

            PageResponse<DeviceResponse> result = deviceService.findAll("Apple", DeviceState.AVAILABLE, pageable);

            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("update (PUT)")
    class Update {

        @Test
        @DisplayName("should update device when not in use")
        void shouldUpdateWhenNotInUse() {
            DeviceRequest request = new DeviceRequest();
            request.setName("iPhone 15 Pro");
            request.setBrand("Apple");
            request.setState(DeviceState.INACTIVE);

            when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
            when(deviceMapper.toResponse(device)).thenReturn(deviceResponse);

            deviceService.update(deviceId, request);

            verify(deviceMapper).updateEntityFromRequest(request, device);
        }

        @Test
        @DisplayName("should throw conflict when changing name of in-use device")
        void shouldThrowWhenInUseAndNameChanging() {
            device.setState(DeviceState.IN_USE);

            DeviceRequest request = new DeviceRequest();
            request.setName("Different Name");
            request.setBrand("Apple");
            request.setState(DeviceState.IN_USE);

            when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

            assertThatThrownBy(() -> deviceService.update(deviceId, request))
                    .isInstanceOf(DeviceConflictException.class)
                    .hasMessageContaining("in use");
        }

        @Test
        @DisplayName("should throw conflict when changing brand of in-use device")
        void shouldThrowWhenInUseAndBrandChanging() {
            device.setState(DeviceState.IN_USE);

            DeviceRequest request = new DeviceRequest();
            request.setName("iPhone 15");
            request.setBrand("Samsung");
            request.setState(DeviceState.IN_USE);

            when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

            assertThatThrownBy(() -> deviceService.update(deviceId, request))
                    .isInstanceOf(DeviceConflictException.class);
        }

        @Test
        @DisplayName("should allow state-only update on in-use device")
        void shouldAllowStateOnlyUpdateOnInUseDevice() {
            device.setState(DeviceState.IN_USE);

            DeviceRequest request = new DeviceRequest();
            request.setName("iPhone 15");   // same name
            request.setBrand("Apple");       // same brand
            request.setState(DeviceState.AVAILABLE);

            when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
            when(deviceMapper.toResponse(device)).thenReturn(deviceResponse);

            deviceService.update(deviceId, request);

            verify(deviceMapper).updateEntityFromRequest(request, device);
        }
    }

    @Nested
    @DisplayName("patch (PATCH)")
    class Patch {

        @Test
        @DisplayName("should patch state only without conflict")
        void shouldPatchState() {
            device.setState(DeviceState.IN_USE);

            DevicePatchRequest request = new DevicePatchRequest();
            request.setState(DeviceState.AVAILABLE);

            when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
            when(deviceMapper.toResponse(device)).thenReturn(deviceResponse);

            deviceService.patch(deviceId, request);

            verify(deviceMapper).patchEntityFromRequest(request, device);
        }

        @Test
        @DisplayName("should throw conflict when patching name of in-use device")
        void shouldThrowWhenPatchingNameOfInUseDevice() {
            device.setState(DeviceState.IN_USE);

            DevicePatchRequest request = new DevicePatchRequest();
            request.setName("New Name");

            when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

            assertThatThrownBy(() -> deviceService.patch(deviceId, request))
                    .isInstanceOf(DeviceConflictException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should delete device when not in use")
        void shouldDeleteWhenNotInUse() {
            when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

            deviceService.delete(deviceId);

            verify(deviceRepository).delete(device);
        }

        @Test
        @DisplayName("should throw conflict when deleting in-use device")
        void shouldThrowWhenInUse() {
            device.setState(DeviceState.IN_USE);
            when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

            assertThatThrownBy(() -> deviceService.delete(deviceId))
                    .isInstanceOf(DeviceConflictException.class)
                    .hasMessageContaining("in use");
        }

        @Test
        @DisplayName("should throw not found when device does not exist")
        void shouldThrowNotFound() {
            when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> deviceService.delete(deviceId))
                    .isInstanceOf(DeviceNotFoundException.class);
        }
    }
}
