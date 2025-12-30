package com.electronics_store.controller.user;

import com.electronics_store.dto.wishlist.WishlistDto;
import com.electronics_store.dto.wishlist.WishlistItemDto;
import com.electronics_store.security.CustomUserDetails;
import com.electronics_store.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/user/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    // ==================== WEB PAGES ====================

    /**
     * Hiển thị trang wishlist của người dùng
     */
    @GetMapping
    public String wishlistPage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model) {

        Long userId = userDetails.getUser().getId();

        // Tạo pageable với sorting
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Lấy dữ liệu wishlist
        Page<WishlistItemDto> wishlistPage = wishlistService.getUserWishlist(userId, pageable);
        long wishlistCount = wishlistService.getWishlistCount(userId);

        // Thêm vào model
        model.addAttribute("wishlistItems", wishlistPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", wishlistPage.getTotalPages());
        model.addAttribute("totalItems", wishlistPage.getTotalElements());
        model.addAttribute("wishlistCount", wishlistCount);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("pageSize", size);

        return "client-user/wishlist";
    }

    /**
     * Thêm sản phẩm vào wishlist và redirect về trang trước đó
     */
    @PostMapping("/add")
    public String addToWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long productId,
            @RequestHeader(value = "Referer", required = false) String referer,
            RedirectAttributes redirectAttributes) {

        Long userId = userDetails.getUser().getId();

        boolean success = wishlistService.addToWishlist(userId, productId);

        if (success) {
            redirectAttributes.addFlashAttribute("success", "Đã thêm sản phẩm vào danh sách yêu thích!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Sản phẩm đã có trong danh sách yêu thích!");
        }

        return "redirect:" + (referer != null ? referer : "/user/wishlist");
    }

    /**
     * Xóa sản phẩm khỏi wishlist và redirect về trang trước đó
     */
    @PostMapping("/remove")
    public String removeFromWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long productId,
            @RequestHeader(value = "Referer", required = false) String referer,
            RedirectAttributes redirectAttributes) {

        Long userId = userDetails.getUser().getId();

        boolean success = wishlistService.removeFromWishlist(userId, productId);

        if (success) {
            redirectAttributes.addFlashAttribute("success", "Đã xóa sản phẩm khỏi danh sách yêu thích!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Sản phẩm không có trong danh sách yêu thích!");
        }

        return "redirect:" + (referer != null ? referer : "/user/wishlist");
    }

    /**
     * Xóa tất cả sản phẩm khỏi wishlist
     */
    @PostMapping("/clear")
    public String clearWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        Long userId = userDetails.getUser().getId();

        wishlistService.clearWishlist(userId);
        redirectAttributes.addFlashAttribute("success", "Đã xóa tất cả sản phẩm khỏi danh sách yêu thích!");

        return "redirect:/user/wishlist";
    }

    /**
     * Chuyển sản phẩm từ wishlist sang cart
     */
    @PostMapping("/move-to-cart")
    public String moveToCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long productId,
            @RequestHeader(value = "Referer", required = false) String referer,
            RedirectAttributes redirectAttributes) {

        Long userId = userDetails.getUser().getId();

        boolean success = wishlistService.moveToCart(userId, productId);

        if (success) {
            redirectAttributes.addFlashAttribute("success", "Đã chuyển sản phẩm vào giỏ hàng!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không thể chuyển sản phẩm vào giỏ hàng!");
        }

        return "redirect:" + (referer != null ? referer : "/user/wishlist");
    }

    // ==================== API ENDPOINTS (AJAX) ====================

    /**
     * API thêm vào wishlist (AJAX)
     */
    @PostMapping("/api/add")
    @ResponseBody
    public ResponseEntity<?> addToWishlistApi(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long productId) {

        Long userId = userDetails.getUser().getId();

        boolean success = wishlistService.addToWishlist(userId, productId);
        long wishlistCount = wishlistService.getWishlistCount(userId);

        if (success) {
            return ResponseEntity.ok(new WishlistResponse(
                    true,
                    "Đã thêm sản phẩm vào danh sách yêu thích!",
                    wishlistCount
            ));
        } else {
            return ResponseEntity.ok(new WishlistResponse(
                    false,
                    "Sản phẩm đã có trong danh sách yêu thích!",
                    wishlistCount
            ));
        }
    }

    /**
     * API xóa khỏi wishlist (AJAX)
     */
    @DeleteMapping("/api/remove")
    @ResponseBody
    public ResponseEntity<?> removeFromWishlistApi(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long productId) {

        Long userId = userDetails.getUser().getId();

        boolean success = wishlistService.removeFromWishlist(userId, productId);
        long wishlistCount = wishlistService.getWishlistCount(userId);

        if (success) {
            return ResponseEntity.ok(new WishlistResponse(
                    true,
                    "Đã xóa sản phẩm khỏi danh sách yêu thích!",
                    wishlistCount
            ));
        } else {
            return ResponseEntity.ok(new WishlistResponse(
                    false,
                    "Sản phẩm không có trong danh sách yêu thích!",
                    wishlistCount
            ));
        }
    }

    /**
     * API kiểm tra sản phẩm có trong wishlist không (AJAX)
     */
    @GetMapping("/api/check/{productId}")
    @ResponseBody
    public ResponseEntity<WishlistDto> checkInWishlistApi(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long productId) {

        Long userId = userDetails.getUser().getId();

        WishlistDto dto = new WishlistDto();
        dto.setProductId(productId);
        dto.setInWishlist(wishlistService.isInWishlist(userId, productId));

        return ResponseEntity.ok(dto);
    }

    /**
     * API lấy số lượng sản phẩm trong wishlist (AJAX)
     */
    @GetMapping("/api/count")
    @ResponseBody
    public ResponseEntity<Long> getWishlistCountApi(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUser().getId();
        long count = wishlistService.getWishlistCount(userId);

        return ResponseEntity.ok(count);
    }

    /**
     * API lấy danh sách sản phẩm trong wishlist (AJAX)
     */
    @GetMapping("/api/items")
    @ResponseBody
    public ResponseEntity<List<WishlistItemDto>> getWishlistItemsApi(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUser().getId();
        List<WishlistItemDto> items = wishlistService.getUserWishlist(userId);

        return ResponseEntity.ok(items);
    }

    /**
     * API kiểm tra nhiều sản phẩm có trong wishlist không (AJAX)
     */
    @PostMapping("/api/check-multiple")
    @ResponseBody
    public ResponseEntity<List<WishlistDto>> checkMultipleProductsInWishlistApi(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody List<Long> productIds) {

        Long userId = userDetails.getUser().getId();
        List<WishlistDto> results = wishlistService.checkProductsInWishlist(userId, productIds);

        return ResponseEntity.ok(results);
    }

    // ==================== HELPER CLASSES ====================

    /**
     * Response class cho API
     */
    public static class WishlistResponse {
        private boolean success;
        private String message;
        private Long wishlistCount;

        public WishlistResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public WishlistResponse(boolean success, String message, Long wishlistCount) {
            this.success = success;
            this.message = message;
            this.wishlistCount = wishlistCount;
        }

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public Long getWishlistCount() { return wishlistCount; }
        public void setWishlistCount(Long wishlistCount) { this.wishlistCount = wishlistCount; }
    }
}