# 叮咚 DingDong

家庭关爱提醒助手 - 微信小程序

## 项目简介

叮咚是一款面向家庭场景的社交提醒小程序，让子女可以远程监督长辈的日常任务（如吃药、喝水、运动等），长辈完成后点击"叮咚"告知子女，增进家人之间的关爱互动。

## 技术架构

### 前端
- **框架**: UniApp (Vue 3)
- **UI**: 原生组件 + 自定义样式
- **构建**: Vite
- **目标平台**: 微信小程序

### 后端
- **框架**: Spring Boot 2.7+
- **ORM**: MyBatis-Plus
- **数据库**: MySQL 8.0
- **容器化**: Docker + Docker Compose

### 项目结构
```
dingdong/
├── app/                    # 前端 UniApp 项目
│   └── src/
│       ├── pages/          # 页面组件
│       │   ├── elder/      # 长辈主页（叮咚任务）
│       │   ├── history/    # 历史记录
│       │   ├── supervisor/ # 监督中心
│       │   ├── relation/   # 关系管理
│       │   ├── task/       # 任务管理
│       │   ├── login/      # 登录页
│       │   └── mine/       # 我的
│       └── utils/          # 工具类
│           ├── request.js  # 请求封装
│           └── constants.js # 常量定义
├── server/                 # 后端 Spring Boot 项目
│   └── src/main/java/com/dingdong/
│       ├── controller/     # 控制器
│       ├── service/        # 业务层
│       ├── mapper/         # 数据访问层
│       ├── entity/         # 实体类
│       ├── dto/            # 数据传输对象
│       └── common/         # 公共模块
│           ├── constant/   # 枚举常量
│           └── base/       # 基类
├── sql/                    # 数据库脚本
└── docker/                 # Docker 配置
```

## 核心枚举

| 枚举类 | 说明 | 值 |
|--------|------|-----|
| TaskStatus | 任务完成状态 | 0-待完成, 1-已完成, 2-已错过 |
| RelationStatus | 关系确认状态 | 0-待确认, 1-已确认 |
| TaskEnabled | 任务启用状态 | 0-停用, 1-启用 |
| RepeatType | 重复类型 | 0-单次, 1-每天, 2-工作日, 3-周末 |

## API 接口

### 任务相关
| 接口 | 方法 | 说明 |
|------|------|------|
| `/task/daily` | GET | 获取每日任务列表 |
| `/task/add` | POST | 创建新任务 |

### 叮咚打卡
| 接口 | 方法 | 说明 |
|------|------|------|
| `/checkIn/do` | POST | 执行叮咚打卡 |
| `/checkIn/logs` | GET | 获取打卡记录 |

### 关系管理
| 接口 | 方法 | 说明 |
|------|------|------|
| `/relation/myRelations` | GET | 获取我的关系列表 |
| `/relation/invite` | POST | 发送绑定邀请 |
| `/relation/accept` | POST | 接受邀请 |

### 用户认证
| 接口 | 方法 | 说明 |
|------|------|------|
| `/auth/login` | POST | 微信登录 |

---

## 功能清单

### ✅ 已实现功能

#### 前端
- [x] 微信一键登录
- [x] 主页展示今日任务列表
- [x] 叮咚打卡按钮（完成任务）
- [x] 历史记录查看（按日期）
- [x] 监督中心（查看被监督者状态）
- [x] 关系管理（我监督的/监督我的）
- [x] 分享邀请链接绑定好友
- [x] 创建任务（单次/重复）
- [x] API请求封装
- [x] 状态常量定义

#### 后端
- [x] 用户登录认证
- [x] 任务CRUD
- [x] 每日任务状态计算
- [x] 叮咚打卡记录
- [x] 用户关系管理
- [x] 邀请/接受关系
- [x] 构造器注入（@RequiredArgsConstructor）
- [x] 枚举替代魔法值
- [x] DTO规范化（不含实体引用）

### 🚧 待开发功能

#### 短期计划
- [ ] 列表分页查询
- [ ] 微信订阅消息推送
- [ ] 任务编辑/删除
- [ ] 关系解除
- [ ] 用户资料编辑
- [ ] 头像上传

#### 中期计划
- [ ] 任务提醒（定时推送）
- [ ] 打卡统计图表
- [ ] 连续打卡天数记录
- [ ] 补打卡功能
- [ ] 多时间段任务

#### 长期计划
- [ ] 语音叮咚
- [ ] 家庭群组
- [ ] 健康数据接入
- [ ] AI智能提醒建议

---

## 快速开始

### 后端启动
```bash
cd server
mvn spring-boot:run
```

### 前端启动
```bash
cd app
npm install
npm run dev:mp-weixin
```

### Docker 启动
```bash
docker-compose up -d
```

## 数据库初始化
```bash
mysql -u root -p < sql/init.sql
```

---

## 设计规范

### 配色方案
- 主色（Mint Green）: `#68FFB4`
- 背景色: `#F8F8F8`
- 文字色: `#333333`
- 次要文字: `#999999`

### 代码规范
- 后端注释使用中文
- DTO不包含实体类引用
- 使用枚举替代魔法值
- Service层使用构造器注入

---

## License

MIT License
