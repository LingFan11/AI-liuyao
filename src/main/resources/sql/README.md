# 六爻智能解卦系统 - 数据库SQL脚本

## 📁 脚本目录结构

```
sql/
├── 00_create_database.sql              # 创建数据库
├── 01_create_table_users.sql           # 创建用户表
├── 02_create_table_hexagrams.sql       # 创建卦象表
├── 03_create_table_interpretations.sql # 创建解释表
├── 04_create_table_divination_histories.sql # 创建历史记录表
├── 05_create_table_knowledge.sql       # 创建知识库表（3张表）
├── 06_insert_hexagrams_data.sql        # 插入六十四卦数据
├── 07_insert_test_data.sql             # 插入测试数据
└── execute_all.sql                     # 一键执行所有脚本
```

## 🚀 快速开始

### 方式一：使用一键执行脚本（推荐）

```bash
# 在MySQL命令行中执行
mysql -u root -p < src/main/resources/sql/execute_all.sql
```

### 方式二：逐个执行脚本

```bash
# 1. 创建数据库
mysql -u root -p < src/main/resources/sql/00_create_database.sql

# 2. 创建表结构
mysql -u root -p < src/main/resources/sql/01_create_table_users.sql
mysql -u root -p < src/main/resources/sql/02_create_table_hexagrams.sql
mysql -u root -p < src/main/resources/sql/03_create_table_interpretations.sql
mysql -u root -p < src/main/resources/sql/04_create_table_divination_histories.sql
mysql -u root -p < src/main/resources/sql/05_create_table_knowledge.sql

# 3. 插入基础数据
mysql -u root -p < src/main/resources/sql/06_insert_hexagrams_data.sql

# 4. 插入测试数据（可选，仅开发环境）
mysql -u root -p < src/main/resources/sql/07_insert_test_data.sql
```

### 方式三：使用MySQL Workbench

1. 打开MySQL Workbench
2. 连接到数据库
3. 依次打开并执行每个SQL脚本文件

## 📊 数据库设计说明

### 核心表

#### 1. users - 用户表
- **用途**: 存储用户账号信息、个人资料、VIP信息
- **关键字段**: 
  - `username`: 用户名（唯一）
  - `password`: 密码（BCrypt加密）
  - `vip_type`: VIP类型（0-普通，1-月度，2-年度）
  - `daily_divination_count`: 今日占卜次数

#### 2. hexagrams - 卦象表
- **用途**: 存储用户起卦的卦象信息
- **关键字段**:
  - `hexagram_code`: 六爻编码（如：111111表示乾卦）
  - `original_hex`: 本卦名称
  - `changed_hex`: 变卦名称（如有变爻）
  - `method`: 起卦方式（1-手动，2-时间，3-数字，4-随机）

 - **同一性判定要素（新增）**：为满足“卦象一致、变卦一致、用神五行等信息一致才算相同”的业务规则，新增以下规范化字段，并引入签名：
   - `palace`: 卦宫（乾、坎、艮、震、巽、离、坤、兑）
   - `shi_line` / `ying_line`: 世爻/应爻爻位（1-6）
   - `mutual_hex_number` / `opposite_hex_number`: 互卦/综卦序号（1-64）
   - `na_jia`: 纳甲排盘（JSON，含六爻地支、六亲、六神等）
   - `yong_shen`/`yong_shen_element`/`yong_shen_branch`/`yong_shen_line`: 用神六亲/五行/地支/爻位
   - `yue_jian`/`ri_zhi`: 月建/日支（用于旺衰判定一致性）
   - `signature`: 同一性签名（SHA-256），由应用层基于上述字段规范化后计算写入

 - **唯一约束（新增）**：`UNIQUE KEY uk_user_hex_signature (user_id, signature)`
   - 语义：同一用户下，同一“规范化卦象结果”只能保存一条记录
   - 注：签名留空时不触发唯一约束冲突（MySQL允许多条 NULL）

 - **签名生成参考**：
   - SQL（示意）
     ```sql
     SELECT SHA2(LOWER(CONCAT_WS('|',
         original_hex_number,
         COALESCE(changed_hex_number, 0),
         COALESCE(changing_lines, ''),
         COALESCE(palace, ''),
         COALESCE(shi_line, 0),
         COALESCE(ying_line, 0),
         COALESCE(mutual_hex_number, 0),
         COALESCE(yong_shen, ''),
         COALESCE(yong_shen_element, ''),
         COALESCE(yong_shen_branch, ''),
         COALESCE(yong_shen_line, 0),
         COALESCE(yue_jian, ''),
         COALESCE(ri_zhi, '')
     )), 256) AS signature;
     ```
   - Java（示意）
     ```java
     public static String buildHexSignature(Hex h) {
         String base = String.join("|",
             String.valueOf(h.getOriginalHexNumber()),
             String.valueOf(Optional.ofNullable(h.getChangedHexNumber()).orElse(0)),
             defaultStr(h.getChangingLines()),
             defaultStr(h.getPalace()),
             String.valueOf(orZero(h.getShiLine())),
             String.valueOf(orZero(h.getYingLine())),
             String.valueOf(orZero(h.getMutualHexNumber())),
             defaultStr(h.getYongShen()),
             defaultStr(h.getYongShenElement()),
             defaultStr(h.getYongShenBranch()),
             String.valueOf(orZero(h.getYongShenLine())),
             defaultStr(h.getYueJian()),
             defaultStr(h.getRiZhi())
         ).toLowerCase();
         return DigestUtils.sha256Hex(base);
     }
     ```

#### 3. interpretations - 解释表
- **用途**: 存储卦象的解释（基础+AI）
- **关键字段**:
  - `basic_interpretation`: 基础解释
  - `ai_interpretation`: AI智能解释
  - `judgment`: 吉凶判断
  - `confidence`: 置信度

#### 4. divination_histories - 历史记录表
- **用途**: 存储用户的占卜历史和收藏
- **关键字段**:
  - `is_favorite`: 是否收藏
  - `notes`: 用户备注
  - `verification_result`: 验证结果

#### 5. hexagram_knowledge - 卦象知识库表
- **用途**: 存储六十四卦的基础知识
- **关键字段**:
  - `sequence`: 卦序（1-64）
  - `hexagram_name`: 卦名
  - `hexagram_text`: 卦辞原文
  - `hexagram_explanation`: 白话解释

#### 6. yao_knowledge - 爻辞知识库表
- **用途**: 存储每卦六爻的爻辞知识
- **关键字段**:
  - `hexagram_id`: 关联的卦象
  - `position`: 爻位（1-6）
  - `yao_text`: 爻辞原文

#### 7. case_studies - 案例库表
- **用途**: 存储占卜案例和验证结果
- **关键字段**:
  - `question`: 占卜问题
  - `interpretation`: 解卦内容
  - `verification`: 实际验证结果

## 🔑 测试账号

执行测试数据脚本后，可使用以下账号登录：

| 用户名 | 密码 | 角色 | VIP等级 |
|--------|------|------|---------|
| testuser1 | 123456 | 普通用户 | 无 |
| testuser2 | 123456 | 普通用户 | 月度VIP |
| vipuser | 123456 | VIP用户 | 年度VIP |
| admin | 123456 | 管理员 | 年度VIP |

## ⚠️ 注意事项

1. **生产环境部署**:
   - **不要执行** `07_insert_test_data.sql`
   - 修改默认密码和数据库连接信息
   - 定期备份数据库

2. **密码加密**:
   - 测试数据中的密码使用BCrypt加密
   - 实际的加密哈希值需要在应用中生成

3. **数据完整性**:
   - 脚本包含外键约束和索引
   - 删除数据时注意关联关系

4. **字符集**:
   - 数据库使用 `utf8mb4` 字符集
   - 支持中文和emoji表情

## 🔄 数据库更新流程

如果需要修改表结构：

1. 创建新的迁移脚本（如：`08_alter_table_xxx.sql`）
2. 在脚本中注明版本号和修改日期
3. 更新本README文档
4. 在测试环境验证后再应用到生产环境

## 📝 变更日志

### Version 1.0.0 (2025-10-22)
- ✅ 创建数据库 `liuyao_db`
- ✅ 创建用户表 `users`
- ✅ 创建卦象表 `hexagrams`
- ✅ 创建解释表 `interpretations`
- ✅ 创建历史记录表 `divination_histories`
- ✅ 创建知识库表 `hexagram_knowledge`, `yao_knowledge`, `case_studies`
- ✅ 插入六十四卦基础数据（示例20卦）
- ✅ 插入测试数据（用户、卦象、解释、历史记录、案例）

## 🛠️ 维护建议

1. **定期备份**: 每天自动备份数据库
2. **索引优化**: 监控慢查询，适时添加索引
3. **数据清理**: 定期清理测试数据和过期数据
4. **性能监控**: 使用慢查询日志分析性能瓶颈

## 📞 技术支持

如有问题，请联系开发团队或查看项目文档。
