# AgentFlow API & Event Contract Baseline

This document defines the strict frontend-backend contract that must be preserved during any architectural refactoring.

## 1. REST API Endpoints

The system must support the following routes under both `/api/sessions` and `/api/v1/sessions`:

| Method | Endpoint | Description | Expected Response |
|--------|----------|-------------|-------------------|
| POST | `/` | Create a new session | `{"code": 200, "data": {"session_id": "uuid"}, "message": "..."}` |
| GET | `/` | List sessions | `{"code": 200, "data": {"sessions": [...]}, "message": "..."}` |
| GET | `/{sessionId}` | Get session details | `{"code": 200, "data": {"session_id": "...", "events": [...], "files": [...]}}` |
| GET | `/{sessionId}/files`| Get session files | `{"code": 200, "data": {"files": [...]}}` |
| POST | `/{sessionId}/chat` | Send message & start stream | `text/event-stream` response |
| POST | `/{sessionId}/stop` | Stop task | `{"code": 200, "message": "..."}` |
| POST | `/{sessionId}/delete` | Delete session | `{"code": 200, "message": "..."}` |
| POST | `/{sessionId}/clear-unread-message-count`| Clear unread | `{"code": 200, "message": "..."}` |
| POST | `/stream` | Stream all sessions | `text/event-stream` response |

## 2. Session Object Shape
When returning session items in lists or details, these fields must be present:
- `session_id` (String)
- `title` (String)
- `latest_message` (String)
- `latest_message_at` (String/Date)
- `status` (String, values: `pending`, `running`, `waiting`, `completed`)
- `unread_message_count` (Integer)

## 3. SSE Event Formats
All events streamed in `/chat` must contain these base fields:
- `id` (String, used for SSE `id: `)
- `event_id` (String, same as `id`)
- `type` (String, used for SSE `event: `)
- `timestamp` (Long)

### Known Types:
- **`message`**: Contains `role` (user/assistant), `message` (String), `attachments` (List<String>)
- **`wait`**: Empty body (besides base fields)
- **`done`**: Empty body (besides base fields)
- **`error`**: Contains `error` (String)
- **`plan`**: Contains `plan` (Object with steps)
- **`step`**: Contains `step` (Object with details)
- **`tool`**: Contains `function_name`, `function_args`, `status`
- **`title`**: Contains `title` (String)

