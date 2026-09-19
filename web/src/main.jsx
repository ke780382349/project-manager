import React from "react";
import { createRoot } from "react-dom/client";
import "./styles.css";

const TOKEN_KEY = "project_manager_token";
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "";
const PERMISSION_IDS = {
  USER_MANAGE: "188F3A564F4243468F849927D46E8295",
  ROLE_MANAGE: "029A7C6E986F453398F5B093AED62B61",
};

async function request(path, options = {}) {
  const token = localStorage.getItem(TOKEN_KEY);
  const headers = { ...(options.body ? { "Content-Type": "application/json" } : {}), ...options.headers };
  if (token) headers.Authorization = `Bearer ${token}`;

  const response = await fetch(`${API_BASE_URL}${path}`, { ...options, headers });
  const text = await response.text();
  let data = {};
  try {
    data = text ? JSON.parse(text) : {};
  } catch {
    data = { message: text };
  }
  if (!response.ok) {
    throw new Error(data.detail || data.message || data.error || "请求失败");
  }
  return data;
}

function AuthForm({ onAuthenticated }) {
  const [account, setAccount] = React.useState("");
  const [password, setPassword] = React.useState("");
  const [message, setMessage] = React.useState("");
  const [submitting, setSubmitting] = React.useState(false);

  async function submit(event) {
    event.preventDefault();
    setMessage("");
    setSubmitting(true);
    try {
      const data = await request("/api/auth/login", {
        method: "POST",
        body: JSON.stringify({ account: account.trim(), password }),
      });
      localStorage.setItem(TOKEN_KEY, data.token);
      onAuthenticated(data.user);
    } catch (error) {
      setMessage(error instanceof TypeError ? "无法连接服务端，请确认后端运行在 8080 端口" : error.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="auth-card card">
      <p className="eyebrow">PROJECT MANAGER</p>
      <h1>统一登录</h1>
      <p className="muted">请使用管理员为你开通的账号登录系统。</p>
      <form onSubmit={submit}>
        <label>账号或邮箱
          <input type="text" value={account} onChange={(event) => setAccount(event.target.value)} required autoComplete="username" placeholder="请输入账号或邮箱" />
        </label>
        <label>密码
          <input type="password" value={password} onChange={(event) => setPassword(event.target.value)} required autoComplete="current-password" placeholder="请输入密码" />
        </label>
        <button className="primary" disabled={submitting} type="submit">{submitting ? "登录中…" : "登录"}</button>
      </form>
      <p className="message">{message}</p>
    </section>
  );
}

const navigationGroups = [
  {
    title: "工作区",
    items: [
      { id: "dashboard", label: "工作台", icon: "⌂" },
      { id: "my-tasks", label: "我的任务", icon: "✓" },
      { id: "projects", label: "项目", icon: "▣" },
    ],
  },
  {
    title: "研发管理",
    items: [
      { id: "requirements", label: "需求池", icon: "◇" },
      { id: "bugs", label: "Bug", icon: "⚠" },
      { id: "changes", label: "需求变更", icon: "↗" },
      { id: "releases", label: "版本发布", icon: "◷" },
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

function UserAvatar({ user, small = false }) {
  const name = user?.displayName || user?.username || user?.email || "用户";
  return <span className={`avatar ${small ? "avatar-small" : ""}`}>{name.slice(0, 1).toUpperCase()}</span>;
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
              {group.items.map((item) => (
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

function WorkspaceHome({ user }) {
  return (
    <>
      <div className="page-heading">
        <div>
          <p className="eyebrow">WORKSPACE OVERVIEW</p>
          <h1>早上好，{user.displayName}</h1>
          <p className="muted">从这里查看项目进度和需要你处理的事项。</p>
        </div>
      </div>
      <div className="stat-grid">
        <div className="stat-card"><span className="stat-label">进行中的项目</span><strong>—</strong><small>项目模块即将接入</small></div>
        <div className="stat-card"><span className="stat-label">我的待办</span><strong>—</strong><small>任务模块即将接入</small></div>
        <div className="stat-card"><span className="stat-label">待处理需求</span><strong>—</strong><small>需求模块即将接入</small></div>
        <div className="stat-card"><span className="stat-label">未关闭 Bug</span><strong>—</strong><small>Bug 模块即将接入</small></div>
      </div>
      <div className="home-grid">
        <section className="content-card">
          <div className="card-heading"><div><h2>最近活动</h2><p>团队中的最新变化会显示在这里。</p></div><button className="link-button" type="button">查看全部</button></div>
          <div className="empty-panel"><span className="empty-icon">✦</span><strong>还没有活动</strong><span>创建项目后，活动记录会出现在这里。</span></div>
        </section>
        <section className="content-card">
          <div className="card-heading"><div><h2>我的任务</h2><p>你负责的待办事项。</p></div><button className="link-button" type="button">查看全部</button></div>
          <div className="empty-panel"><span className="empty-icon">✓</span><strong>还没有任务</strong><span>任务模块接入后可以在这里跟进。</span></div>
        </section>
      </div>
    </>
  );
}

function PlaceholderPage({ item }) {
  return (
    <div className="placeholder-page">
      <span className="placeholder-icon">{item.icon}</span>
      <h1>{item.label}</h1>
      <p>这个模块已经加入系统导航，后续将接入对应的业务功能。</p>
    </div>
  );
}

function RoleDropdown({ roles, value, onChange, compact = false, disabled = false }) {
  const [open, setOpen] = React.useState(false);
  const containerRef = React.useRef(null);
  const selectedRole = roles.find((role) => role.id === value);

  React.useEffect(() => {
    function closeOnOutsideClick(event) {
      if (!containerRef.current?.contains(event.target)) setOpen(false);
    }
    function closeOnEscape(event) {
      if (event.key === "Escape") setOpen(false);
    }
    document.addEventListener("mousedown", closeOnOutsideClick);
    document.addEventListener("keydown", closeOnEscape);
    return () => {
      document.removeEventListener("mousedown", closeOnOutsideClick);
      document.removeEventListener("keydown", closeOnEscape);
    };
  }, []);

  function selectRole(role) {
    onChange(role.id);
    setOpen(false);
  }

  return (
    <div className={`custom-select ${compact ? "custom-select-compact" : ""} ${open ? "custom-select-open" : ""}`} ref={containerRef}>
      <button
        aria-expanded={open}
        aria-haspopup="listbox"
        className="custom-select-trigger"
        disabled={disabled}
        onClick={() => setOpen((current) => !current)}
        type="button"
      >
        <span className="custom-select-value">{selectedRole?.name || "请选择角色"}</span>
        <span className="custom-select-arrow" aria-hidden="true">⌄</span>
      </button>
      {open && <div className="custom-select-menu" role="listbox">
        {roles.map((role) => <button
          aria-selected={role.id === value}
          className={`custom-select-option ${role.id === value ? "custom-select-option-active" : ""}`}
          key={role.id}
          onClick={() => selectRole(role)}
          role="option"
          type="button"
        >
          <span>{role.name}</span>
          {role.id === value && <b aria-hidden="true">✓</b>}
        </button>)}
      </div>}
    </div>
  );
}

function UserManagement({ currentUser }) {
  const [users, setUsers] = React.useState([]);
  const [roles, setRoles] = React.useState([]);
  const [loading, setLoading] = React.useState(true);
  const [submitting, setSubmitting] = React.useState(false);
  const [message, setMessage] = React.useState("");
  const [showCreateModal, setShowCreateModal] = React.useState(false);
  const [form, setForm] = React.useState({ username: "", email: "", displayName: "", password: "", roleId: "" });

  async function loadUsers() {
    try {
      const [userList, roleList] = await Promise.all([request("/api/users"), request("/api/roles")]);
      setUsers(userList);
      setRoles(roleList);
      setForm((current) => ({ ...current, roleId: current.roleId || roleList[0]?.id || "" }));
    } catch (error) {
      setMessage(error.message);
    } finally {
      setLoading(false);
    }
  }

  React.useEffect(() => { loadUsers(); }, []);

  function updateField(event) {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  }

  function openCreateModal() {
    setForm({ username: "", email: "", displayName: "", password: "", roleId: roles[0]?.id || "" });
    setMessage("");
    setShowCreateModal(true);
  }

  async function createUser(event) {
    event.preventDefault();
    setSubmitting(true);
    setMessage("");
    try {
      await request("/api/users", { method: "POST", body: JSON.stringify(form) });
      setForm({ username: "", email: "", displayName: "", password: "", roleId: roles[0]?.id || "" });
      await loadUsers();
      setShowCreateModal(false);
      setMessage("用户已开通");
    } catch (error) {
      setMessage(error.message);
    } finally {
      setSubmitting(false);
    }
  }

  async function toggleUser(user) {
    try {
      const updated = await request(`/api/users/${user.id}/status`, {
        method: "PATCH",
        body: JSON.stringify({ enabled: !user.enabled }),
      });
      setUsers((current) => current.map((item) => item.id === updated.id ? updated : item));
    } catch (error) {
      setMessage(error.message);
    }
  }

  async function changeRole(user, role) {
    try {
      const updated = await request(`/api/users/${user.id}/role`, {
        method: "PATCH",
        body: JSON.stringify({ roleId: role }),
      });
      setUsers((current) => current.map((item) => item.id === updated.id ? updated : item));
    } catch (error) {
      setMessage(error.message);
    }
  }

  return (
    <>
      <div className="page-heading">
        <div><p className="eyebrow">ACCESS CONTROL</p><h1>用户管理</h1><p className="muted">统一管理系统账号、角色和登录状态。</p></div>
        <button className="primary action-button" onClick={openCreateModal} type="button">+ 添加用户</button>
      </div>
      <section className="content-card user-list-card user-list-full">
          <div className="card-heading"><div><h2>用户列表</h2><p>共 {users.length} 个账号，管理员可以直接调整角色和状态。</p></div><span className="list-message">{message}</span></div>
          {loading ? <div className="table-empty">正在加载用户…</div> : (
            <div className="user-table-wrap"><table className="user-table"><thead><tr><th>用户</th><th>邮箱</th><th>角色</th><th>状态</th><th>操作</th></tr></thead><tbody>
              {users.map((item) => { const isCurrentUser = item.id === currentUser.id; return <tr key={item.id}><td><div className="table-user"><UserAvatar user={item} small /><span><strong>{item.displayName}</strong><small>{item.username || "未设置账号"}</small></span></div></td><td className="user-email-cell">{item.email}</td><td><RoleDropdown compact disabled={isCurrentUser} roles={roles} value={item.roleId || ""} onChange={(roleId) => changeRole(item, roleId)} /></td><td><span className={`status-pill ${item.enabled ? "status-enabled" : "status-disabled"}`}>{item.enabled ? "启用" : "停用"}</span></td><td><button className="table-action" disabled={isCurrentUser} title={isCurrentUser ? "不能停用自己的账号" : undefined} onClick={() => toggleUser(item)} type="button">{item.enabled ? "停用" : "启用"}</button></td></tr>; })}
            </tbody></table></div>
          )}
      </section>
      {showCreateModal && <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) setShowCreateModal(false); }}>
        <section aria-labelledby="create-user-title" aria-modal="true" className="modal-card" role="dialog">
          <div className="modal-heading"><div><p className="eyebrow">NEW ACCOUNT</p><h2 id="create-user-title">添加用户</h2><p>创建后用户即可使用账号登录系统。</p></div><button aria-label="关闭弹窗" className="modal-close" onClick={() => setShowCreateModal(false)} type="button">×</button></div>
          <form className="user-create-form modal-form" onSubmit={createUser}>
            <div className="form-two-columns"><label>账号<input name="username" value={form.username} onChange={updateField} minLength="3" maxLength="80" required autoFocus placeholder="例如 zhangsan" /></label><label>显示名称<input name="displayName" value={form.displayName} onChange={updateField} maxLength="80" required placeholder="用户姓名" /></label></div>
            <div className="form-two-columns"><label>邮箱<input name="email" type="email" value={form.email} onChange={updateField} required placeholder="name@example.com" /></label><label>初始密码<input name="password" type="password" value={form.password} onChange={updateField} minLength="8" maxLength="72" required placeholder="至少 8 位" /></label></div>
            <label>角色<RoleDropdown roles={roles} value={form.roleId} onChange={(roleId) => setForm((current) => ({ ...current, roleId }))} /><input aria-hidden="true" className="visually-hidden-input" name="roleId" value={form.roleId} onChange={() => {}} required tabIndex="-1" /></label>
            <div className="modal-actions"><button className="secondary" onClick={() => setShowCreateModal(false)} type="button">取消</button><button className="primary" disabled={submitting} type="submit">{submitting ? "创建中…" : "创建用户"}</button></div>
          </form>
          <p className="message modal-message">{message}</p>
        </section>
      </div>}
    </>
  );
}

function RoleManagement() {
  const [roles, setRoles] = React.useState([]);
  const [permissions, setPermissions] = React.useState([]);
  const [loading, setLoading] = React.useState(true);
  const [message, setMessage] = React.useState("");
  const [submitting, setSubmitting] = React.useState(false);
  const [editingRole, setEditingRole] = React.useState(null);
  const [showRoleModal, setShowRoleModal] = React.useState(false);
  const [form, setForm] = React.useState({ name: "", description: "", permissions: [] });

  async function loadRoles() {
    try {
      const [roleList, permissionList] = await Promise.all([request("/api/roles"), request("/api/roles/permissions")]);
      setRoles(roleList);
      setPermissions(permissionList);
    } catch (error) {
      setMessage(error.message);
    } finally {
      setLoading(false);
    }
  }

  React.useEffect(() => { loadRoles(); }, []);

  function updateField(event) {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  }

  function togglePermission(permission) {
    setForm((current) => ({
      ...current,
      permissions: current.permissions.includes(permission)
        ? current.permissions.filter((item) => item !== permission)
        : [...current.permissions, permission],
    }));
  }

  function startCreate() {
    setEditingRole(null);
    setForm({ name: "", description: "", permissions: [] });
    setMessage("");
    setShowRoleModal(true);
  }

  function startEdit(role) {
    setEditingRole(role);
    setForm({ name: role.name, description: role.description || "", permissions: role.permissions.map((permission) => permission.id) });
    setMessage("");
    setShowRoleModal(true);
  }

  async function saveRole(event) {
    event.preventDefault();
    const wasEditing = Boolean(editingRole);
    setSubmitting(true);
    setMessage("");
    try {
      const path = editingRole ? `/api/roles/${editingRole.id}` : "/api/roles";
      const method = editingRole ? "PATCH" : "POST";
      await request(path, { method, body: JSON.stringify({ name: form.name, description: form.description, permissionIds: form.permissions }) });
      await loadRoles();
      setEditingRole(null);
      setForm({ name: "", description: "", permissions: [] });
      setShowRoleModal(false);
      setMessage(wasEditing ? "角色已更新" : "角色已创建");
    } catch (error) {
      setMessage(error.message);
    } finally {
      setSubmitting(false);
    }
  }

  async function deleteRole(role) {
    if (!window.confirm(`确定删除角色“${role.name}”吗？`)) return;
    try {
      await request(`/api/roles/${role.id}`, { method: "DELETE" });
      await loadRoles();
      if (editingRole?.id === role.id) {
        setEditingRole(null);
        setShowRoleModal(false);
      }
      setMessage("角色已删除");
    } catch (error) {
      setMessage(error.message);
    }
  }

  return (
    <>
      <div className="page-heading">
        <div><p className="eyebrow">ACCESS CONTROL</p><h1>角色管理</h1><p className="muted">创建角色并配置它可以访问的系统功能。</p></div>
        <button className="primary action-button" onClick={startCreate} type="button">+ 新增角色</button>
      </div>
      <section className="content-card role-list-card role-list-full">
        <div className="card-heading"><div><h2>角色列表</h2><p>共 {roles.length} 个角色，内置角色用于系统基础权限。</p></div><span className="list-message">{message}</span></div>
        {loading ? <div className="table-empty">正在加载角色…</div> : <div className="role-table-wrap"><table className="role-table"><thead><tr><th>角色</th><th>描述</th><th>权限</th><th>用户数</th><th>类型</th><th>操作</th></tr></thead><tbody>{roles.map((role) => <tr key={role.id}><td><div className="role-table-name"><strong>{role.name}</strong><small>ID：{role.id.slice(0, 8)}…</small></div></td><td className="role-description-cell">{role.description || "暂无描述"}</td><td><div className="role-permission-list">{role.permissions.length ? role.permissions.map((permission) => <span key={permission.id}>{permission.name}</span>) : <span>暂无权限</span>}</div></td><td>{role.userCount}</td><td><span className={`role-type-pill ${role.builtIn ? "role-type-built-in" : "role-type-custom"}`}>{role.builtIn ? "内置" : "自定义"}</span></td><td>{role.builtIn ? <span className="table-hint">不可修改</span> : <div className="table-actions"><button className="link-button" onClick={() => startEdit(role)} type="button">编辑</button><button className="danger-link" onClick={() => deleteRole(role)} type="button">删除</button></div>}</td></tr>)}</tbody></table></div>}
      </section>
      {showRoleModal && <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) setShowRoleModal(false); }}>
        <section aria-labelledby="role-modal-title" aria-modal="true" className="modal-card role-modal-card" role="dialog">
          <div className="modal-heading"><div><p className="eyebrow">ROLE CONFIGURATION</p><h2 id="role-modal-title">{editingRole ? "编辑角色" : "新增角色"}</h2><p>{editingRole ? "调整自定义角色的名称和权限。" : "系统会自动生成唯一角色 ID。"}</p></div><button aria-label="关闭弹窗" className="modal-close" onClick={() => setShowRoleModal(false)} type="button">×</button></div>
          <form className="role-form modal-form" onSubmit={saveRole}>
            <label>角色名称<input name="name" value={form.name} onChange={updateField} maxLength="80" required placeholder="例如 测试人员" /></label>
            <label>角色描述<textarea name="description" value={form.description} onChange={updateField} maxLength="255" rows="3" placeholder="说明这个角色的职责" /></label>
            <fieldset className="permission-fieldset"><legend>权限</legend><div className="permission-grid">{permissions.map((permission) => <label className="permission-option" key={permission.id}><input type="checkbox" checked={form.permissions.includes(permission.id)} onChange={() => togglePermission(permission.id)} /><span>{permission.name}<small>ID：{permission.id}</small></span></label>)}</div></fieldset>
            <div className="modal-actions"><button className="secondary" onClick={() => setShowRoleModal(false)} type="button">取消</button><button className="primary" disabled={submitting} type="submit">{submitting ? "保存中…" : editingRole ? "保存修改" : "创建角色"}</button></div>
          </form>
          <p className="message modal-message">{message}</p>
        </section>
      </div>}
    </>
  );
}

function PermissionManagement() {
  const [permissions, setPermissions] = React.useState([]);
  const [loading, setLoading] = React.useState(true);
  const [submitting, setSubmitting] = React.useState(false);
  const [message, setMessage] = React.useState("");
  const [editingPermission, setEditingPermission] = React.useState(null);
  const [showModal, setShowModal] = React.useState(false);
  const [form, setForm] = React.useState({ name: "", description: "" });

  async function loadPermissions() {
    try {
      setPermissions(await request("/api/permissions"));
    } catch (error) {
      setMessage(error.message);
    } finally {
      setLoading(false);
    }
  }

  React.useEffect(() => { loadPermissions(); }, []);

  function updateField(event) {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  }

  function startCreate() {
    setEditingPermission(null);
    setForm({ name: "", description: "" });
    setMessage("");
    setShowModal(true);
  }

  function startEdit(permission) {
    setEditingPermission(permission);
    setForm({ name: permission.name, description: permission.description || "" });
    setMessage("");
    setShowModal(true);
  }

  async function savePermission(event) {
    event.preventDefault();
    setSubmitting(true);
    setMessage("");
    try {
      const path = editingPermission ? `/api/permissions/${editingPermission.id}` : "/api/permissions";
      const body = editingPermission
        ? { name: form.name, description: form.description }
        : form;
      await request(path, { method: editingPermission ? "PATCH" : "POST", body: JSON.stringify(body) });
      await loadPermissions();
      setShowModal(false);
      setEditingPermission(null);
      setMessage(editingPermission ? "权限已更新" : "权限已创建");
    } catch (error) {
      setMessage(error.message);
    } finally {
      setSubmitting(false);
    }
  }

  async function deletePermission(permission) {
    if (!window.confirm(`确定删除权限“${permission.name}”吗？`)) return;
    try {
      await request(`/api/permissions/${permission.id}`, { method: "DELETE" });
      await loadPermissions();
      setMessage("权限已删除");
    } catch (error) {
      setMessage(error.message);
    }
  }

  return (
    <>
      <div className="page-heading">
        <div><p className="eyebrow">ACCESS CONTROL</p><h1>权限管理</h1><p className="muted">创建可复用的权限，再将权限分配给角色。</p></div>
        <button className="primary action-button" onClick={startCreate} type="button">+ 新增权限</button>
      </div>
      <section className="content-card role-list-card role-list-full">
        <div className="card-heading"><div><h2>权限列表</h2><p>共 {permissions.length} 个权限，可在角色管理中分配。</p></div><span className="list-message">{message}</span></div>
        {loading ? <div className="table-empty">正在加载权限…</div> : <div className="role-table-wrap"><table className="role-table"><thead><tr><th>权限</th><th>描述</th><th>类型</th><th>操作</th></tr></thead><tbody>{permissions.map((permission) => <tr key={permission.id}><td><div className="role-table-name"><strong>{permission.name}</strong><small>ID：{permission.id}</small></div></td><td className="role-description-cell">{permission.description || "暂无描述"}</td><td><span className={`role-type-pill ${permission.builtIn ? "role-type-built-in" : "role-type-custom"}`}>{permission.builtIn ? "内置" : "自定义"}</span></td><td>{permission.builtIn ? <span className="table-hint">不可修改</span> : <div className="table-actions"><button className="link-button" onClick={() => startEdit(permission)} type="button">编辑</button><button className="danger-link" onClick={() => deletePermission(permission)} type="button">删除</button></div>}</td></tr>)}</tbody></table></div>}
      </section>
      {showModal && <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) setShowModal(false); }}>
        <section aria-labelledby="permission-modal-title" aria-modal="true" className="modal-card role-modal-card" role="dialog">
          <div className="modal-heading"><div><p className="eyebrow">PERMISSION CONFIGURATION</p><h2 id="permission-modal-title">{editingPermission ? "编辑权限" : "新增权限"}</h2><p>{editingPermission ? "调整自定义权限的名称和描述。" : "系统会自动生成唯一权限 ID。"}</p></div><button aria-label="关闭弹窗" className="modal-close" onClick={() => setShowModal(false)} type="button">×</button></div>
          <form className="role-form modal-form" onSubmit={savePermission}>
            <label>权限名称<input name="name" value={form.name} onChange={updateField} maxLength="80" required placeholder="例如 导出项目" /></label>
            <label>权限描述<textarea name="description" value={form.description} onChange={updateField} maxLength="255" rows="3" placeholder="说明权限的作用" /></label>
            <div className="modal-actions"><button className="secondary" onClick={() => setShowModal(false)} type="button">取消</button><button className="primary" disabled={submitting} type="submit">{submitting ? "保存中…" : editingPermission ? "保存修改" : "创建权限"}</button></div>
          </form>
          <p className="message modal-message">{message}</p>
        </section>
      </div>}
    </>
  );
}

function Dashboard({ user, onLogout }) {
  const [activeId, setActiveId] = React.useState("dashboard");
  const [mobileOpen, setMobileOpen] = React.useState(false);
  const activeItem = findNavigationItem(activeId);

  return (
    <div className="app-shell">
      <Sidebar
        user={user}
        activeId={activeId}
        onNavigate={setActiveId}
        onLogout={onLogout}
        mobileOpen={mobileOpen}
        onClose={() => setMobileOpen(false)}
      />
      <div className="app-main">
        <header className="topbar">
          <button aria-label="打开菜单" className="mobile-menu-button" onClick={() => setMobileOpen(true)} type="button">☰</button>
          <div className="breadcrumbs"><span>项目管理空间</span><b>/</b><strong>{activeItem.label}</strong></div>
          <div className="topbar-actions"><button aria-label="搜索" className="topbar-icon" type="button">⌕</button><UserAvatar user={user} small /></div>
        </header>
        <main className="page-content">
          {activeId === "dashboard" ? <WorkspaceHome user={user} /> : activeId === "users" ? <UserManagement currentUser={user} /> : activeId === "roles" ? <RoleManagement /> : activeId === "permissions" ? <PermissionManagement /> : <PlaceholderPage item={activeItem} />}
        </main>
      </div>
    </div>
  );
}

function App() {
  const [user, setUser] = React.useState(null);
  const [loading, setLoading] = React.useState(true);

  React.useEffect(() => {
    if (!localStorage.getItem(TOKEN_KEY)) {
      setLoading(false);
      return;
    }
    request("/api/auth/me")
      .then(setUser)
      .catch(() => localStorage.removeItem(TOKEN_KEY))
      .finally(() => setLoading(false));
  }, []);

  async function logout() {
    try {
      await request("/api/auth/logout", { method: "POST" });
    } catch {
      // 即使网络中断，也清理当前浏览器中的令牌。
    }
    localStorage.removeItem(TOKEN_KEY);
    setUser(null);
  }

  if (loading) return <main className="container"><section className="card loading">正在检查登录状态…</section></main>;
  return user ? <Dashboard user={user} onLogout={logout} /> : <main className="container"><AuthForm onAuthenticated={setUser} /></main>;
}

createRoot(document.getElementById("root")).render(<React.StrictMode><App /></React.StrictMode>);
