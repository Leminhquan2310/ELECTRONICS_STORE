package com.electronics_store.service.impl;

import com.electronics_store.dto.category.CategoryDto;
import com.electronics_store.dto.category.CategoryDtoCreate;
import com.electronics_store.dto.category.CategoryDtoUpdate;
import com.electronics_store.helper.CategoryUtil;
import com.electronics_store.model.Category;
import com.electronics_store.repository.CategoryRepository;
import com.electronics_store.repository.ProductRepository;
import com.electronics_store.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;

    @Transactional
    @Override
    public Category create(CategoryDtoCreate dto) {
        Category category = toEntity(dto);

        if (dto.getParent() != null) {
            // Tìm cha và ném lỗi rõ ràng nếu không thấy
            Category parent = categoryRepository.findById(dto.getParent())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));

            // Cập nhật trạng thái cho cha: không còn là nút lá nữa
            if (parent.isLeaf()) {
                parent.setLeaf(false);
                categoryRepository.save(parent);
            }

            category.setParent(parent);
            category.setLevel(parent.getLevel() + 1);
            category.setSortOrder(categoryRepository.findMaxSortOrderByParenId(parent.getId()) + 1);

        } else {
            // Trường hợp là danh mục gốc (Root)
            category.setParent(null);
            category.setLevel(1);
            category.setSortOrder(categoryRepository.findMaxSortOrderRoot() + 1);
        }
        category.setLeaf(true);
        return categoryRepository.save(category);
    }

    public Category toEntity(CategoryDtoCreate dto) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setSlug(dto.getSlug());
        return category;
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
    public boolean update(CategoryDtoUpdate dto) {
        // 1. Lấy dữ liệu cũ
        Category oldCategory = categoryRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Category oldParent = oldCategory.getParent();
        Long oldParentId = (oldParent != null) ? oldParent.getId() : null;
        Long newParentId = dto.getParent();

        // check nếu thay đổi parent category
        if (!Objects.equals(oldParentId, newParentId)) {
            // check chỉ sửa parent nếu là nút lá
            if (!oldCategory.isLeaf()) return false;

            Category newCategory = dtoUpdateToEntity(dto, null);

            // XỬ LÝ CHA MỚI (New Parent)
            if (newParentId != null) {
                Category newParent = categoryRepository.findById(newParentId)
                        .orElseThrow(() -> new RuntimeException("Parent mới không tồn tại"));

                newParent.setLeaf(false); // Cha mới chắc chắn không còn là lá
                categoryRepository.save(newParent);

                newCategory.setParent(newParent);
                newCategory.setLevel(newParent.getLevel() + 1);
                newCategory.setSortOrder(categoryRepository.findMaxSortOrderByParenId(newParentId) + 1);
            } else {
                // Chuyển từ có cha thành Root (Parent = null)
                newCategory.setParent(null);
                newCategory.setLevel(1);
                newCategory.setSortOrder(categoryRepository.findMaxSortOrderRoot() + 1);
            }

            newCategory.setLeaf(true);
            categoryRepository.save(newCategory);

            // XỬ LÝ CHA CŨ (Old Parent)
            // Chỉ xử lý nếu cha cũ tồn tại (không phải Root cũ)
            if (oldParent != null) {
                // Sau khi con rời đi, kiểm tra xem cha cũ còn con không
                boolean hasOtherChildren = categoryRepository.existsByParentId(oldParentId);
                if (!hasOtherChildren) {
                    oldParent.setLeaf(true);
                    categoryRepository.save(oldParent);
                }
            }
            return true;

        }

        // Trường hợp không đổi cha, chỉ update thông tin thường
        Category newCategory = dtoUpdateToEntity(dto, oldParent);
        newCategory.setSortOrder(oldCategory.getSortOrder());
        newCategory.setLevel(oldCategory.getLevel());
        newCategory.setLeaf(oldCategory.isLeaf());
        categoryRepository.save(newCategory);

        return true;
    }


    public Category dtoUpdateToEntity(CategoryDtoUpdate categoryDtoUpdate, Category categoryParent) {
        Category category = new Category();
        category.setId(categoryDtoUpdate.getId());
        category.setName(categoryDtoUpdate.getName());
        category.setSlug(categoryDtoUpdate.getSlug());
        category.setParent(categoryParent);
        return category;
    }

    @Transactional
    @Override
    public boolean delete(Long id) {
        // 1. Tìm category cần xóa
        Optional<Category> categoryOptional = categoryRepository.findById(id);
        if (categoryOptional.isEmpty()) return false;

        Category currentCategory = categoryOptional.get();
        Category parent = currentCategory.getParent();

        log.info("exist reference: {}", isExistReference(id));
        log.info("Is leaf: {}", currentCategory.isLeaf(

        ));

        // 2. Kiểm tra nghiệp vụ (có PK với product không, có con không)
        if (isExistReference(id) || !currentCategory.isLeaf()) {
            return false;
        }

        // 3. Thực hiện xóa
        categoryRepository.delete(currentCategory);

        // 4. Cập nhật nút cha nếu cần
        if (parent != null) {
            // Kiểm tra xem cha còn đứa con nào khác không
            boolean hasOtherChildren = categoryRepository.existsByParentId(parent.getId());

            if (!hasOtherChildren) {
                parent.setLeaf(true);
                categoryRepository.save(parent);
            }
        }

        return true;
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
    public List<CategoryDto> getListByIsLeafTrue() {
        List<Category> list = categoryRepository.findByIsActiveTrueAndIsLeafTrue();
        return list.stream().filter(category -> category.getParent() != null).map(CategoryUtil::getCategoryFullName).toList();
    }


    @Override
    public List<CategoryDto> getListRootForClient() {
        List<Category> list = categoryRepository.findByIsActiveTrueAndLevel(1);
        return list.stream().map(this::mapToDto).toList();
    }

    private CategoryDto mapToDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    @Override
    public List<Category> getListByLevelAndIsActiveTrue(Integer level) {
        return categoryRepository.findByIsActiveTrueAndLevel(level);
    }

    @Override
    public CategoryDtoUpdate getByIdForUpdate(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));
        return toDtoUpdate(category);
    }


    public CategoryDtoUpdate toDtoUpdate(Category category) {
        CategoryDtoUpdate categoryDtoUpdate = new CategoryDtoUpdate();
        categoryDtoUpdate.setId(category.getId());
        categoryDtoUpdate.setName(category.getName());
        categoryDtoUpdate.setSlug(category.getSlug());
        categoryDtoUpdate.setParent(category.getParent() != null ? category.getParent().getId() : null);
        categoryDtoUpdate.setLeaf(category.isLeaf());
        return categoryDtoUpdate;
    }


    private boolean isExistReference(Long id) {
        Category category = categoryRepository.findById(id).get();
        return productRepository.existsByCategory(category);
    }

}
