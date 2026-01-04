package com.electronics_store.repository;

import com.electronics_store.model.Cart;
import com.electronics_store.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserAndActiveTrue(User user);

    Optional<Cart> findByUserIdAndActiveTrue(Long userId);

    Cart findByUser(User user);
}
