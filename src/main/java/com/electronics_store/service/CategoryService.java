package com.electronics_store.service;

import com.electronics_store.dto.category.CategoryDto;
import com.electronics_store.dto.category.CategoryDtoCreate;
import com.electronics_store.dto.category.CategoryDtoUpdate;
import com.electronics_store.model.Category;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.validation.BindingResult;

import java.util.List;

public interface CategoryService {
    Category create(CategoryDtoCreate categoryDtoCreate);

    List<Category> createBatch(List<Category> categories);

    Category getById(Long id);

    List<Category> getAll();

    void checkSlugIsDuplicateForCreate(String slug, BindingResult bindingResult);

    boolean update(CategoryDtoUpdate categoryDtoUpdate);

    void checkSlugIsDuplicateForUpdate(String slug, Long id, BindingResult bindingResult);

    boolean delete(Long id);

    List<CategoryDto> getListByIsLeafTrue();

    List<CategoryDto> getListRootForClient();

    List<Category> getListByLevelAndIsActiveTrue(Integer level);

    CategoryDtoUpdate getByIdForUpdate(Long id);
}
