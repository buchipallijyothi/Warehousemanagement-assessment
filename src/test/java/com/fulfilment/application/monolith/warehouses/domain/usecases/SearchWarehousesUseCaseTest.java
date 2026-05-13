package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SearchWarehousesUseCaseTest {

  private WarehouseStore warehouseStore;
  private SearchWarehousesUseCase useCase;

  @BeforeEach
  public void setup() {
    warehouseStore = mock(WarehouseStore.class);
    useCase = new SearchWarehousesUseCase(warehouseStore);
  }

  @Test
  public void returnsResultsFromStore() {
    Warehouse w1 = new Warehouse();
    Warehouse w2 = new Warehouse();
    List<Warehouse> expected = Arrays.asList(w1, w2);

    when(warehouseStore.searchWarehouses("LOC-1", 10, 100, "capacity", "asc", 0, 10))
        .thenReturn(expected);

    List<Warehouse> results = useCase.search("LOC-1", 10, 100, "capacity", "asc", 0, 10);

    assertSame(expected, results);
    verify(warehouseStore).searchWarehouses("LOC-1", 10, 100, "capacity", "asc", 0, 10);
  }

  @Test
  public void capsPageSizeAt100() {
    when(warehouseStore.searchWarehouses(any(), any(), any(), any(), any(), any(), eq(100)))
        .thenReturn(Collections.emptyList());

    useCase.search(null, null, null, null, null, 0, 200);

    verify(warehouseStore).searchWarehouses(null, null, null, null, null, 0, 100);
  }

  @Test
  public void passesNullPageSizeThrough() {
    when(warehouseStore.searchWarehouses(any(), any(), any(), any(), any(), any(), isNull()))
        .thenReturn(Collections.emptyList());

    useCase.search("L-1", null, null, null, null, 0, null);

    verify(warehouseStore).searchWarehouses("L-1", null, null, null, null, 0, null);
  }
}
