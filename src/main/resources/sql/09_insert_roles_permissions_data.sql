-- ================================
-- 六爻智能解卦系统 - 角色权限初始数据
-- ================================
-- 描述: 插入系统初始角色、权限和关联关系
-- 版本: 1.0.0
-- 日期: 2025-10-26
-- ================================

USE liuyao_db;

-- ================================
-- 1. 插入角色数据 (roles)
-- ================================
INSERT INTO roles (id, role_code, role_name, description, status, sort_order) VALUES
(1, 'admin', '超级管理员', '拥有系统所有权限，可管理用户、配置系统', 1, 1),
(2, 'vip_year', '年度VIP会员', '年度VIP会员，享受高级解卦服务和更多占卜次数', 1, 2),
(3, 'vip_month', '月度VIP会员', '月度VIP会员，享受高级解卦服务', 1, 3),
(4, 'user', '普通用户', '系统基础用户，拥有基本占卜和查询功能', 1, 4),
(5, 'guest', '游客', '未登录用户，仅可查看公开内容', 1, 5);

-- ================================
-- 2. 插入权限数据 (permissions)
-- ================================

-- 用户管理权限 (user)
INSERT INTO permissions (id, permission_code, permission_name, resource_type, description, parent_id, status, sort_order) VALUES
(1, 'user:view', '查看用户', 'user', '查看用户信息', NULL, 1, 1),
(2, 'user:create', '创建用户', 'user', '创建新用户', NULL, 1, 2),
(3, 'user:update', '更新用户', 'user', '更新用户信息', NULL, 1, 3),
(4, 'user:delete', '删除用户', 'user', '删除用户（逻辑删除）', NULL, 1, 4),
(5, 'user:manage', '管理用户', 'user', '用户管理（包含所有用户操作）', NULL, 1, 5);

-- 占卜功能权限 (divination)
INSERT INTO permissions (id, permission_code, permission_name, resource_type, description, parent_id, status, sort_order) VALUES
(11, 'divination:create', '起卦', 'divination', '创建占卜记录', NULL, 1, 11),
(12, 'divination:view', '查看占卜', 'divination', '查看自己的占卜记录', NULL, 1, 12),
(13, 'divination:view_all', '查看所有占卜', 'divination', '查看所有用户的占卜记录（管理员）', NULL, 1, 13),
(14, 'divination:delete', '删除占卜', 'divination', '删除占卜记录', NULL, 1, 14),
(15, 'divination:export', '导出占卜', 'divination', '导出占卜记录', NULL, 1, 15);

-- 解卦功能权限 (interpretation)
INSERT INTO permissions (id, permission_code, permission_name, resource_type, description, parent_id, status, sort_order) VALUES
(21, 'interpretation:basic', '基础解卦', 'interpretation', '使用基础解卦功能', NULL, 1, 21),
(22, 'interpretation:ai', 'AI解卦', 'interpretation', '使用AI智能解卦', NULL, 1, 22),
(23, 'interpretation:advanced', '高级解卦', 'interpretation', '使用高级解卦功能（六爻详解）', NULL, 1, 23),
(24, 'interpretation:stream', '流式解卦', 'interpretation', '使用流式输出解卦', NULL, 1, 24);

-- 历史记录权限 (history)
INSERT INTO permissions (id, permission_code, permission_name, resource_type, description, parent_id, status, sort_order) VALUES
(31, 'history:view', '查看历史', 'history', '查看自己的历史记录', NULL, 1, 31),
(32, 'history:favorite', '收藏记录', 'history', '收藏占卜记录', NULL, 1, 32),
(33, 'history:export', '导出历史', 'history', '导出历史记录', NULL, 1, 33);

-- 知识库权限 (knowledge)
INSERT INTO permissions (id, permission_code, permission_name, resource_type, description, parent_id, status, sort_order) VALUES
(41, 'knowledge:view', '查看知识库', 'knowledge', '查看卦象知识库', NULL, 1, 41),
(42, 'knowledge:search', '搜索知识库', 'knowledge', '搜索卦象和案例', NULL, 1, 42),
(43, 'knowledge:manage', '管理知识库', 'knowledge', '管理知识库内容（管理员）', NULL, 1, 43);

-- 系统管理权限 (system)
INSERT INTO permissions (id, permission_code, permission_name, resource_type, description, parent_id, status, sort_order) VALUES
(51, 'system:config', '系统配置', 'system', '修改系统配置', NULL, 1, 51),
(52, 'system:monitor', '系统监控', 'system', '查看系统监控数据', NULL, 1, 52),
(53, 'system:log', '日志管理', 'system', '查看和管理系统日志', NULL, 1, 53),
(54, 'system:backup', '数据备份', 'system', '备份和恢复数据', NULL, 1, 54);

-- ================================
-- 3. 角色-权限关联 (role_permissions)
-- ================================

-- 超级管理员：拥有所有权限
INSERT INTO role_permissions (role_id, permission_id) 
SELECT 1, id FROM permissions WHERE deleted = 0;

-- 年度VIP会员：高级占卜和解卦权限
INSERT INTO role_permissions (role_id, permission_id) VALUES
-- 用户权限
(2, 1), -- user:view
(2, 3), -- user:update
-- 占卜权限
(2, 11), -- divination:create
(2, 12), -- divination:view
(2, 14), -- divination:delete
(2, 15), -- divination:export
-- 解卦权限（全部）
(2, 21), -- interpretation:basic
(2, 22), -- interpretation:ai
(2, 23), -- interpretation:advanced
(2, 24), -- interpretation:stream
-- 历史记录权限（全部）
(2, 31), -- history:view
(2, 32), -- history:favorite
(2, 33), -- history:export
-- 知识库权限
(2, 41), -- knowledge:view
(2, 42); -- knowledge:search

-- 月度VIP会员：基础占卜和AI解卦
INSERT INTO role_permissions (role_id, permission_id) VALUES
-- 用户权限
(3, 1), -- user:view
(3, 3), -- user:update
-- 占卜权限
(3, 11), -- divination:create
(3, 12), -- divination:view
(3, 14), -- divination:delete
-- 解卦权限（基础+AI）
(3, 21), -- interpretation:basic
(3, 22), -- interpretation:ai
-- 历史记录权限
(3, 31), -- history:view
(3, 32), -- history:favorite
-- 知识库权限
(3, 41), -- knowledge:view
(3, 42); -- knowledge:search

-- 普通用户：基础功能
INSERT INTO role_permissions (role_id, permission_id) VALUES
-- 用户权限
(4, 1), -- user:view
(4, 3), -- user:update
-- 占卜权限（基础）
(4, 11), -- divination:create
(4, 12), -- divination:view
-- 解卦权限（仅基础）
(4, 21), -- interpretation:basic
-- 历史记录权限（仅查看）
(4, 31), -- history:view
-- 知识库权限（仅查看）
(4, 41); -- knowledge:view

-- 游客：仅查看权限
INSERT INTO role_permissions (role_id, permission_id) VALUES
(5, 41); -- knowledge:view

-- ================================
-- 4. 为测试用户分配角色 (user_roles)
-- ================================

-- 测试用户1 (test_user) → 普通用户
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 4); -- user角色

-- 测试用户2 (vip_user) → 月度VIP会员
INSERT INTO user_roles (user_id, role_id) VALUES
(2, 3); -- vip_month角色

-- 测试用户3 (admin_user) → 超级管理员
INSERT INTO user_roles (user_id, role_id) VALUES
(3, 1); -- admin角色

-- 测试用户4 (year_vip_user) → 年度VIP会员
INSERT INTO user_roles (user_id, role_id) VALUES
(4, 2); -- vip_year角色

-- 显示插入成功信息
SELECT '角色权限数据插入成功！' AS Status;
SELECT CONCAT('已插入 ', COUNT(*), ' 个角色') AS RoleCount FROM roles WHERE deleted = 0;
SELECT CONCAT('已插入 ', COUNT(*), ' 个权限') AS PermissionCount FROM permissions WHERE deleted = 0;
SELECT CONCAT('已配置 ', COUNT(*), ' 条角色-权限关联') AS RolePermissionCount FROM role_permissions;
SELECT CONCAT('已配置 ', COUNT(*), ' 条用户-角色关联') AS UserRoleCount FROM user_roles;
