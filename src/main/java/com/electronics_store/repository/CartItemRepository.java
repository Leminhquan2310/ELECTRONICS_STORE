package com.electronics_store.repository;

import com.electronics_store.model.Cart;
import com.electronics_store.model.CartItem;
import com.electronics_store.model.Product;
import com.electronics_store.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProductAndVariant(Cart cart, Product product, ProductVariant variant);

    Optional<CartItem> findByCartAndVariant(Cart cart, ProductVariant variant);

    void deleteAllByCart(Cart cart);

    boolean existsByVariant(ProductVariant variant);

    boolean existsByProductId(Long id);
}
