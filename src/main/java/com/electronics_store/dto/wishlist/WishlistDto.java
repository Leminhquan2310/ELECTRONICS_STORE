package com.electronics_store.dto.wishlist;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WishlistDto {
    private Long productId;
    private boolean inWishlist;
}
