package com.unishare.api.modules.products.service;

import com.unishare.api.common.dto.AppException;
import com.unishare.api.infrastructure.storage.FileStorageService;
import com.unishare.api.modules.products.dto.DocumentAssetResponse;
import com.unishare.api.modules.products.entity.Products;
import com.unishare.api.modules.products.entity.ProductsAsset;
import com.unishare.api.modules.products.exception.ProductsErrorCode;
import com.unishare.api.modules.products.mapper.ProductsMapper;
import com.unishare.api.modules.products.repository.ProductsAssetRepository;
import com.unishare.api.modules.products.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentAssetService {

    private final DocumentRepository documentRepository;
    private final ProductsAssetRepository assetRepository;
    private final FileStorageService fileStorageService;
    private final ProductsMapper productsMapper;

    @Transactional
    public DocumentAssetResponse uploadAsset(Long sellerId, Long documentId, MultipartFile file, String fileType, String fileFormat) {
        Products products = documentRepository.findById(documentId)
                .filter(d -> d.getSellerId().equals(sellerId))
                .orElseThrow(() -> new AppException(ProductsErrorCode.DOCUMENT_NOT_FOUND));

        String folder = "documents/" + documentId;
        String fileUrl = fileStorageService.uploadFile(file, folder);

        ProductsAsset asset = new ProductsAsset();
        asset.setProducts(products);
        asset.setFileType(fileType);
        asset.setFileFormat(fileFormat != null ? fileFormat : extractExtension(file.getOriginalFilename()));
        asset.setFileUrl(fileUrl);
        asset.setFileSize(file.getSize());
        
        asset = assetRepository.save(asset);
        
        return productsMapper.toResponse(asset);
    }

    @Transactional
    public void deleteAsset(Long sellerId, Long documentId, Long assetId) {
        ProductsAsset asset = assetRepository.findById(assetId)
                .filter(a -> a.getProducts().getId().equals(documentId))
                .filter(a -> a.getProducts().getSellerId().equals(sellerId))
                .orElseThrow(() -> new AppException(ProductsErrorCode.ASSET_NOT_FOUND));

        fileStorageService.deleteFile(asset.getFileUrl());
        assetRepository.delete(asset);
    }
    
    @Transactional(readOnly = true)
    public List<DocumentAssetResponse> getDocumentAssets(Long documentId) {
        return assetRepository.findByDocumentId(documentId).stream()
                .map(productsMapper::toResponse)
                .collect(Collectors.toList());
    }

    private String extractExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) return "unknown";
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
