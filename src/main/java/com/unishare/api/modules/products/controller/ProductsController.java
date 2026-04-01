package com.unishare.api.modules.products.controller;

import com.unishare.api.common.dto.ApiResponse;
import com.unishare.api.infrastructure.security.CustomUserPrincipal;
import com.unishare.api.modules.products.dto.DocumentRequest;
import com.unishare.api.modules.products.dto.DocumentResponse;
import com.unishare.api.modules.products.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class ProductsController {

    private final DocumentService documentService;

    // --- Public endpoints ---
    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getAllDocuments() {
        return ResponseEntity.ok(ApiResponse.<List<DocumentResponse>>build()
                .withData(documentService.getAllDocuments()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentResponse>> getDocumentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<DocumentResponse>build()
                .withData(documentService.getDocumentById(id)));
    }

    // --- Seller endpoints ---
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getMyDocuments(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.<List<DocumentResponse>>build()
                .withData(documentService.getSellerDocuments(principal.getUserId())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DocumentResponse>> createDocument(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody DocumentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<DocumentResponse>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(documentService.createDocument(principal.getUserId(), request))
                .withMessage("Document created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentResponse>> updateDocument(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody DocumentRequest request) {
        return ResponseEntity.ok(ApiResponse.<DocumentResponse>build()
                .withData(documentService.updateDocument(principal.getUserId(), id, request))
                .withMessage("Document updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long id) {
        documentService.deleteDocument(principal.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.<Void>build().withMessage("Document deleted successfully"));
    }

    // --- Wishlist functionality (can be in same controller or path) ---
    @GetMapping("/wishlist")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getMyWishlist(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.<List<DocumentResponse>>build()
                .withData(documentService.getUserWishlist(principal.getUserId())));
    }

    @PostMapping("/{id}/wishlist")
    public ResponseEntity<ApiResponse<Void>> addToWishlist(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long id) {
        documentService.addToWishlist(principal.getUserId(), id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<Void>build().withHttpStatus(HttpStatus.CREATED).withMessage("Added to wishlist"));
    }

    @DeleteMapping("/{id}/wishlist")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long id) {
        documentService.removeFromWishlist(principal.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.<Void>build().withMessage("Removed from wishlist"));
    }
}
