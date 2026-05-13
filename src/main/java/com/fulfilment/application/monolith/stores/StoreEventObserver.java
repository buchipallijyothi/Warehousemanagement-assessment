package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.eclipse.microprofile.context.ManagedExecutor;

@ApplicationScoped
public class StoreEventObserver {

  private static final Logger LOGGER = Logger.getLogger(StoreEventObserver.class.getName());

  @Inject 
  LegacyStoreManagerGateway legacyStoreManagerGateway;

  @Inject
  ManagedExecutor executor;

  public void onStoreCreated(@Observes(during = TransactionPhase.AFTER_SUCCESS) StoreCreatedEvent event) {
    executor.execute(() -> {
      LOGGER.info("Store created event received, syncing with legacy system: " + event.getStore().id);
      legacyStoreManagerGateway.createStoreOnLegacySystem(event.getStore());
    });
  }

  public void onStoreUpdated(@Observes(during = TransactionPhase.AFTER_SUCCESS) StoreUpdatedEvent event) {
    executor.execute(() -> {
      LOGGER.info("Store updated event received, syncing with legacy system: " + event.getStore().id);
      legacyStoreManagerGateway.updateStoreOnLegacySystem(event.getStore());
    });
  }
}
