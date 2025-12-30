package com.electronics_store.service.impl;

import com.electronics_store.model.ProductOption;
import com.electronics_store.repository.ProductOptionRepository;
import com.electronics_store.service.ProductOptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductOptionServiceImpl implements ProductOptionService {
    @Autowired
    private ProductOptionRepository productOptionRepository;

    @Override
    public ProductOption create(Object t) throws ChangeSetPersister.NotFoundException {
        return null;
    }

    @Override
    public List<ProductOption> createBatch(List<ProductOption> productOptions) {
        return List.of();
    }

    @Override
    public ProductOption getById(Long id) {
        return null;
    }

    @Override
    public List<ProductOption> getAll() {
        return List.of();
    }

    @Override
    public ProductOption update(Object t) {
        return null;
    }

    @Override
    public boolean delete(Long id) {
        return false;
    }

    @Override
    public List<ProductOption> getProductOptionsByProductId(Long productId) {
        return productOptionRepository.findByProductId(productId);
    }
}
