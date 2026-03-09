package com.electronics_store.helper;

import com.electronics_store.dto.category.CategoryDto;
import com.electronics_store.model.Category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CategoryUtil {

    public static CategoryDto getCategoryFullName(Category category) {
        if (category == null) return new CategoryDto();

        List<String> names = new ArrayList<>();
        Category current = category;

        // Vòng lặp ngược lên cho đến khi không còn cha
        while (current != null) {
            names.add(current.getName());
            current = current.getParent();
        }

        // Đảo ngược danh sách và nối lại bằng dấu gạch chéo
        Collections.reverse(names);
        String name = String.join(" / ", names);
        return new CategoryDto(category.getId(), name);
    }
}
