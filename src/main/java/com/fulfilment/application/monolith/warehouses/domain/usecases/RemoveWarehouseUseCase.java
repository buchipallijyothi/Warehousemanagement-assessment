package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.RemoveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Use case for removing/deleting warehouses from the system.
 *
 * Handles business logic validation before deletion.
 */
@ApplicationScoped
public class RemoveWarehouseUseCase implements RemoveWarehouseOperation {

  private final WarehouseStore warehouseStore;

  public RemoveWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void remove(Warehouse warehouse) {

    if(warehouse ==null){
      throw new IllegalArgumentException("Warehouse cannot be null");
    }
    if(warehouse.businessUnitCode==null || warehouse.businessUnitCode.isBlank()){
      throw new IllegalArgumentException("Warehouse business unit code cannot be null");
    }
    Warehouse existing = warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);
    if (existing == null) {
      throw new IllegalArgumentException(
          "Warehouse with business unit code '" + warehouse.businessUnitCode + "' does not exist");
    }


    if (existing.archivedAt != null) {
      throw new IllegalArgumentException(
          "Warehouse with business unit code '" + warehouse.businessUnitCode +
          "' is archived and cannot be deleted. Archive must be reversed first.");
    }


    if (existing.stock > 0) {
      throw new IllegalArgumentException(
          "Warehouse with business unit code '" + warehouse.businessUnitCode +
          "' has stock (" + existing.stock + ") and cannot be deleted. Stock must be cleared first.");
    }
    warehouseStore.remove(existing);
  }
}
