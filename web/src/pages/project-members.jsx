import React from "react";
import { request } from "../api.js";
import { PERMISSION_IDS } from "../permissions.js";
import { OptionDropdown } from "../components/dropdown.jsx";

function sortMembers(members) {
  return [...members].sort((first, second) => Number(second.owner) - Number(first.owner)
    || first.displayName.localeCompare(second.displayName, "zh-Hans-CN"));
}

function ProjectMembers({ projectId, currentUser }) {
  const [project, setProject] = React.useState(null);
  const [userOptions, setUserOptions] = React.useState([]);
  const [selectedUserId, setSelectedUserId] = React.useState("");
  const [loading, setLoading] = React.useState(true);
  const [submitting, setSubmitting] = React.useState(false);
  const [message, setMessage] = React.useState("");
  const canManage = currentUser.permissions?.includes(PERMISSION_IDS.PROJECT_MANAGE);

  async function loadMembers() {
    try {
      const [detail, options] = await Promise.all([
        request(`/api/projects/${projectId}`),
        canManage ? request("/api/users/options") : Promise.resolve([]),
      ]);
      setProject(detail);
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
    setSelectedUserId("");
    loadMembers();
  }, [projectId]);

  async function addMember() {
    if (!selectedUserId) return;
    setSubmitting(true);
    setMessage("");
    try {
      const updated = await request(`/api/projects/${projectId}/members`, {
        method: "POST",
        body: JSON.stringify({ userId: selectedUserId }),
      });
      setProject(updated);
      setSelectedUserId("");
      setMessage("成员已添加");
    } catch (error) {
      setMessage(error.message);
    } finally {
      setSubmitting(false);
    }
  }

  async function removeMember(member) {
    if (!window.confirm(`确定将“${member.displayName}”移出项目吗？`)) return;
    setSubmitting(true);
    setMessage("");
    try {
      const updated = await request(`/api/projects/${projectId}/members/${member.id}`, { method: "DELETE" });
      setProject(updated);
      setMessage("成员已移除");
    } catch (error) {
      setMessage(error.message);
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) {
    return <section className="content-card project-list-card"><div className="table-empty">正在加载成员…</div></section>;
  }

  const members = sortMembers(project?.members || []);
  const memberIds = new Set(members.map((member) => member.id));
  const candidates = userOptions.filter((option) => !memberIds.has(option.id));

  return (
    <section className="content-card project-list-card">
      <div className="card-heading">
        <div><h2>项目成员</h2><p>共 {members.length} 名成员，成员为系统用户，负责人始终保留。</p></div>
        <span className="list-message">{message}</span>
      </div>
      {canManage && <div className="project-add-member">
        <OptionDropdown
          disabled={submitting || candidates.length === 0}
          onChange={setSelectedUserId}
          options={candidates.map((option) => ({ value: option.id, label: option.displayName, secondary: option.username || option.email }))}
          placeholder={candidates.length === 0 ? "没有可添加的用户" : "选择要添加的用户"}
          value={selectedUserId}
        />
        <button className="primary" disabled={submitting || !selectedUserId} onClick={addMember} type="button">{submitting ? "处理中…" : "添加成员"}</button>
      </div>}
      <div className="project-table-wrap"><table className="project-table">
        <thead><tr><th>成员</th><th>账号</th><th>邮箱</th><th>身份</th>{canManage && <th>操作</th>}</tr></thead>
        <tbody>
          {members.map((member) => <tr key={member.id}>
            <td><div className="project-member-cell"><span className="project-member-avatar">{member.displayName.slice(0, 1).toUpperCase()}</span><strong>{member.displayName}</strong></div></td>
            <td>{member.username || "—"}</td>
            <td className="project-description-cell">{member.email || "—"}</td>
            <td><span className={`role-type-pill ${member.owner ? "role-type-built-in" : "role-type-custom"}`}>{member.owner ? "负责人" : "成员"}</span></td>
            {canManage && <td>{member.owner ? <span className="table-hint">不可移除</span> : <button className="danger-link" disabled={submitting} onClick={() => removeMember(member)} type="button">移除</button>}</td>}
          </tr>)}
        </tbody>
      </table></div>
      {canManage && candidates.length === 0 && <p className="table-hint project-add-hint">所有启用的用户都已在该项目中。</p>}
    </section>
  );
}

export { ProjectMembers };
