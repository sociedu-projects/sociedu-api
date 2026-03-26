package com.unishare.api.modules.document.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItemId implements Serializable {
    private Long userId;
    private Long documentId;
}
