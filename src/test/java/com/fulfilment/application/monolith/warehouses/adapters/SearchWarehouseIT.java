package com.fulfilment.application.monolith.warehouses.adapters;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;
import com.fulfilment.application.monolith.location.LocationGateway;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Tests for searchWarehouses functionality
 * Tests complete workflow with real database via Testcontainers
 */
@QuarkusTest
public class SearchWarehouseIT {

  @Inject
  WarehouseRepository warehouseRepository;

  @Inject
  LocationGateway locationGateway;

  @Inject
  EntityManager em;

  private CreateWarehouseUseCase createWarehouseUseCase;

  @BeforeEach
  @Transactional
  public void setup() {
    // Clean database before each test
    em.createQuery("DELETE FROM DbWarehouse").executeUpdate();
    createWarehouseUseCase = new CreateWarehouseUseCase(warehouseRepository, locationGateway);
  }

  /**
   * Test: Search returns all non-archived warehouses when no filters applied
   */
  @Test
  @Transactional
  public void testSearchWithoutFiltersReturnsAllActiveWarehouses() {
    // Create multiple warehouses
    createWarehouse("SEARCH-001", "AMSTERDAM-001", 50);
    createWarehouse("SEARCH-002", "AMSTERDAM-001", 100);
    createWarehouse("SEARCH-003", "AMSTERDAM-001", 75);

    // Search without filters
    List<Warehouse> results = warehouseRepository.searchWarehouses(
        null, null, null, null, null, 0, 10);

    // Should return all 3 warehouses
    assertEquals(3, results.size());
  }

  /**
   * Test: Search filters by location correctly
   */
  @Test
  @Transactional
  public void testSearchFiltersByLocation() {
    createWarehouse("LOC-001", "AMSTERDAM-001", 50);
    createWarehouse("LOC-002", "AMSTERDAM-001", 100);
    createWarehouse("LOC-003", "ZWOLLE-001", 30);

    // Search by Amsterdam location
    List<Warehouse> results = warehouseRepository.searchWarehouses(
        "AMSTERDAM-001", null, null, null, null, 0, 10);

    // Should return only Amsterdam warehouses
    assertEquals(2, results.size());
    assertTrue(results.stream().allMatch(w -> "AMSTERDAM-001".equals(w.location)));
  }

  /**
   * Test: Search filters by minimum capacity
   */
  @Test
  @Transactional
  public void testSearchFiltersByMinCapacity() {
    createWarehouse("CAP-001", "AMSTERDAM-001", 30);
    createWarehouse("CAP-002", "AMSTERDAM-001", 60);
    createWarehouse("CAP-003", "AMSTERDAM-001", 90);

    // Search for warehouses with capacity >= 60
    List<Warehouse> results = warehouseRepository.searchWarehouses(
        null, 60, null, null, null, 0, 10);

    // Should return 2 warehouses
    assertEquals(2, results.size());
    assertTrue(results.stream().allMatch(w -> w.capacity >= 60));
  }

  /**
   * Test: Search filters by maximum capacity
   */
  @Test
  @Transactional
  public void testSearchFiltersByMaxCapacity() {
    createWarehouse("MAXCAP-001", "AMSTERDAM-001", 30);
    createWarehouse("MAXCAP-002", "AMSTERDAM-001", 60);
    createWarehouse("MAXCAP-003", "AMSTERDAM-001", 90);

    // Search for warehouses with capacity <= 70
    List<Warehouse> results = warehouseRepository.searchWarehouses(
        null, null, 70, null, null, 0, 10);

    // Should return 2 warehouses
    assertEquals(2, results.size());
    assertTrue(results.stream().allMatch(w -> w.capacity <= 70));
  }

  /**
   * Test: Search filters by capacity range (min and max)
   */
  @Test
  @Transactional
  public void testSearchFiltersByCapacityRange() {
    createWarehouse("RANGE-001", "AMSTERDAM-001", 20);
    createWarehouse("RANGE-002", "AMSTERDAM-001", 50);
    createWarehouse("RANGE-003", "AMSTERDAM-001", 75);
    createWarehouse("RANGE-004", "AMSTERDAM-001", 100);

    // Search for capacity between 40 and 80
    List<Warehouse> results = warehouseRepository.searchWarehouses(
        null, 40, 80, null, null, 0, 10);

    // Should return 2 warehouses
    assertEquals(2, results.size());
    assertTrue(results.stream().allMatch(w -> w.capacity >= 40 && w.capacity <= 80));
  }

  /**
   * Test: Search combines location and capacity filters
   */
  @Test
  @Transactional
  public void testSearchWithMultipleFiltersLocationAndCapacity() {
    createWarehouse("MULTI-001", "AMSTERDAM-001", 30);
    createWarehouse("MULTI-002", "AMSTERDAM-001", 80);
    createWarehouse("MULTI-003", "ZWOLLE-001", 30);

    // Search: Amsterdam location AND capacity >= 70
    List<Warehouse> results = warehouseRepository.searchWarehouses(
        "AMSTERDAM-001", 70, null, null, null, 0, 10);

    // Should return only MULTI-002
    assertEquals(1, results.size());
    assertEquals("AMSTERDAM-001", results.get(0).location);
    assertEquals(80, results.get(0).capacity);
  }

  /**
   * Test: Search excludes archived warehouses
   */
  @Test
  @Transactional
  public void testSearchExcludesArchivedWarehouses() {
    Warehouse w1 = createWarehouse("ARCH-001", "AMSTERDAM-001", 50);
    createWarehouse("ARCH-002", "AMSTERDAM-001", 60);

    // Archive first warehouse
    w1.archivedAt = java.time.LocalDateTime.now();
    warehouseRepository.update(w1);

    // Search should not include archived warehouse
    List<Warehouse> results = warehouseRepository.searchWarehouses(
        null, null, null, null, null, 0, 10);

    // Should only return active warehouse
    assertEquals(1, results.size());
    assertEquals("ARCH-002", results.get(0).businessUnitCode);
  }

  /**
   * Test: Search sorts by createdAt ascending (default)
   */
  @Test
  @Transactional
  public void testSearchSortsByCreatedAtAscending() {
    // Create warehouses with slight delays to ensure different timestamps
    Warehouse w1 = new Warehouse();
    w1.businessUnitCode = "SORT-001";
    w1.location = "AMSTERDAM-001";
    w1.capacity = 50;
    w1.stock = 10;
    w1.createdAt = java.time.LocalDateTime.now().minusHours(2);
    createWarehouseUseCase.create(w1);

    Warehouse w2 = new Warehouse();
    w2.businessUnitCode = "SORT-002";
    w2.location = "AMSTERDAM-001";
    w2.capacity = 60;
    w2.stock = 10;
    w2.createdAt = java.time.LocalDateTime.now();
    createWarehouseUseCase.create(w2);

    // Search with default sort (ascending by createdAt)
    List<Warehouse> results = warehouseRepository.searchWarehouses(
        null, null, null, null, "asc", 0, 10);

    // SORT-001 should come before SORT-002
    assertEquals("SORT-001", results.get(0).businessUnitCode);
    assertEquals("SORT-002", results.get(1).businessUnitCode);
  }

  /**
   * Test: Search sorts by capacity descending
   */
  @Test
  @Transactional
  public void testSearchSortsByCapacityDescending() {
    createWarehouse("SORTCAP-001", "AMSTERDAM-001", 30);
    createWarehouse("SORTCAP-002", "AMSTERDAM-001", 90);
    createWarehouse("SORTCAP-003", "AMSTERDAM-001", 60);

    // Sort by capacity descending
    List<Warehouse> results = warehouseRepository.searchWarehouses(
        null, null, null, "capacity", "desc", 0, 10);

    // Should be ordered: 90, 60, 30
    assertEquals(90, results.get(0).capacity);
    assertEquals(60, results.get(1).capacity);
    assertEquals(30, results.get(2).capacity);
  }

  /**
   * Test: Search pagination - first page
   */
  @Test
  @Transactional
  public void testSearchPaginationFirstPage() {
    // Create 15 warehouses
    for (int i = 1; i <= 15; i++) {
      createWarehouse("PAGE-" + String.format("%03d", i), "AMSTERDAM-001", 50 + i);
    }

    // Get first page with pageSize=5
    List<Warehouse> page1 = warehouseRepository.searchWarehouses(
        null, null, null, null, null, 0, 5);

    // Should return 5 items
    assertEquals(5, page1.size());
  }

  /**
   * Test: Search pagination - second page
   */
  @Test
  @Transactional
  public void testSearchPaginationSecondPage() {
    for (int i = 1; i <= 15; i++) {
      createWarehouse("PAG2-" + String.format("%03d", i), "AMSTERDAM-001", 50 + i);
    }

    // Get second page with pageSize=5
    List<Warehouse> page2 = warehouseRepository.searchWarehouses(
        null, null, null, null, null, 1, 5);

    // Should return 5 items (different from page 1)
    assertEquals(5, page2.size());
  }

  /**
   * Test: Search enforces max pageSize limit (capped at 100)
   */
  @Test
  @Transactional
  public void testSearchEnforcesMaxPageSize() {
    // Create 150 warehouses
    for (int i = 1; i <= 150; i++) {
      createWarehouse("MAXPAGE-" + String.format("%03d", i), "AMSTERDAM-001", 50);
    }

    // Request pageSize=200 (should be capped at 100)
    List<Warehouse> results = warehouseRepository.searchWarehouses(
        null, null, null, null, null, 0, 200);

    // Should return max 100 items
    assertEquals(100, results.size());
  }

  /**
   * Test: Search with negative page defaults to 0
   */
  @Test
  @Transactional
  public void testSearchNegativePageDefaultsToZero() {
    for (int i = 1; i <= 5; i++) {
      createWarehouse("NEG-" + i, "AMSTERDAM-001", 50);
    }

    // Request with negative page
    List<Warehouse> results = warehouseRepository.searchWarehouses(
        null, null, null, null, null, -1, 10);

    // Should return results (as if page=0)
    assertEquals(5, results.size());
  }

  /**
   * Test: Search with zero or negative pageSize defaults to 10
   */
  @Test
  @Transactional
  public void testSearchZeroPageSizeDefaultsToTen() {
    for (int i = 1; i <= 15; i++) {
      createWarehouse("ZEROSIZE-" + i, "AMSTERDAM-001", 50);
    }

    // Request with pageSize <= 0
    List<Warehouse> results = warehouseRepository.searchWarehouses(
        null, null, null, null, null, 0, 0);

    // Should return default 10 items
    assertEquals(10, results.size());
  }

  /**
   * Test: Search with empty location string treated as filter
   */
  @Test
  @Transactional
  public void testSearchEmptyLocationStringIgnored() {
    createWarehouse("EMPT-001", "AMSTERDAM-001", 50);
    createWarehouse("EMPT-002", "ZWOLLE-001", 30);

    // Search with empty location (should be ignored)
    List<Warehouse> results = warehouseRepository.searchWarehouses(
        "", null, null, null, null, 0, 10);

    // Should return 2 (empty string is ignored)
    assertEquals(2, results.size());
  }

  /**
   * Test: Search returns empty list when no matches
   */
  @Test
  @Transactional
  public void testSearchNoMatches() {
    createWarehouse("NOMATCH-001", "AMSTERDAM-001", 50);

    // Search for non-existent location
    List<Warehouse> results = warehouseRepository.searchWarehouses(
        "NON-EXISTENT-LOCATION", null, null, null, null, 0, 10);

    // Should return empty list
    assertTrue(results.isEmpty());
  }

  /**
   * Test: Complex search - multiple filters + sort + pagination
   */
  @Test
  @Transactional
  public void testComplexSearchMultipleFiltersAndPagination() {
    // Create 20 warehouses in Amsterdam with varying capacities
    for (int i = 1; i <= 20; i++) {
      createWarehouse("COMPLEX-" + String.format("%02d", i), "AMSTERDAM-001", 40 + (i * 3));
    }

    // Search: Amsterdam, capacity 70-120, sorted by capacity desc, page 0, size 5
    List<Warehouse> results = warehouseRepository.searchWarehouses(
        "AMSTERDAM-001", 70, 120, "capacity", "desc", 0, 5);

    // Should return 5 items, sorted by capacity descending
    assertEquals(5, results.size());

    // Verify capacity order descending
    for (int i = 0; i < results.size() - 1; i++) {
      assertTrue(results.get(i).capacity >= results.get(i + 1).capacity);
    }
  }

  // Helper method
  private Warehouse createWarehouse(String code, String location, int capacity) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = code;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = 10;
    createWarehouseUseCase.create(warehouse);
    return warehouse;
  }
}

