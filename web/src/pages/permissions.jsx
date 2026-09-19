import React from "react";
import { request } from "../api.js";

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

export { PermissionManagement };
