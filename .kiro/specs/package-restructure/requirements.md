# 需求文档：包结构重构

## 简介

将修仙挂机游戏 Spring Boot 项目从扁平化包结构（controller/service/mapper/entity 按层分类）重构为模块化包结构（按业务模块分类），提升代码可维护性和模块内聚性。

## 术语表

- **扁平化结构**：按技术层（controller/service/mapper/entity）组织代码的方式
- **模块化结构**：按业务模块（player/combat/pet 等）组织代码的方式
- **迁移**：在新位置创建文件并更新 package 声明，暂时保留旧文件
- **common 模块**：存放跨模块共用基础设施代码（config/exception/security/annotation/aspect/util）
- **dto**：数据传输对象，跨模块共用，保持在根包下不迁移

## 需求

### 需求 1：模块化包结构

**用户故事：** 作为开发者，我希望代码按业务模块组织，以便快速定位和维护特定功能的代码。

#### 验收标准

1. THE 项目 SHALL 将所有业务代码迁移到 `com.xiuxian.game.modules.<模块名>` 包下
2. THE 项目 SHALL 将所有基础设施代码迁移到 `com.xiuxian.game.common.<子包>` 包下
3. THE 项目 SHALL 保持 `com.xiuxian.game.dto` 包不变（跨模块共用）
4. WHEN 迁移完成后，THE 项目 SHALL 删除原扁平化包结构中的所有文件

### 需求 2：渐进式迁移

**用户故事：** 作为开发者，我希望迁移过程不破坏编译，以便在迁移期间项目始终可运行。

#### 验收标准

1. WHILE 迁移进行中，THE 项目 SHALL 保持编译通过
2. WHEN 迁移单个文件时，THE 系统 SHALL 在新位置创建文件并更新 package 声明
3. WHEN 迁移单个文件时，THE 系统 SHALL 更新文件内引用已迁移类的 import 语句
4. WHILE 迁移进行中，THE 系统 SHALL 保留旧位置文件直到所有模块迁移完成

### 需求 3：代码完整性

**用户故事：** 作为开发者，我希望迁移后业务逻辑完全不变，以便功能正常运行。

#### 验收标准

1. WHEN 迁移文件时，THE 系统 SHALL 只修改 package 声明和 import 语句，不改变任何业务逻辑
2. WHEN 所有模块迁移完成后，THE 项目 SHALL 通过 `mvn compile` 编译验证
3. THE 项目 SHALL 包含 22 个业务模块和 1 个 common 模块，共 23 个模块

### 需求 4：模块文件归属

**用户故事：** 作为开发者，我希望每个文件都归属到正确的业务模块，以便模块边界清晰。

#### 验收标准

1. THE 每个模块 SHALL 包含该业务领域的 entity、mapper、service、controller 四层代码
2. WHEN 文件属于多个模块时，THE 系统 SHALL 按主要业务职责归属到对应模块
3. THE admin 模块 SHALL 包含所有运营管理相关的 service 和 controller（包括缓存、安全、统计等基础服务）
