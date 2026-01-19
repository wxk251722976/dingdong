# DingDong Project Developer Guide (Enhanced)

> **Audience**: Experienced backend / frontend engineers
>
> **Goal**: Provide a *single source of truth* that promotes **clean architecture**, **high performance**, **strong consistency**, and **modern language features**.

---

## 1. Architecture Overview

### 1.1 Design Principles

* **DDD-lite + Layered Architecture**
* **Readability > Cleverness** (optimize only after measurement)
* **Fail Fast & Explicit Errors**
* **Immutable DTOs, Controlled Mutation in Domain/Service Layer**
* **Stateless Services** (except cache)

```
Controller  ->  Application Service  ->  Domain Service  ->  Repository (Mapper)
                |                       |
                |                       +-- Cache / MQ / External Services
                +-- Assembler (DTO <-> DO)
```

---

## 2. Technology Stack

### 2.1 Frontend (User App)

* **Framework**: UniApp (Vue 3)
* **Runtime**: WeChat Mini Program (`mp-weixin`)
* **Build Tool**: Vite
* **Language**: TypeScript (mandatory)
* **State**: Pinia (single store per domain)
* **Location**: `/app`

**Best Practices**:

* Prefer **Composition API + `<script setup>`**
* UI components must be **stateless** when possible
* All API calls go through **typed API modules**

---

### 2.2 Backend (Server)

* **Framework**: Spring Boot 3.3+
* **JDK**: Java 21 (records, virtual threads ready)
* **ORM**: MyBatis-Plus 3.5+
* **Database**: MySQL 8.0
* **Cache**: Redis 7.x
* **File Storage**: SeaweedFS
* **Security**: Double JWT (Access + Refresh)
* **Location**: `/server`

**Mandatory Dependencies**:

* `spring-boot-starter-validation`
* `spring-boot-starter-cache`
* `spring-boot-starter-aop`

---

## 3. Database Design Guidelines

### 3.1 General Rules

* All tables must include:

  * `id BIGINT PRIMARY KEY`
  * `create_time`, `update_time`
  * `create_by`, `update_by`
  * `deleted TINYINT`
* **No nullable foreign keys unless justified**
* **No TEXT unless length > 512**

### 3.2 Core Tables

#### `sys_user`

| Field    | Notes           |
| -------- | --------------- |
| openid   | Unique, indexed |
| nickname | Snapshot only   |
| avatar   | CDN URL         |

---

#### `check_in_task`

| Field       | Notes                |
| ----------- | -------------------- |
| user_id     | Task owner           |
| creator_id  | Creator (may differ) |
| remind_time | HH:mm:ss             |
| repeat_type | Enum-driven          |
| status      | Soft enable/disable  |

**Index Recommendation**:

```
(user_id, status)
(user_id, remind_time)
```

---

#### `check_in_log`

| Field      | Notes            |
| ---------- | ---------------- |
| task_id    | FK               |
| check_time | Actual execution |
| status     | Enum             |

**Uniqueness**:

* `(task_id, DATE(check_time))`

---

## 4. Backend Coding Standards

### 4.1 Controller Layer

Responsibilities:

* Authentication & authorization
* Parameter validation
* Request/response mapping

```java
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskAppService taskAppService;

    @PostMapping
    public Result<Long> create(@Valid @RequestBody TaskCreateCmd cmd) {
        return Result.ok(taskAppService.create(cmd));
    }
}
```

**Rules**:

* No business logic
* No database calls
* Always return `Result<T>`

---

### 4.2 Application Service Layer

* Orchestrates use cases
* Controls transactions
* Converts DTO <-> Domain

```java
@Transactional
public Long create(TaskCreateCmd cmd) {
    var userId = SystemContextHolder.getUserId();
    var task = TaskFactory.create(cmd, userId);
    repository.save(task);
    return task.getId();
}
```

---

### 4.3 Domain Model

* Rich domain objects
* Encapsulate invariants

```java
public class CheckInTask {

    public void enable() {
        if (this.status == Status.ENABLED) return;
        this.status = Status.ENABLED;
    }
}
```

---

### 4.4 DTO & VO Design

* **Java record preferred**
* Immutable
* Validation annotations on DTO only

```java
public record TaskCreateCmd(
        @NotBlank String title,
        @NotNull LocalTime remindTime,
        @NotNull RepeatType repeatType
) {}
```

---

## 5. MyBatis-Plus Best Practices

### 5.1 Mapper Rules

* Prefer **LambdaQueryWrapper**
* Avoid XML unless:

  * Complex joins
  * Window functions

```java
lambdaQuery()
    .eq(CheckInTask::getUserId, userId)
    .eq(CheckInTask::getStatus, ENABLED)
    .list();
```

### 5.2 Performance Rules

* Never call `selectOne()` without unique constraint
* Batch insert/update for logs
* Use `exists()` instead of `count(*)`

---

## 6. Cache Strategy (Redis)

### 6.1 What to Cache

* User profile (short TTL)
* Task list by user
* Daily check-in status

### 6.2 Cache Pattern

```
Cache Aside Pattern
```

* TTL required
* Cache key must include `userId`

---

## 7. File Upload (Avatar)

### Backend

* Use `FileStorageService`
* Validate MIME & size
* Async post-processing if needed

### Frontend

* Centralized `upload.ts`
* Show progress & retry

---

## 8. Frontend Engineering Rules

* One page = one domain
* API response must be typed
* No magic strings (Enums only)

```ts
export interface TaskVO {
  id: number
  title: string
  status: TaskStatus
}
```

---

## 9. Error Handling

* Business error → custom `BizException`
* System error → global handler

```java
throw new BizException(ErrorCode.TASK_NOT_FOUND);
```

---

## 10. Checklist Before Merge

* [ ] No controller logic leakage
* [ ] DTO ≠ Entity
* [ ] Index reviewed
* [ ] Cache key reviewed
* [ ] Enum instead of int

---

> **Rule of Thumb**: If a junior dev cannot read it in 5 minutes, it is too complex.
