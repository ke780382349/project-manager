import React from "react";
import { PERMISSION_IDS } from "../permissions.js";
import { UserAvatar } from "./avatar.jsx";

const navigationGroups = [
  {
    title: "工作区",
    items: [
      { id: "dashboard", label: "工作台", icon: "⌂" },
      { id: "my-tasks", label: "我的任务", icon: "✓" },
      { id: "projects", label: "项目", icon: "▣", permission: PERMISSION_IDS.PROJECT_VIEW },
    ],
  },
];

const standaloneNavigationItems = [
  { id: "users", label: "用户管理", icon: "♙", permission: PERMISSION_IDS.USER_MANAGE },
  { id: "roles", label: "角色管理", icon: "◆", permission: PERMISSION_IDS.ROLE_MANAGE },
  { id: "permissions", label: "权限管理", icon: "◇", permission: PERMISSION_IDS.ROLE_MANAGE },
  { id: "notifications", label: "通知中心", icon: "◌" },
  { id: "settings", label: "设置", icon: "⚙" },
];

function findNavigationItem(id) {
  return [...navigationGroups.flatMap((group) => group.items), ...standaloneNavigationItems].find((item) => item.id === id) ?? {
    id: "dashboard",
    label: "工作台",
    icon: "⌂",
  };
}

function roleLabel(role, name) {
  return name || (role === "ADMIN" ? "管理员" : role === "USER" ? "普通用户" : role);
}

function Sidebar({ user, activeId, onNavigate, onLogout, mobileOpen, onClose }) {
  const secondaryItems = standaloneNavigationItems.filter((item) => item.id !== "settings" && (!item.permission || user.permissions?.includes(item.permission)));
  return (
    <>
      {mobileOpen && <button aria-label="关闭菜单" className="sidebar-backdrop" onClick={onClose} type="button" />}
      <aside className={`sidebar ${mobileOpen ? "sidebar-open" : ""}`}>
        <div className="brand">
          <span className="brand-mark">PM</span>
          <span>项目管理</span>
        </div>

        <button className="workspace-switcher" type="button">
          <span className="workspace-mark">P</span>
          <span className="workspace-copy"><strong>项目管理空间</strong><small>默认工作空间</small></span>
          <span className="workspace-chevron">⌄</span>
        </button>

        <nav className="sidebar-nav" aria-label="主导航">
          {navigationGroups.map((group) => (
            <div className="nav-group" key={group.title}>
              <p className="nav-group-title">{group.title}</p>
              {group.items.filter((item) => !item.permission || user.permissions?.includes(item.permission)).map((item) => (
                <button
                  className={`nav-item ${activeId === item.id ? "nav-item-active" : ""}`}
                  key={item.id}
                  onClick={() => { onNavigate(item.id); onClose(); }}
                  type="button"
                >
                  <span className="nav-icon" aria-hidden="true">{item.icon}</span>
                  <span>{item.label}</span>
                </button>
              ))}
            </div>
          ))}
          <div className="nav-group nav-group-last">
            <p className="nav-group-title">其他</p>
            {secondaryItems.map((item) => (
              <button
                className={`nav-item ${activeId === item.id ? "nav-item-active" : ""}`}
                key={item.id}
                onClick={() => { onNavigate(item.id); onClose(); }}
                type="button"
              >
                <span className="nav-icon" aria-hidden="true">{item.icon}</span>
                <span>{item.label}</span>
                {item.id === "notifications" && <span className="notification-dot" aria-label="有通知" />}
              </button>
            ))}
          </div>
        </nav>

        <div className="sidebar-footer">
          <button
            className={`nav-item ${activeId === "settings" ? "nav-item-active" : ""}`}
            onClick={() => { onNavigate("settings"); onClose(); }}
            type="button"
          >
            <span className="nav-icon" aria-hidden="true">⚙</span>
            <span>设置</span>
          </button>
          <div className="sidebar-user">
            <UserAvatar user={user} small />
            <span className="sidebar-user-copy"><strong>{user.displayName}</strong><small>{roleLabel(user.role, user.roleName)}</small></span>
            <button aria-label="退出登录" className="logout-icon" onClick={onLogout} type="button">↪</button>
          </div>
        </div>
      </aside>
    </>
  );
}

export { Sidebar, findNavigationItem };
