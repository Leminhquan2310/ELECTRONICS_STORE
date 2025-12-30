package com.electronics_store.service.impl;

import com.electronics_store.dto.category.CategoryDtoCreate;
import com.electronics_store.dto.category.CategoryDtoUpdate;
import com.electronics_store.mapper.CategoryMapper;
import com.electronics_store.model.Category;
import com.electronics_store.repository.CategoryRepository;
import com.electronics_store.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.List;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CategoryMapper categoryMapper;

    @Transactional
    @Override
    public Category create(Object categoryDtoCreate) throws ChangeSetPersister.NotFoundException {
        CategoryDtoCreate categoryDto = (CategoryDtoCreate) categoryDtoCreate;
        Category categoryParent = categoryDto.getParent() != null ? categoryRepository.findById(categoryDto.getParent()).get() : null;
        Category category = categoryMapper.dtoCreateToEntity(categoryDto, categoryParent);
        if (category.getParent() != null) {
            Category parentCategory = categoryRepository.findById(categoryDto.getParent()).get();
            parentCategory.setLeaf(false);
            category.setParent(parentCategory);
            category.setSortOrder(categoryRepository.findMaxSortOrderByParenId(parentCategory.getId()) + 1);
            category.setLevel(parentCategory.getLevel() + 1);
        } else {
            category.setSortOrder(categoryRepository.findMaxSortOrderRoot() + 1);
            category.setLevel(1);
        }
        return categoryRepository.save(category);
    }

    @Override
    public List<Category> createBatch(List<Category> categories) {
        return List.of();
    }

    @Override
    public Category getById(Long id) {
        return categoryRepository.findById(id).get();
    }

    @Override
    public List<Category> getAll() {
        return categoryRepository.findByIsActiveTrue();
    }

    @Override
    public Category update(Object categoryDtoUpdate) {
        CategoryDtoUpdate categoryDto = (CategoryDtoUpdate) categoryDtoUpdate;
        Category parent = categoryDto.getParent() != null ? categoryRepository.findById(categoryDto.getParent()).get() : null;
        Category category = categoryMapper.dtoUpdateToEntity(categoryDto, parent);
        if (isChangeParentId(category.getId(), categoryDto.getParent())) {
            if (category.getParent() != null) {
                Category parentCategory = categoryRepository.findById(categoryDto.getParent()).get();
                category.setParent(parentCategory);
                category.setSortOrder(categoryRepository.findMaxSortOrderByParenId(parentCategory.getId()) + 1);
                category.setLevel(parentCategory.getLevel() + 1);
            } else {
                category.setLevel(1);
                category.setSortOrder(categoryRepository.findMaxSortOrderRoot() + 1);
            }
        } else {
            log.info("Not Change");
            Category oldCategory = categoryRepository.findById(categoryDto.getId()).get();
            category.setParent(oldCategory.getParent());
            category.setSortOrder(oldCategory.getSortOrder());
            category.setLevel(oldCategory.getLevel());
        }
        return categoryRepository.save(category);
    }

    @Override
    public boolean delete(Long id) {
        if (checkCanDelete(id)) {
            Category category = categoryRepository.findById(id).get();
            category.setLeaf(false);
            categoryRepository.save(category);
            return true;
        }
        return false;
    }

    @Override
    public void checkSlugIsDuplicateForCreate(String slug, BindingResult bindingResult) {
        if (!slug.isEmpty() && categoryRepository.existsBySlugAndIsActiveTrue(slug)) {
            bindingResult.rejectValue("slug", "slug.exists", "Slug already existed");
        }
    }

    @Override
    public void checkSlugIsDuplicateForUpdate(String slug, Long id, BindingResult bindingResult) {
        if (!slug.isEmpty() && categoryRepository.existsBySlugAndIsActiveTrue(slug) && !categoryRepository.findById(id).get().getSlug().equals(slug)) {
            bindingResult.rejectValue("slug", "slug.exists", "Slug already existed");
        }
    }

    @Override
    public List<Category> getListByParentId(Long parentId) {
        return categoryRepository.findByParentIdOrderBySortOrder(parentId);
    }

    @Override
    public List<Category> getListByIsLeafTrue() {
        return categoryRepository.findByIsActiveTrueAndIsLeafTrue();
    }

    @Override
    public List<Category> getListByLevelAndIsActiveTrue(Integer level) {
        return categoryRepository.findByIsActiveTrueAndLevel(level);
    }

    private boolean isChangeParentId(Long id, Long newParentId) {
        Category category = categoryRepository.findById(id).get();
        if (category.getParent() == null && newParentId != null) return true;
        if (category.getParent() != null && newParentId == null) return true;
        if (category.getParent() == null) return false;
        return !category.getParent().getId().equals(newParentId);
    }

    private boolean checkCanDelete(Long id) {
        // check category in product
        return false;
    }
}
