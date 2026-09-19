import React from "react";
import { request } from "../api.js";
import { PERMISSION_IDS } from "../permissions.js";
import { OptionDropdown } from "../components/dropdown.jsx";

const SEVERITY_LABELS = { LOW: "低", MEDIUM: "中", HIGH: "高" };
const STATUS_LABELS = { OPEN: "待修复", FIXED: "已修复", CLOSED: "已关闭" };
const SEVERITY_OPTIONS = Object.keys(SEVERITY_LABELS).map((value) => ({ value, label: SEVERITY_LABELS[value] }));
const STATUS_OPTIONS = Object.keys(STATUS_LABELS).map((value) => ({ value, label: STATUS_LABELS[value] }));

function ProjectBugs({ projectId, currentUser }) {
  const [bugs, setBugs] = React.useState([]);
  const [loading, setLoading] = React.useState(true);
  const [submitting, setSubmitting] = React.useState(false);
  const [message, setMessage] = React.useState("");
  const [editingBug, setEditingBug] = React.useState(null);
  const [showModal, setShowModal] = React.useState(false);
  const [form, setForm] = React.useState({ title: "", description: "", severity: "MEDIUM", status: "OPEN" });
  const canManage = currentUser.permissions?.includes(PERMISSION_IDS.BUG_MANAGE);

  async function loadBugs() {
    try {
      setBugs(await request(`/api/bugs?projectId=${encodeURIComponent(projectId)}`));
    } catch (error) {
      setMessage(error.message);
    } finally {
      setLoading(false);
    }
  }

  React.useEffect(() => {
    setLoading(true);
    setMessage("");
    loadBugs();
  }, [projectId]);

  function updateField(event) {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  }

  function startCreate() {
    setEditingBug(null);
    setForm({ title: "", description: "", severity: "MEDIUM", status: "OPEN" });
    setMessage("");
    setShowModal(true);
  }

  function startEdit(bug) {
    setEditingBug(bug);
    setForm({ title: bug.title, description: bug.description || "", severity: bug.severity, status: bug.status });
    setMessage("");
    setShowModal(true);
  }

  async function saveBug(event) {
    event.preventDefault();
    const wasEditing = Boolean(editingBug);
    setSubmitting(true);
    setMessage("");
    try {
      const payload = wasEditing
        ? { title: form.title, description: form.description, severity: form.severity, status: form.status }
        : { projectId, title: form.title, description: form.description, severity: form.severity };
      await request(wasEditing ? `/api/bugs/${editingBug.id}` : "/api/bugs", {
        method: wasEditing ? "PATCH" : "POST",
        body: JSON.stringify(payload),
      });
      await loadBugs();
      setEditingBug(null);
      setShowModal(false);
      setMessage(wasEditing ? "Bug 已更新" : "Bug 已提交");
    } catch (error) {
      setMessage(error.message);
    } finally {
      setSubmitting(false);
    }
  }

  async function removeBug(bug) {
    if (!window.confirm(`确定删除 Bug“${bug.title}”吗？删除后不可恢复。`)) return;
    try {
      await request(`/api/bugs/${bug.id}`, { method: "DELETE" });
      await loadBugs();
      setMessage("Bug 已删除");
    } catch (error) {
      setMessage(error.message);
    }
  }

  return (
    <>
      <section className="content-card project-list-card">
        <div className="card-heading">
          <div><h2>Bug 列表</h2><p>共 {bugs.length} 个 Bug，按提交时间排序。</p></div>
          <span className="list-message">{message}</span>
        </div>
        <div className="requirement-toolbar">
          <button className="primary action-button" onClick={startCreate} type="button">+ 提交 Bug</button>
        </div>
        {loading ? <div className="table-empty">正在加载 Bug…</div> : bugs.length === 0 ? (
          <div className="empty-panel"><span className="empty-icon">⚠</span><strong>还没有 Bug</strong><span>点击「提交 Bug」记录当前项目发现的问题。</span></div>
        ) : (
          <div className="project-table-wrap"><table className="project-table"><thead><tr><th>Bug</th><th>描述</th><th>严重程度</th><th>状态</th><th>报告人</th>{canManage && <th>操作</th>}</tr></thead><tbody>
            {bugs.map((item) => <tr key={item.id}>
              <td className="project-name-cell"><strong>{item.title}</strong></td>
              <td className="project-description-cell">{item.description || "暂无描述"}</td>
              <td><span className={`status-pill bug-severity-${item.severity.toLowerCase()}`}>{SEVERITY_LABELS[item.severity] || item.severity}</span></td>
              <td><span className={`status-pill bug-status-${item.status.toLowerCase()}`}>{STATUS_LABELS[item.status] || item.status}</span></td>
              <td>{item.reporterName}</td>
              {canManage && <td><div className="table-actions"><button className="link-button" onClick={() => startEdit(item)} type="button">编辑</button><button className="danger-link" onClick={() => removeBug(item)} type="button">删除</button></div></td>}
            </tr>)}
          </tbody></table></div>
        )}
      </section>
      {showModal && <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) setShowModal(false); }}>
        <section aria-labelledby="bug-modal-title" aria-modal="true" className="modal-card role-modal-card" role="dialog">
          <div className="modal-heading"><div><p className="eyebrow">BUG</p><h2 id="bug-modal-title">{editingBug ? "编辑 Bug" : "提交 Bug"}</h2><p>{editingBug ? "调整 Bug 内容、严重程度和处理状态。" : "Bug 会绑定到当前项目，报告人自动记录。"}</p></div><button aria-label="关闭弹窗" className="modal-close" onClick={() => setShowModal(false)} type="button">×</button></div>
          <form className="role-form modal-form" onSubmit={saveBug}>
            <label>Bug 标题<input autoFocus maxLength="80" name="title" onChange={updateField} placeholder="例如 导出报表缺少成员列" required value={form.title} /></label>
            <label>Bug 描述<textarea maxLength="500" name="description" onChange={updateField} placeholder="复现步骤和期望行为" rows="3" value={form.description} /></label>
            <label>严重程度<OptionDropdown onChange={(severity) => setForm((current) => ({ ...current, severity }))} options={SEVERITY_OPTIONS} value={form.severity} /></label>
            {editingBug && <label>处理状态<OptionDropdown onChange={(status) => setForm((current) => ({ ...current, status }))} options={STATUS_OPTIONS} value={form.status} /></label>}
            <div className="modal-actions"><button className="secondary" onClick={() => setShowModal(false)} type="button">取消</button><button className="primary" disabled={submitting} type="submit">{submitting ? "保存中…" : editingBug ? "保存修改" : "提交 Bug"}</button></div>
          </form>
          <p className="message modal-message">{message}</p>
        </section>
      </div>}
    </>
  );
}

export { ProjectBugs };
