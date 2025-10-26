-- ================================
-- 六爻智能解卦系统 - 角色权限表创建脚本
-- ================================
-- 描述: RBAC权限模型，支持用户-角色-权限的多对多关系
-- 版本: 1.0.0
-- 日期: 2025-10-26
-- ================================

USE liuyao_db;

-- ================================
-- 1. 角色表 (roles)
-- ================================
DROP TABLE IF EXISTS roles;

CREATE TABLE roles (
    -- 主键
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    
    -- 角色信息
    role_code VARCHAR(50) UNIQUE NOT NULL COMMENT '角色编码（唯一，如：admin, vip, user）',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称（如：管理员、VIP会员、普通用户）',
    description VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    
    -- 角色状态
    status INT DEFAULT 1 COMMENT '状态：0-禁用, 1-启用',
    sort_order INT DEFAULT 0 COMMENT '排序顺序（数字越小越靠前）',
    
    -- 时间戳
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除, 1-已删除',
    
    -- 索引
    INDEX idx_role_code (role_code),
    INDEX idx_status (status)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';


-- ================================
-- 2. 权限表 (permissions)
-- ================================
DROP TABLE IF EXISTS permissions;

CREATE TABLE permissions (
    -- 主键
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '权限ID',
    
    -- 权限信息
    permission_code VARCHAR(100) UNIQUE NOT NULL COMMENT '权限编码（唯一，如：user:create, divination:view）',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称（如：创建用户、查看占卜）',
    resource_type VARCHAR(50) NOT NULL COMMENT '资源类型（如：user, divination, interpretation）',
    description VARCHAR(255) DEFAULT NULL COMMENT '权限描述',
    
    -- 权限分类
    parent_id BIGINT DEFAULT NULL COMMENT '父权限ID（支持权限树结构）',
    
    -- 权限状态
    status INT DEFAULT 1 COMMENT '状态：0-禁用, 1-启用',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    
    -- 时间戳
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除, 1-已删除',
    
    -- 索引
    INDEX idx_permission_code (permission_code),
    INDEX idx_resource_type (resource_type),
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';


-- ================================
-- 3. 用户-角色关联表 (user_roles)
-- ================================
DROP TABLE IF EXISTS user_roles;

CREATE TABLE user_roles (
    -- 主键
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    
    -- 关联信息
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    
    -- 时间戳
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    -- 唯一约束：一个用户不能重复拥有同一个角色
    UNIQUE KEY uk_user_role (user_id, role_id),
    
    -- 索引
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-角色关联表';


-- ================================
-- 4. 角色-权限关联表 (role_permissions)
-- ================================
DROP TABLE IF EXISTS role_permissions;

CREATE TABLE role_permissions (
    -- 主键
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    
    -- 关联信息
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    
    -- 时间戳
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    -- 唯一约束：一个角色不能重复拥有同一个权限
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    
    -- 索引
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色-权限关联表';


-- 显示创建成功信息
SELECT 'RBAC权限表创建成功！已创建：roles, permissions, user_roles, role_permissions' AS Status;
