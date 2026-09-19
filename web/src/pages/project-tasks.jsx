import React from "react";
import { request } from "../api.js";
import { PERMISSION_IDS } from "../permissions.js";
import { OptionDropdown } from "../components/dropdown.jsx";

const STATUS_LABELS = { TODO: "未开始", DOING: "进行中", DONE: "已完成" };
const STATUS_OPTIONS = Object.keys(STATUS_LABELS).map((value) => ({ value, label: STATUS_LABELS[value] }));

function ProjectTasks({ projectId, currentUser }) {
  const [tasks, setTasks] = React.useState([]);
  const [userOptions, setUserOptions] = React.useState([]);
  const [loading, setLoading] = React.useState(true);
  const [submitting, setSubmitting] = React.useState(false);
  const [message, setMessage] = React.useState("");
  const [editingTask, setEditingTask] = React.useState(null);
  const [showModal, setShowModal] = React.useState(false);
  const [form, setForm] = React.useState({ title: "", description: "", status: "TODO", assigneeId: "" });
  const canManage = currentUser.permissions?.includes(PERMISSION_IDS.TASK_MANAGE);

  async function loadTasks() {
    try {
      const [taskList, options] = await Promise.all([
        request(`/api/tasks?projectId=${encodeURIComponent(projectId)}`),
        request("/api/users/options").catch(() => []),
      ]);
      setTasks(taskList);
      setUserOptions(options);
    } catch (error) {
      setMessage(error.message);
    } finally {
      setLoading(false);
    }
  }

  React.useEffect(() => {
    setLoading(true);
    setMessage("");
    loadTasks();
  }, [projectId]);

  function updateField(event) {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  }

  function startCreate() {
    setEditingTask(null);
    setForm({ title: "", description: "", status: "TODO", assigneeId: "" });
    setMessage("");
    setShowModal(true);
  }

  function startEdit(task) {
    setEditingTask(task);
    setForm({ title: task.title, description: task.description || "", status: task.status, assigneeId: task.assigneeId || "" });
    setMessage("");
    setShowModal(true);
  }

  async function saveTask(event) {
    event.preventDefault();
    const wasEditing = Boolean(editingTask);
    setSubmitting(true);
    setMessage("");
    try {
      const payload = wasEditing
        ? { title: form.title, description: form.description, status: form.status, assigneeId: form.assigneeId || null }
        : { projectId, title: form.title, description: form.description, assigneeId: form.assigneeId || null };
      await request(wasEditing ? `/api/tasks/${editingTask.id}` : "/api/tasks", {
        method: wasEditing ? "PATCH" : "POST",
        body: JSON.stringify(payload),
      });
      await loadTasks();
      setEditingTask(null);
      setShowModal(false);
      setMessage(wasEditing ? "任务已更新" : "任务已创建");
    } catch (error) {
      setMessage(error.message);
    } finally {
      setSubmitting(false);
    }
  }

  async function removeTask(task) {
    if (!window.confirm(`确定删除任务“${task.title}”吗？删除后不可恢复。`)) return;
    try {
      await request(`/api/tasks/${task.id}`, { method: "DELETE" });
      await loadTasks();
      setMessage("任务已删除");
    } catch (error) {
      setMessage(error.message);
    }
  }

  const assigneeOptions = [{ value: "", label: "不指定负责人" }, ...userOptions.map((option) => ({ value: option.id, label: option.displayName }))];

  return (
    <>
      <section className="content-card project-list-card">
        <div className="card-heading">
          <div><h2>研发任务</h2><p>共 {tasks.length} 个任务，按创建时间排序。</p></div>
          <span className="list-message">{message}</span>
        </div>
        <div className="requirement-toolbar">
          <button className="primary action-button" onClick={startCreate} type="button">+ 新建任务</button>
        </div>
        {loading ? <div className="table-empty">正在加载任务…</div> : tasks.length === 0 ? (
          <div className="empty-panel"><span className="empty-icon">✓</span><strong>还没有任务</strong><span>点击「新建任务」把需求拆成可执行的研发任务。</span></div>
        ) : (
          <div className="project-table-wrap"><table className="project-table"><thead><tr><th>任务</th><th>描述</th><th>状态</th><th>负责人</th>{canManage && <th>操作</th>}</tr></thead><tbody>
            {tasks.map((item) => <tr key={item.id}>
              <td className="project-name-cell"><strong>{item.title}</strong></td>
              <td className="project-description-cell">{item.description || "暂无描述"}</td>
              <td><span className={`status-pill task-status-${item.status.toLowerCase()}`}>{STATUS_LABELS[item.status] || item.status}</span></td>
              <td>{item.assigneeName || <span className="table-hint">未指定</span>}</td>
              {canManage && <td><div className="table-actions"><button className="link-button" onClick={() => startEdit(item)} type="button">编辑</button><button className="danger-link" onClick={() => removeTask(item)} type="button">删除</button></div></td>}
            </tr>)}
          </tbody></table></div>
        )}
      </section>
      {showModal && <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) setShowModal(false); }}>
        <section aria-labelledby="task-modal-title" aria-modal="true" className="modal-card role-modal-card" role="dialog">
          <div className="modal-heading"><div><p className="eyebrow">TASK</p><h2 id="task-modal-title">{editingTask ? "编辑任务" : "新建任务"}</h2><p>{editingTask ? "调整任务内容、状态和负责人。" : "任务会绑定到当前项目。"}</p></div><button aria-label="关闭弹窗" className="modal-close" onClick={() => setShowModal(false)} type="button">×</button></div>
          <form className="role-form modal-form" onSubmit={saveTask}>
            <label>任务标题<input autoFocus maxLength="80" name="title" onChange={updateField} placeholder="例如 完成报表导出接口" required value={form.title} /></label>
            <label>任务描述<textarea maxLength="500" name="description" onChange={updateField} placeholder="说明实现范围和验收方式" rows="3" value={form.description} /></label>
            <label>负责人<OptionDropdown onChange={(assigneeId) => setForm((current) => ({ ...current, assigneeId }))} options={assigneeOptions} placeholder="不指定负责人" value={form.assigneeId} /></label>
            {editingTask && <label>任务状态<OptionDropdown onChange={(status) => setForm((current) => ({ ...current, status }))} options={STATUS_OPTIONS} value={form.status} /></label>}
            <div className="modal-actions"><button className="secondary" onClick={() => setShowModal(false)} type="button">取消</button><button className="primary" disabled={submitting} type="submit">{submitting ? "保存中…" : editingTask ? "保存修改" : "创建任务"}</button></div>
          </form>
          <p className="message modal-message">{message}</p>
        </section>
      </div>}
    </>
  );
}

export { ProjectTasks };
