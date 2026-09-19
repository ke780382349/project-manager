import React from "react";
import { request } from "../api.js";

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

export { RoleManagement };
