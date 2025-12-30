package com.electronics_store.helper;

import com.electronics_store.model.ProductOptionValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VariantGenerator {

    public static List<List<ProductOptionValue>> generate(
            List<List<ProductOptionValue>> groups) {

        List<List<ProductOptionValue>> result = new ArrayList<>();
        backtrack(groups, 0, new ArrayList<>(), result);
        return result;
    }

    private static void backtrack(
            List<List<ProductOptionValue>> groups,
            int index,
            List<ProductOptionValue> current,
            List<List<ProductOptionValue>> result
    ) {
        if (index == groups.size()) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (ProductOptionValue value : groups.get(index)) {
            current.add(value);
            backtrack(groups, index + 1, current, result);
            current.remove(current.size() - 1);
        }
    }
}