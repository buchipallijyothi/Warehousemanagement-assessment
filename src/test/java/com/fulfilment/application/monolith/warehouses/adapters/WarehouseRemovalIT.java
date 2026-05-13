package com.fulfilment.application.monolith.warehouses.adapters;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.usecases.RemoveWarehouseUseCase;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for warehouse removal functionality.
 * Tests the complete flow from use case to database.
 */
@QuarkusTest
public class WarehouseRemovalIT {

  @Inject
  WarehouseRepository warehouseRepository;

  @Inject
  RemoveWarehouseUseCase removeWarehouseUseCase;

  @BeforeEach
  @Transactional
  public void setup() {
    // Clean database before each test
    warehouseRepository.getEntityManager().createQuery("DELETE FROM DbWarehouse").executeUpdate();
  }

  @Test
  @Transactional
  public void testCompleteWarehouseRemovalFlow() {

    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "INTEGRATION-TEST-001";
    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = 100;
    warehouse.stock = 0; // Empty warehouse
    warehouse.createdAt = LocalDateTime.now();

    warehouseRepository.create(warehouse);


    Warehouse created = warehouseRepository.findByBusinessUnitCode("INTEGRATION-TEST-001");
    assertNotNull(created, "Warehouse should exist before removal");
    assertEquals("INTEGRATION-TEST-001", created.businessUnitCode);


    removeWarehouseUseCase.remove(warehouse);

    Warehouse removed = warehouseRepository.findByBusinessUnitCode("INTEGRATION-TEST-001");
    assertNull(removed, "Warehouse should be completely removed from database");


    assertTrue(warehouseRepository.getAll().isEmpty(), "No warehouses should remain in database");
  }

  @Test
  @Transactional
  public void testRemovalWithBusinessRuleValidation() {

    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "STOCKED-WAREHOUSE";
    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = 100;
    warehouse.stock = 50; // Has stock
    warehouse.createdAt = LocalDateTime.now();

    warehouseRepository.create(warehouse);


    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      removeWarehouseUseCase.remove(warehouse);
    });


    assertTrue(exception.getMessage().contains("has stock"));
    assertTrue(exception.getMessage().contains("cannot be deleted"));

    Warehouse stillExists = warehouseRepository.findByBusinessUnitCode("STOCKED-WAREHOUSE");
    assertNotNull(stillExists, "Warehouse should still exist after failed removal attempt");
  }
}
