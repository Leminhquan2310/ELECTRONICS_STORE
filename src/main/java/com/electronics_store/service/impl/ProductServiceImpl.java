package com.electronics_store.service.impl;

import com.electronics_store.dto.image.ImageUploadResult;
import com.electronics_store.dto.product.*;
import com.electronics_store.mapper.ProductMapper;
import com.electronics_store.model.*;
import com.electronics_store.repository.*;
import com.electronics_store.service.ImageStorageService;
import com.electronics_store.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService<Product> {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductOptionRepository productOptionRepository;
    @Autowired
    private ProductOptionValueRepository productOptionValueRepository;
    @Autowired
    private ImageStorageService imageStorageService;
    @Autowired
    private ProductImageRepository productImageRepository;

    @Transactional
    @Override
    public boolean create(ProductDtoCreate productDtoCreate) {
        try {
            Category category = categoryRepository.findById(productDtoCreate.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found"));
            Product product = productMapper.dtoCreateToEntity(productDtoCreate, category);
            productRepository.save(product);

            if (productDtoCreate.getOptions() != null) {
                for (ProductOptionDtoCreate optionDto : productDtoCreate.getOptions()) {

                    ProductOption option = productMapper.toOptionEntity(product, optionDto);
                    productOptionRepository.save(option);

                    for (String value : optionDto.getValues()) {
                        ProductOptionValue optionValue =
                                productMapper.toOptionValueEntity(option, value);
                        productOptionValueRepository.save(optionValue);
                    }
                }
            }

            // 2. Lưu từng ảnh
            if (productDtoCreate.getImages() != null && !productDtoCreate.getImages().isEmpty()) {
                for (MultipartFile file : productDtoCreate.getImages()) {

                    if (file.isEmpty()) continue;

                    ImageUploadResult upload = imageStorageService.upload(file);

                    ProductImage image = new ProductImage();
                    image.setProduct(product);
                    image.setImageUrl(upload.getUrl());
                    image.setPublicId(upload.getPublicId());

                    productImageRepository.save(image);
                }
            }
            return true;
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("Create product failed", e);
            return false;
        }

    }

    @Override
    public Product getById(Long id) {
        return productRepository.findById(id).get();
    }

    @Override
    public List<Product> getAll() {
        return productRepository.findProductsByIsActiveTrue();
    }

    @Transactional
    @Override
    public boolean update(ProductDtoUpdate productDtoUpdate) {
        try {
            Category category = categoryRepository.findById(productDtoUpdate.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found"));
            Product product = productMapper.dtoUpdateToEntity(productDtoUpdate, category);
            productRepository.save(product);

            updateOptions(product, productDtoUpdate.getOptions());

            updateImages(product, productDtoUpdate);
            return true;
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("Create product failed", e);
            return false;
        }
    }

    @Override
    public boolean delete(Long id) {
        if (checkCanDelete(id)) {
            Product product = productRepository.findById(id).get();
            product.setIsActive(false);
            productRepository.save(product);
            return true;
        }
        return false;
    }

    private boolean checkCanDelete(Long id) {
        // check product in order, invoice,...
        return true;
    }

    private void updateOptions(Product product, List<ProductOptionDtoUpdate> optionDtos) {
        // 1. Lấy option hiện có
        List<ProductOption> existingOptions = productOptionRepository.findByProductId(product.getId());

        Map<Long, ProductOption> optionMap = existingOptions.stream()
                .collect(Collectors.toMap(ProductOption::getId, o -> o));

        // 2. Xử lý update + insert
        for (ProductOptionDtoUpdate dto : optionDtos) {

            ProductOption option = dto.getId() != null
                    ? optionMap.remove(dto.getId())
                    : new ProductOption();

            option.setProduct(product);
            option.setName(dto.getName());

            productOptionRepository.save(option);

            updateOptionValues(option, dto.getValues());
        }

        // 3. DELETE option bị remove
        optionMap.values().forEach(productOptionRepository::delete);
    }

    private void updateOptionValues(ProductOption option, List<ProductOptionValueDtoUpdate> valueDtos) {
        List<ProductOptionValue> existingValues = productOptionValueRepository.findByProductOptionId(option.getId());

        Map<Long, ProductOptionValue> valueMap = existingValues.stream()
                .collect(Collectors.toMap(ProductOptionValue::getId, v -> v));

        for (ProductOptionValueDtoUpdate dto : valueDtos) {

            ProductOptionValue value = dto.getId() != null
                    ? valueMap.remove(dto.getId())
                    : new ProductOptionValue();

            value.setProductOption(option);
            value.setValue(dto.getValue());

            productOptionValueRepository.save(value);
        }

        // DELETE value bị remove
        valueMap.values().forEach(productOptionValueRepository::delete);
    }

    private void updateImages(Product product, ProductDtoUpdate dto) {
        // DELETE ảnh
        if (dto.getImageIdDelete() != null) {
            for (Long id : dto.getImageIdDelete()) {
                ProductImage image = productImageRepository.findById(id)
                        .orElseThrow();

                imageStorageService.delete(image.getPublicId());
                productImageRepository.delete(image);
            }
        }

        // ADD ảnh mới
        if (dto.getImages() != null) {
            for (MultipartFile file : dto.getImages()) {
                if (file.isEmpty()) continue;

                ImageUploadResult upload = imageStorageService.upload(file);

                ProductImage image = new ProductImage();
                image.setProduct(product);
                image.setImageUrl(upload.getUrl());
                image.setPublicId(upload.getPublicId());

                productImageRepository.save(image);
            }
        }
    }

    @Override
    public List<ProductImage> getImagesByProductId(Long id) {
        return productImageRepository.findByProductId(id);
    }

    @Override
    public List<ProductOptionValue> getProductOptionValuesByProductId(Long id) {
        List<ProductOptionValue> values = productOptionValueRepository.findProductOptionValueByProductId(id);
        return values;
    }
}
