package com.electronics_store.repository;

import com.electronics_store.model.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptionRepository extends JpaRepository<Option, Long> {
    // Lấy tất cả Option và OptionValue để vẽ ra Sidebar
    // Dùng LEFT JOIN FETCH để lấy luôn giá trị con trong 1 query
    @Query("SELECT DISTINCT o FROM Option o LEFT JOIN FETCH o.values")
    List<Option> findAllOptionsWithValues();

    boolean existsByName(String name);
}
