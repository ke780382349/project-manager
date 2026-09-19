import React from "react";
import { request } from "../api.js";
import { PERMISSION_IDS } from "../permissions.js";
import { ProjectMembers } from "./project-members.jsx";
import { ProjectRequirements } from "./project-requirements.jsx";
import { ProjectTasks } from "./project-tasks.jsx";
import { ProjectBugs } from "./project-bugs.jsx";

function ProjectSpace({ projectId, currentUser, onBack }) {
  const [project, setProject] = React.useState(null);
  const [loading, setLoading] = React.useState(true);
  const [message, setMessage] = React.useState("");
  const [tab, setTab] = React.useState("");

  const tabs = [
    { id: "requirements", label: "需求池", show: currentUser.permissions?.includes(PERMISSION_IDS.REQUIREMENT_VIEW) },
    { id: "tasks", label: "任务", show: currentUser.permissions?.includes(PERMISSION_IDS.TASK_VIEW) },
    { id: "bugs", label: "Bug", show: currentUser.permissions?.includes(PERMISSION_IDS.BUG_VIEW) },
    { id: "members", label: "成员", show: true },
  ].filter((item) => item.show);

  React.useEffect(() => {
    setLoading(true);
    setMessage("");
    setTab("");
    request(`/api/projects/${projectId}`)
      .then((detail) => {
        setProject(detail);
        setTab(tabs[0]?.id || "members");
      })
      .catch((error) => setMessage(error.message))
      .finally(() => setLoading(false));
  }, [projectId]);

  if (loading) {
    return <section className="content-card project-list-card"><div className="table-empty">正在加载项目空间…</div></section>;
  }

  if (!project) {
    return (
      <>
        <div className="page-heading">
          <div><p className="eyebrow">PROJECT SPACE</p><h1>项目空间</h1><p className="muted">无法加载该项目。</p></div>
        </div>
        <section className="content-card project-list-card">
          <div className="empty-panel"><span className="empty-icon">▣</span><strong>{message || "项目不存在"}</strong><span>返回项目列表后重新选择。</span></div>
          <div className="project-detail-actions"><button className="secondary project-back-button" onClick={onBack} type="button">返回项目列表</button></div>
        </section>
      </>
    );
  }

  return (
    <>
      <div className="page-heading">
        <div>
          <button className="project-back link-button" onClick={onBack} type="button">← 返回项目列表</button>
          <p className="eyebrow">PROJECT SPACE</p>
          <h1>{project.name}</h1>
          <p className="muted">{project.description || "暂无描述"}</p>
        </div>
        <span className="table-hint">负责人：{project.owner?.displayName || "—"}</span>
      </div>
      <div className="tabs project-tabs" role="tablist">
        {tabs.map((item) => <button
          aria-selected={tab === item.id}
          className={`tab ${tab === item.id ? "active" : ""}`}
          key={item.id}
          onClick={() => setTab(item.id)}
          role="tab"
          type="button"
        >{item.label}</button>)}
      </div>
      {tab === "requirements" && <ProjectRequirements currentUser={currentUser} projectId={projectId} />}
      {tab === "tasks" && <ProjectTasks currentUser={currentUser} projectId={projectId} />}
      {tab === "bugs" && <ProjectBugs currentUser={currentUser} projectId={projectId} />}
      {tab === "members" && <ProjectMembers currentUser={currentUser} projectId={projectId} />}
    </>
  );
}

export { ProjectSpace };
