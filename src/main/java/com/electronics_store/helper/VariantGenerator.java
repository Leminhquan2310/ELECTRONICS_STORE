package com.electronics_store.helper;

import com.electronics_store.model.OptionValue;

import java.util.ArrayList;
import java.util.List;

public class VariantGenerator {
    public static <T> List<List<T>> generate(List<List<T>> lists) {
        List<List<T>> result = new ArrayList<>();
        if (lists.isEmpty()) return result;
        combineRecursive(lists, 0, new ArrayList<>(), result);
        return result;
    }

    private static <T> void combineRecursive(List<List<T>> lists, int depth, List<T> current, List<List<T>> result) {
        if (depth == lists.size()) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (T item : lists.get(depth)) {
            current.add(item);
            combineRecursive(lists, depth + 1, current, result);
            current.remove(current.size() - 1);
        }
    }
}