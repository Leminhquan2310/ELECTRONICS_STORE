package com.electronics_store.controller.admin;

import com.electronics_store.dto.coupon.CouponCreateDto;
import com.electronics_store.dto.coupon.CouponUpdateDto;
import com.electronics_store.model.Coupon;
import com.electronics_store.service.CouponService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/admin/coupons")
@PreAuthorize("hasRole('ADMIN')")
public class CouponController {

    @Autowired
    private CouponService couponService;

    // Trang danh sách coupon
    @GetMapping
    public ModelAndView index(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) String status) {
        ModelAndView mav = new ModelAndView("admin/coupon/index");

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Coupon> coupons = couponService.findWithFilters(keyword, status, pageable);

        mav.addObject("coupons", coupons);
        mav.addObject("currentPage", page);
        mav.addObject("keyword", keyword);
        mav.addObject("status", status);

        return mav;
    }

    // Trang tạo coupon mới
    @GetMapping("/create")
    public ModelAndView showCreateForm() {
        ModelAndView mav = new ModelAndView("admin/coupon/create");
        mav.addObject("couponDto", new CouponCreateDto());
        return mav;
    }

    // Xử lý tạo coupon
    @PostMapping("/create")
    public String createCoupon(@Valid @ModelAttribute("couponDto") CouponCreateDto couponDto,
                               BindingResult result,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (result.hasErrors()) {
            model.addAttribute("couponDto", couponDto);
            return "admin/coupon/create";
        }

        try {
            couponService.createCoupon(couponDto);
            redirectAttributes.addFlashAttribute("success", "Coupon created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating coupon: " + e.getMessage());
            return "redirect:/admin/coupons/create";
        }

        return "redirect:/admin/coupons";
    }

    // Trang chỉnh sửa coupon
    @GetMapping("/edit/{id}")
    public ModelAndView showEditForm(@PathVariable Long id) {
        ModelAndView mav = new ModelAndView("admin/coupon/edit");

        try {
            Coupon coupon = couponService.findById(id);
            CouponUpdateDto couponDto = couponService.convertToUpdateDto(coupon);
            mav.addObject("couponDto", couponDto);
            mav.addObject("couponId", id);
            mav.addObject("currentUsage", coupon.getUsedCount());
        } catch (Exception e) {
            mav.setViewName("redirect:/admin/coupons");
            mav.addObject("error", "Coupon not found");
        }

        return mav;
    }

    // Xử lý cập nhật coupon
    @PostMapping("/edit/{id}")
    public String updateCoupon(@PathVariable Long id,
                               @Valid @ModelAttribute("couponDto") CouponUpdateDto couponDto,
                               BindingResult result,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (result.hasErrors()) {
            try {
                Coupon coupon = couponService.findById(id);
                model.addAttribute("couponDto", couponDto);
                model.addAttribute("couponId", id);
                model.addAttribute("currentUsage", coupon.getUsedCount());
                return "admin/coupon/edit";
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Coupon not found");
                return "redirect:/admin/coupons";
            }
        }

        try {
            couponService.updateCoupon(id, couponDto);
            redirectAttributes.addFlashAttribute("success", "Coupon updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating coupon: " + e.getMessage());
            return "redirect:/admin/coupons/edit/" + id;
        }

        return "redirect:/admin/coupons";
    }


    // Xóa coupon
    @DeleteMapping("/delete/{id}")
    public String deleteCoupon(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            couponService.deleteCoupon(id);
            redirectAttributes.addFlashAttribute("success", "Coupon deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting coupon: " + e.getMessage());
        }

        return "redirect:/admin/coupons";
    }

    // Thay đổi trạng thái active/inactive
    @PostMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            couponService.toggleStatus(id);
            redirectAttributes.addFlashAttribute("success", "Coupon status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating coupon status: " + e.getMessage());
        }

        return "redirect:/admin/coupons";
    }
}