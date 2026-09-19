import React from "react";
import { request } from "../api.js";
import { PERMISSION_IDS } from "../permissions.js";
import { OptionDropdown } from "../components/dropdown.jsx";

const STATUS_LABELS = { PENDING: "待评审", ACCEPTED: "已接受", REJECTED: "已拒绝" };
const STATUS_OPTIONS = Object.keys(STATUS_LABELS).map((value) => ({ value, label: STATUS_LABELS[value] }));

function ProjectRequirements({ projectId, currentUser }) {
  const [requirements, setRequirements] = React.useState([]);
  const [loading, setLoading] = React.useState(true);
  const [submitting, setSubmitting] = React.useState(false);
  const [message, setMessage] = React.useState("");
  const [editingRequirement, setEditingRequirement] = React.useState(null);
  const [showModal, setShowModal] = React.useState(false);
  const [form, setForm] = React.useState({ title: "", description: "", status: "PENDING" });
  const canManage = currentUser.permissions?.includes(PERMISSION_IDS.REQUIREMENT_MANAGE);

  async function loadRequirements() {
    try {
      setRequirements(await request(`/api/requirements?projectId=${encodeURIComponent(projectId)}`));
    } catch (error) {
      setMessage(error.message);
    } finally {
      setLoading(false);
    }
  }

  React.useEffect(() => {
    setLoading(true);
    setMessage("");
    loadRequirements();
  }, [projectId]);

  function updateField(event) {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  }

  function startCreate() {
    setEditingRequirement(null);
    setForm({ title: "", description: "", status: "PENDING" });
    setMessage("");
    setShowModal(true);
  }

  function startEdit(requirement) {
    setEditingRequirement(requirement);
    setForm({ title: requirement.title, description: requirement.description || "", status: requirement.status });
    setMessage("");
    setShowModal(true);
  }

  async function saveRequirement(event) {
    event.preventDefault();
    const wasEditing = Boolean(editingRequirement);
    setSubmitting(true);
    setMessage("");
    try {
      const payload = wasEditing
        ? { title: form.title, description: form.description, status: form.status }
        : { projectId, title: form.title, description: form.description };
      await request(wasEditing ? `/api/requirements/${editingRequirement.id}` : "/api/requirements", {
        method: wasEditing ? "PATCH" : "POST",
        body: JSON.stringify(payload),
      });
      await loadRequirements();
      setEditingRequirement(null);
      setShowModal(false);
      setMessage(wasEditing ? "需求已更新" : "需求已提交");
    } catch (error) {
      setMessage(error.message);
    } finally {
      setSubmitting(false);
    }
  }

  async function removeRequirement(requirement) {
    if (!window.confirm(`确定删除需求“${requirement.title}”吗？删除后不可恢复。`)) return;
    try {
      await request(`/api/requirements/${requirement.id}`, { method: "DELETE" });
      await loadRequirements();
      setMessage("需求已删除");
    } catch (error) {
      setMessage(error.message);
    }
  }

  return (
    <>
      <section className="content-card project-list-card">
        <div className="card-heading">
          <div><h2>需求池</h2><p>共 {requirements.length} 条需求，按提交时间排序。</p></div>
          <span className="list-message">{message}</span>
        </div>
        <div className="requirement-toolbar">
          <button className="primary action-button" onClick={startCreate} type="button">+ 新建需求</button>
        </div>
        {loading ? <div className="table-empty">正在加载需求…</div> : requirements.length === 0 ? (
          <div className="empty-panel"><span className="empty-icon">◇</span><strong>还没有需求</strong><span>点击「新建需求」提交第一条绑定到该项目的需求。</span></div>
        ) : (
          <div className="project-table-wrap"><table className="project-table"><thead><tr><th>需求</th><th>描述</th><th>状态</th><th>提出人</th>{canManage && <th>操作</th>}</tr></thead><tbody>
            {requirements.map((item) => <tr key={item.id}>
              <td className="project-name-cell"><strong>{item.title}</strong></td>
              <td className="project-description-cell">{item.description || "暂无描述"}</td>
              <td><span className={`status-pill requirement-status-${item.status.toLowerCase()}`}>{STATUS_LABELS[item.status] || item.status}</span></td>
              <td>{item.creatorName}</td>
              {canManage && <td><div className="table-actions"><button className="link-button" onClick={() => startEdit(item)} type="button">编辑</button><button className="danger-link" onClick={() => removeRequirement(item)} type="button">删除</button></div></td>}
            </tr>)}
          </tbody></table></div>
        )}
      </section>
      {showModal && <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) setShowModal(false); }}>
        <section aria-labelledby="requirement-modal-title" aria-modal="true" className="modal-card role-modal-card" role="dialog">
          <div className="modal-heading"><div><p className="eyebrow">REQUIREMENT</p><h2 id="requirement-modal-title">{editingRequirement ? "编辑需求" : "新建需求"}</h2><p>{editingRequirement ? "调整需求内容或评审状态。" : "需求会绑定到当前项目。"}</p></div><button aria-label="关闭弹窗" className="modal-close" onClick={() => setShowModal(false)} type="button">×</button></div>
          <form className="role-form modal-form" onSubmit={saveRequirement}>
            <label>需求标题<input autoFocus maxLength="80" name="title" onChange={updateField} placeholder="例如 支持导出项目报表" required value={form.title} /></label>
            <label>需求描述<textarea maxLength="500" name="description" onChange={updateField} placeholder="说明需求背景与验收标准" rows="3" value={form.description} /></label>
            {editingRequirement && <label>评审状态<OptionDropdown onChange={(status) => setForm((current) => ({ ...current, status }))} options={STATUS_OPTIONS} value={form.status} /></label>}
            <div className="modal-actions"><button className="secondary" onClick={() => setShowModal(false)} type="button">取消</button><button className="primary" disabled={submitting} type="submit">{submitting ? "保存中…" : editingRequirement ? "保存修改" : "提交需求"}</button></div>
          </form>
          <p className="message modal-message">{message}</p>
        </section>
      </div>}
    </>
  );
}

export { ProjectRequirements };
