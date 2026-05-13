package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.SearchWarehousesOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class SearchWarehousesUseCase implements SearchWarehousesOperation {

  private final WarehouseStore warehouseStore;

  public SearchWarehousesUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public List<Warehouse> search(
      String location,
      Integer minCapacity,
      Integer maxCapacity,
      String sortBy,
      String sortOrder,
      Integer page,
      Integer pageSize) {

    if (pageSize != null && pageSize > 100) {
      pageSize = 100;
    }


    return warehouseStore.searchWarehouses(
        location, minCapacity, maxCapacity, sortBy, sortOrder, page, pageSize);
  }
}

