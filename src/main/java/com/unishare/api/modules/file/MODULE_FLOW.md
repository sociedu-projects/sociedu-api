# Module: File — Quản lý File tập trung

Hạ tầng lưu trữ dùng chung cho toàn bộ hệ thống — mọi upload/download đều đi qua module này.

## Entities
- `files` — metadata của file (uploader_id, file_name, file_url, mime_type, file_size, storage_provider, visibility, entity_type, entity_id, deleted_at)
- `visibility` — `public` | `private` | `internal`
- `entity_type` — tag xác định ngữ cảnh: `USER_AVATAR`, `USER_CERT`, `BOOKING_EVIDENCE`, `MESSAGE_ATTACHMENT`, `REPORT_EVIDENCE`

## Core Flows

**Upload file**: Module khác gọi FileService với file stream + metadata → validate mime_type và file_size → lưu vật lý lên `storage_provider` (S3/Cloudinary/local) → tạo bản ghi `files` → trả về `fileId` cho module gọi.

**Truy cập file**: GET request với `fileId` → kiểm tra `visibility` và quyền của người dùng → trả về URL trực tiếp hoặc presigned URL tùy Provider.

**Xóa mềm**: Khi entity liên kết bị xóa → cập nhật `files.deleted_at` → background job định kỳ xóa file vật lý khỏi Provider sau grace period.

## Quan hệ Module
- **Được gọi bởi**: `user` (avatar, certificate), `booking` (evidence), `chat` (attachment), `report` (evidence).
- **Hoàn toàn không gọi module khác** — module này là leaf node trong dependency graph.

## Business Rules
- Chỉ `uploader_id` hoặc Admin mới được xóa file.
- File `private` chỉ có thể truy cập bởi `uploader_id` hoặc qua business context (ví dụ: cả hai bên của một booking).
- Validate `mime_type` whitelist và `file_size` tối đa trước khi lưu. Từ chối file không hợp lệ.
- `entity_type` + `entity_id` dùng để nhóm file theo ngữ cảnh nghiệp vụ — không dùng FK cứng.
