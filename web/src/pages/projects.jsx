import React from "react";
import { request } from "../api.js";
import { PERMISSION_IDS } from "../permissions.js";

function ProjectManagement({ currentUser, onOpenProject }) {
  const [projects, setProjects] = React.useState([]);
  const [loading, setLoading] = React.useState(true);
  const [message, setMessage] = React.useState("");
  const [submitting, setSubmitting] = React.useState(false);
  const [editingProject, setEditingProject] = React.useState(null);
  const [showModal, setShowModal] = React.useState(false);
  const [form, setForm] = React.useState({ name: "", description: "" });
  const canManage = currentUser.permissions?.includes(PERMISSION_IDS.PROJECT_MANAGE);

  async function loadProjects() {
    try {
      setProjects(await request("/api/projects"));
    } catch (error) {
      setMessage(error.message);
    } finally {
      setLoading(false);
    }
  }

  React.useEffect(() => { loadProjects(); }, []);

  function updateField(event) {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  }

  function startCreate() {
    setEditingProject(null);
    setForm({ name: "", description: "" });
    setMessage("");
    setShowModal(true);
  }

  function startEdit(project) {
    setEditingProject(project);
    setForm({ name: project.name, description: project.description || "" });
    setMessage("");
    setShowModal(true);
  }

  async function saveProject(event) {
    event.preventDefault();
    const wasEditing = Boolean(editingProject);
    setSubmitting(true);
    setMessage("");
    try {
      const payload = { name: form.name, description: form.description };
      if (wasEditing) {
        await request(`/api/projects/${editingProject.id}`, { method: "PATCH", body: JSON.stringify(payload) });
      } else {
        await request("/api/projects", { method: "POST", body: JSON.stringify(payload) });
      }
      await loadProjects();
      setEditingProject(null);
      setShowModal(false);
      setMessage(wasEditing ? "项目已更新" : "项目已创建");
    } catch (error) {
      setMessage(error.message);
    } finally {
      setSubmitting(false);
    }
  }

  async function removeProject(project) {
    if (!window.confirm(`确定删除项目“${project.name}”吗？删除后不可恢复。`)) return;
    try {
      await request(`/api/projects/${project.id}`, { method: "DELETE" });
      await loadProjects();
      setMessage("项目已删除");
    } catch (error) {
      setMessage(error.message);
    }
  }

  return (
    <>
      <div className="page-heading">
        <div><p className="eyebrow">DELIVERY</p><h1>项目</h1><p className="muted">管理项目目标和参与成员。</p></div>
        {canManage && <button className="primary action-button" onClick={startCreate} type="button">+ 新建项目</button>}
      </div>
      <section className="content-card project-list-card">
        <div className="card-heading"><div><h2>项目列表</h2><p>共 {projects.length} 个项目，按最近更新排序。</p></div><span className="list-message">{message}</span></div>
        {loading ? <div className="table-empty">正在加载项目…</div> : projects.length === 0 ? (
          <div className="empty-panel"><span className="empty-icon">▣</span><strong>还没有项目</strong><span>{canManage ? "点击「新建项目」开始规划第一个项目。" : "你参与的项目会显示在这里。"}</span></div>
        ) : (
          <div className="project-table-wrap"><table className="project-table"><thead><tr><th>项目</th><th>描述</th><th>负责人</th><th>成员</th><th>操作</th></tr></thead><tbody>
            {projects.map((project) => <tr key={project.id}>
              <td className="project-name-cell"><button className="project-name-link" onClick={() => onOpenProject(project.id)} type="button"><strong>{project.name}</strong></button></td>
              <td className="project-description-cell">{project.description || "暂无描述"}</td>
              <td>{project.owner?.displayName || "—"}</td>
              <td><div className="project-member-list">{project.members.slice(0, 4).map((member) => <span key={member.id} title={member.displayName}>{member.displayName}</span>)}{project.members.length > 4 && <span className="table-hint">+{project.members.length - 4}</span>}</div></td>
              <td><div className="table-actions"><button className="link-button" onClick={() => onOpenProject(project.id)} type="button">成员</button>{canManage && <button className="link-button" onClick={() => startEdit(project)} type="button">编辑</button>}{canManage && <button className="danger-link" onClick={() => removeProject(project)} type="button">删除</button>}</div></td>
            </tr>)}
          </tbody></table></div>
        )}
      </section>
      {showModal && <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) setShowModal(false); }}>
        <section aria-labelledby="project-modal-title" aria-modal="true" className="modal-card role-modal-card" role="dialog">
          <div className="modal-heading"><div><p className="eyebrow">PROJECT</p><h2 id="project-modal-title">{editingProject ? "编辑项目" : "新建项目"}</h2><p>{editingProject ? "调整项目名称和描述，成员在项目详情页维护。" : "系统会自动生成唯一项目 ID，创建人默认成为负责人。"}</p></div><button aria-label="关闭弹窗" className="modal-close" onClick={() => setShowModal(false)} type="button">×</button></div>
          <form className="role-form modal-form" onSubmit={saveProject}>
            <label>项目名称<input name="name" value={form.name} onChange={updateField} maxLength="80" required autoFocus placeholder="例如 官网改版" /></label>
            <label>项目描述<textarea name="description" value={form.description} onChange={updateField} maxLength="500" rows="3" placeholder="说明项目目标和范围" /></label>
            <div className="modal-actions"><button className="secondary" onClick={() => setShowModal(false)} type="button">取消</button><button className="primary" disabled={submitting} type="submit">{submitting ? "保存中…" : editingProject ? "保存修改" : "创建项目"}</button></div>
          </form>
          <p className="message modal-message">{message}</p>
        </section>
      </div>}
    </>
  );
}

export { ProjectManagement };
