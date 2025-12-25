package com.electronics_store.mapper;

import com.electronics_store.dto.category.CategoryDtoCreate;
import com.electronics_store.dto.category.CategoryDtoUpdate;
import com.electronics_store.model.Category;
import com.electronics_store.repository.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CategoryMapper {

    public Category dtoCreateToEntity(CategoryDtoCreate categoryDtoCreate) {
        Category category = new Category();
        category.setName(categoryDtoCreate.getName());
        category.setSlug(categoryDtoCreate.getSlug());
        Category parent = categoryDtoCreate.getParent() != null ? new Category(categoryDtoCreate.getParent()) : null;
        category.setParent(parent);
        return category;
    }

    public Category dtoUpdateToEntity(CategoryDtoUpdate categoryDtoUpdate) {
        Category category = new Category();
        category.setId(categoryDtoUpdate.getId());
        category.setName(categoryDtoUpdate.getName());
        category.setSlug(categoryDtoUpdate.getSlug());
        Category parent = categoryDtoUpdate.getParent() != null ? new Category(categoryDtoUpdate.getParent()) : null;
        category.setParent(parent);
        return category;
    }

    public CategoryDtoUpdate toDtoUpdate(Category category) {
        CategoryDtoUpdate categoryDtoUpdate = new CategoryDtoUpdate();
        categoryDtoUpdate.setId(category.getId());
        categoryDtoUpdate.setName(category.getName());
        categoryDtoUpdate.setSlug(category.getSlug());
        categoryDtoUpdate.setParent(category.getParent() != null ? category.getParent().getId() : null);
        return categoryDtoUpdate;
    }
}
