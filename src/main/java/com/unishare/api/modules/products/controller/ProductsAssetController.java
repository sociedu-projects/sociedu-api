package com.unishare.api.modules.products.controller;

import com.unishare.api.common.dto.ApiResponse;
import com.unishare.api.infrastructure.security.CustomUserPrincipal;
import com.unishare.api.modules.products.dto.DocumentAssetResponse;
import com.unishare.api.modules.products.service.DocumentAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documents/{documentId}/files")
@RequiredArgsConstructor
public class ProductsAssetController {

    private final DocumentAssetService assetService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DocumentAssetResponse>> uploadFile(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long documentId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileType") String fileType,
            @RequestParam(value = "fileFormat", required = false) String fileFormat) {
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<DocumentAssetResponse>build()
                        .withHttpStatus(HttpStatus.CREATED)
                        .withData(assetService.uploadAsset(principal.getUserId(), documentId, file, fileType, fileFormat))
                        .withMessage("File uploaded successfully"));
    }

    @DeleteMapping("/{assetId}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long documentId,
            @PathVariable Long assetId) {
        assetService.deleteAsset(principal.getUserId(), documentId, assetId);
        return ResponseEntity.ok(ApiResponse.<Void>build().withMessage("File deleted successfully"));
    }
    
    // API lấy list files của document này, ai cũng xem được (chỉ lấy metadata)
    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentAssetResponse>>> getDocumentFiles(
            @PathVariable Long documentId) {
        return ResponseEntity.ok(ApiResponse.<List<DocumentAssetResponse>>build()
                .withData(assetService.getDocumentAssets(documentId)));
    }
}
