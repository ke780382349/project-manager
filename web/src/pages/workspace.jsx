import React from "react";
import { request } from "../api.js";
import { PERMISSION_IDS } from "../permissions.js";

function WorkspaceHome({ user }) {
  const [projectCount, setProjectCount] = React.useState(null);

  React.useEffect(() => {
    if (!user.permissions?.includes(PERMISSION_IDS.PROJECT_VIEW)) return undefined;
    let cancelled = false;
    request("/api/projects")
      .then((projects) => { if (!cancelled) setProjectCount(projects.length); })
      .catch(() => { if (!cancelled) setProjectCount(null); });
    return () => { cancelled = true; };
  }, [user]);

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
        <div className="stat-card"><span className="stat-label">项目总数</span><strong>{projectCount === null ? "—" : projectCount}</strong><small>{projectCount === null ? "暂无可查看的项目" : "当前可查看的项目数量"}</small></div>
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

export { WorkspaceHome };
