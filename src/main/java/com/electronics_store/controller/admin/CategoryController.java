package com.electronics_store.controller.admin;

//import com.electronics_store.dto.category.CategoryDtoCreate;

import com.electronics_store.dto.category.CategoryDtoCreate;
import com.electronics_store.dto.category.CategoryDtoUpdate;
import com.electronics_store.mapper.CategoryMapper;
import com.electronics_store.model.Category;
import com.electronics_store.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/admin/categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryMapper categoryMapper;

    @Value("${app.base-url}")
    private String baseUrl;

    @GetMapping("")
    public ModelAndView showListAllCategory() {
        ModelAndView mav = new ModelAndView("admin/category/list");
        mav.addObject("BASE_URL", baseUrl);
        mav.addObject("categories", categoryService.getAll());
        return mav;
    }

    @GetMapping("/create")
    public ModelAndView showCreateCategory() {
        ModelAndView mav = new ModelAndView("admin/category/create");
        mav.addObject("categories", categoryService.getAll());
        mav.addObject("categoryDtoCreate", new CategoryDtoCreate());
        return mav;
    }

    @GetMapping("/update/{id}")
    public ModelAndView showUpdateCategory(@PathVariable Long id) {
        ModelAndView mav = new ModelAndView("admin/category/update");
        CategoryDtoUpdate categoryDtoUpdate = categoryMapper.toDtoUpdate(categoryService.getById(id));
        mav.addObject("categories", categoryService.getAll());
        mav.addObject("categoryDtoUpdate", categoryDtoUpdate);
        return mav;
    }

    @PostMapping("")
    public ModelAndView createCategory(@Validated @ModelAttribute("categoryDtoCreate") CategoryDtoCreate categoryDtoCreate, BindingResult bindingResult, RedirectAttributes redirectAttributes) throws ChangeSetPersister.NotFoundException {
        ModelAndView mav = new ModelAndView("admin/category/create");
        mav.addObject("categories", categoryService.getAll());
        if (!bindingResult.hasErrors()) {
            categoryService.checkSlugIsDuplicateForCreate(categoryDtoCreate.getSlug(), bindingResult); // check slug duplicate
        }
        if (bindingResult.hasErrors()) {
            return mav;
        }
        categoryService.create(categoryDtoCreate);
        mav.addObject("categoryDtoCreate", new CategoryDtoCreate());
        mav.addObject("status", "success");
        mav.addObject("message", "Create category successfully!");
        return mav;
    }

    @PutMapping("")
    public ModelAndView updateCategory(@Validated @ModelAttribute("categoryDtoUpdate") CategoryDtoUpdate categoryDtoUpdate, BindingResult bindingResult, RedirectAttributes redirectAttributes) throws ChangeSetPersister.NotFoundException {
        ModelAndView mav = new ModelAndView("admin/category/update");
        mav.addObject("categories", categoryService.getAll());
        if (!bindingResult.hasErrors()) {
            categoryService.checkSlugIsDuplicateForUpdate(categoryDtoUpdate.getSlug(), categoryDtoUpdate.getId(), bindingResult); // check slug duplicate
        }
        if (bindingResult.hasErrors()) {
            return mav;
        }
        Category category = categoryService.update(categoryDtoUpdate);
        mav.addObject("categoryDtoUpdate", categoryMapper.toDtoUpdate(category));
        mav.addObject("status", "success");
        mav.addObject("message", "Update category successfully!");
        return mav;
    }

    @DeleteMapping("/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean isSuccess = categoryService.delete(id);
        redirectAttributes.addFlashAttribute("status", isSuccess ? "success" : "error");
        redirectAttributes.addFlashAttribute("message", isSuccess ? "Delete category successfully!" : "Delete category failed!");
        return "redirect:/admin/categories";
    }
}
