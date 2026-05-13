package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {
  /**
   * Search warehouses with optional filters, sorting, and pagination. Excludes archived warehouses.
   */
  public List<Warehouse> searchWarehouses(String location, Integer minCapacity, Integer maxCapacity, String sortBy, String sortOrder, Integer page, Integer pageSize) {
    var query = "archivedAt is null";
    java.util.Map<String, Object> params = new java.util.HashMap<>();
    if (location != null && !location.isEmpty()) {
      query += " and location = :location";
      params.put("location", location);
    }
    if (minCapacity != null) {
      query += " and capacity >= :minCapacity";
      params.put("minCapacity", minCapacity);
    }
    if (maxCapacity != null) {
      query += " and capacity <= :maxCapacity";
      params.put("maxCapacity", maxCapacity);
    }

    String sortField = "createdAt";
    if ("capacity".equals(sortBy)) sortField = "capacity";
    String order = "asc";
    if ("desc".equalsIgnoreCase(sortOrder)) order = "desc";

    int p = (page != null && page >= 0) ? page : 0;
    int ps = (pageSize != null && pageSize > 0) ? Math.min(pageSize, 100) : 10;
    Sort sort = "desc".equalsIgnoreCase(sortOrder)
            ? Sort.descending(sortField)
            : Sort.ascending(sortField);


    var dbList = find(query, sort, params)
            .page(p, ps)
            .list();


    return dbList.stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  @Transactional
  public List<Warehouse> getAll() {
    return this.listAll().stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {
    DbWarehouse dbWarehouse = new DbWarehouse();
    dbWarehouse.businessUnitCode = warehouse.businessUnitCode;
    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    dbWarehouse.createdAt = warehouse.createdAt;
    dbWarehouse.archivedAt = warehouse.archivedAt;
    
    this.persist(dbWarehouse);
  }

  @Override
  @Transactional
  public void update(Warehouse warehouse) {
    DbWarehouse db = find("businessUnitCode", warehouse.businessUnitCode).firstResult();
    if (db != null) {
      db.location = warehouse.location;
      db.capacity = warehouse.capacity;
      db.stock = warehouse.stock;
      if (warehouse.archivedAt != null) {
        db.archivedAt = warehouse.archivedAt;
      }

    }
  }

  @Override
  @Transactional
  public void remove(Warehouse warehouse) {
    if (warehouse == null) {
      throw new IllegalArgumentException("Warehouse object cannot be null");
    }
    if (warehouse.businessUnitCode == null) {
      throw new IllegalArgumentException("Warehouse business unit code cannot be null");
    }

    DbWarehouse db = find("businessUnitCode", warehouse.businessUnitCode).firstResult();
    if (db == null) {
      throw new IllegalArgumentException(
          "Warehouse with business unit code '" + warehouse.businessUnitCode + "' does not exist");
    }
    this.delete(db);
  }

  @Override
  @Transactional
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse dbWarehouse = find("businessUnitCode", buCode).firstResult();
    return dbWarehouse != null ? dbWarehouse.toWarehouse() : null;
  }
}
