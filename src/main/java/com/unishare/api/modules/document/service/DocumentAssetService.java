package com.unishare.api.modules.document.service;

import com.unishare.api.common.dto.AppException;
import com.unishare.api.infrastructure.storage.FileStorageService;
import com.unishare.api.modules.document.dto.DocumentAssetResponse;
import com.unishare.api.modules.document.entity.Document;
import com.unishare.api.modules.document.entity.DocumentAsset;
import com.unishare.api.modules.document.exception.DocumentErrorCode;
import com.unishare.api.modules.document.mapper.DocumentMapper;
import com.unishare.api.modules.document.repository.DocumentAssetRepository;
import com.unishare.api.modules.document.repository.DocumentRepository;
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
    private final DocumentAssetRepository assetRepository;
    private final FileStorageService fileStorageService;
    private final DocumentMapper documentMapper;

    @Transactional
    public DocumentAssetResponse uploadAsset(Long sellerId, Long documentId, MultipartFile file, String fileType, String fileFormat) {
        Document document = documentRepository.findById(documentId)
                .filter(d -> d.getSellerId().equals(sellerId))
                .orElseThrow(() -> new AppException(DocumentErrorCode.DOCUMENT_NOT_FOUND));

        String folder = "documents/" + documentId;
        String fileUrl = fileStorageService.uploadFile(file, folder);

        DocumentAsset asset = new DocumentAsset();
        asset.setDocument(document);
        asset.setFileType(fileType);
        asset.setFileFormat(fileFormat != null ? fileFormat : extractExtension(file.getOriginalFilename()));
        asset.setFileUrl(fileUrl);
        asset.setFileSize(file.getSize());
        
        asset = assetRepository.save(asset);
        
        return documentMapper.toResponse(asset);
    }

    @Transactional
    public void deleteAsset(Long sellerId, Long documentId, Long assetId) {
        DocumentAsset asset = assetRepository.findById(assetId)
                .filter(a -> a.getDocument().getId().equals(documentId))
                .filter(a -> a.getDocument().getSellerId().equals(sellerId))
                .orElseThrow(() -> new AppException(DocumentErrorCode.ASSET_NOT_FOUND));

        fileStorageService.deleteFile(asset.getFileUrl());
        assetRepository.delete(asset);
    }
    
    @Transactional(readOnly = true)
    public List<DocumentAssetResponse> getDocumentAssets(Long documentId) {
        return assetRepository.findByDocumentId(documentId).stream()
                .map(documentMapper::toResponse)
                .collect(Collectors.toList());
    }

    private String extractExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) return "unknown";
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
