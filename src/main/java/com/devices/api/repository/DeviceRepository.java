package com.devices.api.repository;

import com.devices.api.domain.Device;
import com.devices.api.domain.DeviceState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {

    @Query("SELECT d FROM Device d WHERE " +
            "(:brand IS NULL OR LOWER(d.brand) = LOWER(CAST(:brand AS string))) AND " +
            "(:state IS NULL OR d.state = :state)")
    Page<Device> findByFilters(
            @Param("brand") String brand,
            @Param("state") DeviceState state,
            Pageable pageable);
}
