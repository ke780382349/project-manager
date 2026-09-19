import React from "react";
import { request } from "../api.js";
import { UserAvatar } from "../components/avatar.jsx";
import { RoleDropdown } from "../components/dropdown.jsx";

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

export { UserManagement };
