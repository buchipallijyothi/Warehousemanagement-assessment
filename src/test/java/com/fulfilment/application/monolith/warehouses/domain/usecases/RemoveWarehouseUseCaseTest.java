package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RemoveWarehouseUseCaseTest {

  private WarehouseStore warehouseStore;
  private RemoveWarehouseUseCase useCase;

  @BeforeEach
  public void setup() {
    warehouseStore = mock(WarehouseStore.class);
    useCase = new RemoveWarehouseUseCase(warehouseStore);
  }

  @Test
  public void removesWarehouseWhenValid() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "BU-1";
    existing.capacity = 100;
    existing.stock = 0;
    existing.createdAt = LocalDateTime.now();
    existing.archivedAt = null;

    when(warehouseStore.findByBusinessUnitCode("BU-1")).thenReturn(existing);

    Warehouse toRemove = new Warehouse();
    toRemove.businessUnitCode = "BU-1";

    useCase.remove(toRemove);

    verify(warehouseStore).remove(existing);
  }

  @Test
  public void throwsWhenWarehouseDoesNotExist() {
    when(warehouseStore.findByBusinessUnitCode("MISSING")).thenReturn(null);

    Warehouse w = new Warehouse();
    w.businessUnitCode = "MISSING";

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> useCase.remove(w));
    assertTrue(ex.getMessage().contains("does not exist"));

    verify(warehouseStore, never()).remove(any());
  }

  @Test
  public void throwsWhenWarehouseIsArchived() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "ARCH-1";
    existing.stock = 0;
    existing.archivedAt = LocalDateTime.now();

    when(warehouseStore.findByBusinessUnitCode("ARCH-1")).thenReturn(existing);

    Warehouse w = new Warehouse();
    w.businessUnitCode = "ARCH-1";

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> useCase.remove(w));
    assertTrue(ex.getMessage().contains("is archived"));

    verify(warehouseStore, never()).remove(any());
  }

  @Test
  public void throwsWhenWarehouseHasStock() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "STOCK-1";
    existing.stock = 10;
    existing.archivedAt = null;

    when(warehouseStore.findByBusinessUnitCode("STOCK-1")).thenReturn(existing);

    Warehouse w = new Warehouse();
    w.businessUnitCode = "STOCK-1";

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> useCase.remove(w));
    assertTrue(ex.getMessage().contains("has stock"));

    verify(warehouseStore, never()).remove(any());
  }
}
