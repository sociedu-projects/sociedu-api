# Module: Chat — Nhắn tin

Cung cấp khả năng nhắn tin thời gian thực giữa các người dùng trong hệ thống.

## Entities
- `conversations` — cuộc trò chuyện (type: general/booking/support, booking_id nếu loại booking)
- `conversation_participants` — danh sách thành viên trong cuộc trò chuyện
- `messages` — nội dung tin nhắn (sender_id, content, type, is_edited)
- `message_attachments` — đính kèm file trong tin nhắn (file_id)
- `message_type` (enum) — `text` | `image` | `file` | `system`

## Core Flows

**Tạo phòng chat Booking**: Module `booking` kích hoạt → tạo `conversations` (type=`booking`, booking_id) → thêm Mentor và Mentee vào `conversation_participants`.

**Gửi tin nhắn**: User gửi qua WebSocket/API → validate là `participant` → lưu `messages` → nếu có file đính kèm: gọi Module `file` → lưu `message_attachments` → đẩy realtime đến các thành viên còn lại.

**Tin nhắn hệ thống**: Khi sự kiện nghiệp vụ xảy ra (lên lịch, hoàn thành buổi học) → hệ thống tự sinh `messages` với type=`system` vào conversation tương ứng.

**Sửa/Xóa tin nhắn**: Người gửi có thể sửa tin nhắn của mình (`is_edited=true`) trong giới hạn thời gian quy định.

## API Surface (hiện tại)
- REST:
  - `POST /api/v1/chat/conversations`
  - `GET /api/v1/chat/conversations`
  - `GET /api/v1/chat/conversations/{conversationId}/messages`
  - `POST /api/v1/chat/conversations/{conversationId}/messages`
- WebSocket/STOMP:
  - Handshake endpoint: `/ws-chat` (SockJS enabled)
  - Client send: `/app/chat.send`
  - Client subscribe: `/topic/conversations/{conversationId}`

## Realtime Architecture
1. Client kết nối `/ws-chat` kèm JWT (`Authorization: Bearer <token>` hoặc query `token`).
2. `ChatHandshakeInterceptor` validate JWT, gán `Principal(userId)` cho websocket session.
3. `ChatInboundChannelInterceptor` chặn subscribe vào `/topic/conversations/{conversationId}` nếu user không phải participant.
4. Client gửi STOMP message tới `/app/chat.send`.
5. `ChatWsController` gọi `ChatService.sendMessage(...)` dùng chung logic với REST.
6. `ChatServiceImpl` lưu message + attachment, publish `ChatMessageSentEvent`.
7. `ChatRealtimeEventHandler` nhận event và broadcast payload tới `/topic/conversations/{conversationId}`.

## Quan hệ Module
- **Được kích hoạt** bởi Module `booking` khi tạo booking mới.
- **Gọi** Module `file` để lưu file đính kèm.
- **Đọc** Module `user` để hiển thị tên/avatar người gửi.
- **Cung cấp** lịch sử chat cho Module `dispute` làm bằng chứng.

## Business Rules
- Chỉ `conversation_participants` mới được đọc và gửi tin nhắn trong conversation đó.
- Chỉ hệ thống (internal) mới được tạo tin nhắn type=`system`.
- Không được xóa vật lý tin nhắn — dùng soft delete để phục vụ giải quyết tranh chấp.
