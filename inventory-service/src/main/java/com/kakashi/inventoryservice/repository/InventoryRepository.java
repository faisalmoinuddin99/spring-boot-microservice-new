package com.kakashi.inventoryservice.repository;

import com.kakashi.inventoryservice.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {


//    Optional<Inventory> findBySkuCode(String skuCode); first method we used earlier

    List<Inventory> findBySkuCodeIn(List<String> skuCode);
}
