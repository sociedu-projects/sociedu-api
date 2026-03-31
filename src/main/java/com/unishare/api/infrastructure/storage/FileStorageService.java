package com.unishare.api.infrastructure.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /**
     * Upload multipart file lên storage provider
     * @param file File cần upload
     * @param folder Tên thư mục lưu trữ (ví dụ: documents, avatars)
     * @return URL của file đã upload thành công
     */
    String uploadFile(MultipartFile file, String folder);

    /**
     * Xóa file khỏi storage provider
     * @param fileUrl URL của file cần xóa
     * @return true nếu xóa thành công
     */
    boolean deleteFile(String fileUrl);
}
