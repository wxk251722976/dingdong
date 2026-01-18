---
name: DingDong Developer Guide
description: Comprehensive guide for the DingDong project, covering tech stack, schema, and development workflow.
---

# DingDong Project Developer Guide

Use this skill to understand the *DingDong* project context. This guide contains the source of truth for the project's architecture, database schema, and coding standards.

## 1. Technology Stack

### Frontend (User App)
- **Framework**: UniApp (based on Vue 3).
- **Environment**: WeChat Mini Program (`mp-weixin`).
- **Build Tool**: Vite.
- **Location**: `/app` directory.

### Backend (Server)
- **Framework**: Spring Boot 3.3+.
- **ORM**: MyBatis-Plus.
- **Database**: MySQL 8.0.
- **Cache**: Redis 7.x.
- **File Storage**: SeaweedFS.
- **Security**: Double JWT (Access Token + Refresh Token).
- **Location**: `/server` directory.

## 2. Database Schema (Reference)

The database is MySQL. Key tables are listed below. All tables include standard audit fields (`create_time`, `create_by`, `deleted`, etc.).

### `sys_user` (Users)
- `id` (PK)
- `openid` (WeChat OpenID, Unique)
- `nickname`, `avatar`

### `check_in_task` (Tasks)
- `id` (PK)
- `user_id` (Target user)
- `creator_id` (Creator)
- `title` (Task name)
- `remind_time`
- `repeat_type` (0-Once, 1-Daily, 2-Workdays, 3-Weekends)
- `status` (1-Enabled, 0-Disabled)

### `check_in_log` (Logs)
- `id` (PK)
- `user_id`
- `task_id`
- `check_time`
- `status` (1-Normal, 2-Makeup, 3-Missed)

### `user_relation` (Relationships)
- `id` (PK)
- `supervisor_id` (The one supervising)
- `supervised_id` (The one being supervised)
- `relation_type` (0-Custom, 1-Couple, 2-Friend, 3-Family)
- `status` (0-Pending, 1-Accepted, 2-Rejected)

## 3. Development Workflow

### Starting the Project
*   **Backend**:
    ```bash
    cd server
    mvn spring-boot:run
    ```
*   **Frontend**:
    ```bash
    cd app
    npm run dev:mp-weixin
    ```
    (Then open `dist/dev/mp-weixin` in WeChat DevTools).

### Code Conventions
*   **Backend**:
    *   **Controller**: Use `@RestController`, return `Result<T>`.
    *   **Service**: Use standard Service/ServiceImpl pattern.
    *   **Auth**: Use `SystemContextHolder.getUserId()` to get the current logged-in user.
    *   **Entities/DTOs**: Maintain strict separation. DTOs for input/output, Entities for DB.
*   **Frontend**:
    *   **Vue 3**: Use `<script setup>`.
    *   **API**: Define requests in `utils/request.js` or separate api modules.

## 4. Common Tasks Guideline

### Adding a New Task Field
If you need to add a field (e.g., `content`) to a task:
1.  **Modify DB**: Update `check_in_task` table in `sql/`.
2.  **Update Entity**: `CheckInTask` class in `server`.
3.  **Update DTO/VO**: `TaskDetailVO` and `TaskSaveDTO`.
4.  **Update Mapper**: `CheckInTaskMapper.xml` if custom SQL exists.
5.  **Update Frontend**: `pages/task/add.vue` and `pages/task/detail.vue`.

### Adding File Upload (Avatar)
To handle file uploads (e.g., avatar):
1.  **Use `FileStorageService`**: Inject and call `uploadFile(MultipartFile)`.
2.  **Controller**: See `FileController` for upload endpoint `/file/upload/avatar`.
3.  **Frontend**: Use `utils/upload.js` -> `uploadAvatar(tempFilePath)`.
4.  **Config**: SeaweedFS URLs configured in `application.yml` via env vars.

