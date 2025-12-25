package com.electronics_store.service;

import com.electronics_store.model.Category;
import org.springframework.validation.BindingResult;

import java.util.List;

public interface CategoryService extends IGenerateService<Category> {
    void checkSlugIsDuplicateForCreate(String slug, BindingResult bindingResult);

    void checkSlugIsDuplicateForUpdate(String slug, Long id, BindingResult bindingResult);

    List<Category> getListByParentId(Long parentId);

    List<Category> getListByIsLeafTrue();
}
