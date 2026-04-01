package com.unishare.api.modules.products.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "wishlist_items")
@IdClass(WishlistItemId.class)
@Getter
@Setter
@NoArgsConstructor
public class WishlistItem {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "document_id")
    private Long documentId;
}
