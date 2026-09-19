import React from "react";
import { TOKEN_KEY, request } from "./api.js";
import { AuthForm } from "./components/auth-form.jsx";
import { Sidebar, findNavigationItem } from "./components/sidebar.jsx";
import { UserAvatar } from "./components/avatar.jsx";
import { WorkspaceHome } from "./pages/workspace.jsx";
import { PlaceholderPage } from "./pages/placeholder.jsx";
import { UserManagement } from "./pages/users.jsx";
import { RoleManagement } from "./pages/roles.jsx";
import { PermissionManagement } from "./pages/permissions.jsx";
import { ProjectManagement } from "./pages/projects.jsx";
import { ProjectDetail } from "./pages/project-detail.jsx";

function Dashboard({ user, onLogout }) {
  const [activeId, setActiveId] = React.useState("dashboard");
  const [openProjectId, setOpenProjectId] = React.useState(null);
  const [mobileOpen, setMobileOpen] = React.useState(false);
  const activeItem = findNavigationItem(activeId);

  function navigate(id) {
    setActiveId(id);
    setOpenProjectId(null);
  }

  function renderPage() {
    if (activeId === "projects") {
      return openProjectId
        ? <ProjectDetail currentUser={user} onBack={() => setOpenProjectId(null)} projectId={openProjectId} />
        : <ProjectManagement currentUser={user} onOpenProject={setOpenProjectId} />;
    }
    if (activeId === "dashboard") return <WorkspaceHome user={user} />;
    if (activeId === "users") return <UserManagement currentUser={user} />;
    if (activeId === "roles") return <RoleManagement />;
    if (activeId === "permissions") return <PermissionManagement />;
    return <PlaceholderPage item={activeItem} />;
  }

  return (
    <div className="app-shell">
      <Sidebar
        user={user}
        activeId={activeId}
        onNavigate={navigate}
        onLogout={onLogout}
        mobileOpen={mobileOpen}
        onClose={() => setMobileOpen(false)}
      />
      <div className="app-main">
        <header className="topbar">
          <button aria-label="打开菜单" className="mobile-menu-button" onClick={() => setMobileOpen(true)} type="button">☰</button>
          <div className="breadcrumbs"><span>项目管理空间</span><b>/</b><strong>{activeItem.label}</strong></div>
          <div className="topbar-actions"><button aria-label="搜索" className="topbar-icon" type="button">⌕</button><UserAvatar user={user} small /></div>
        </header>
        <main className="page-content">
          {renderPage()}
        </main>
      </div>
    </div>
  );
}

function App() {
  const [user, setUser] = React.useState(null);
  const [loading, setLoading] = React.useState(true);

  React.useEffect(() => {
    if (!localStorage.getItem(TOKEN_KEY)) {
      setLoading(false);
      return;
    }
    request("/api/auth/me")
      .then(setUser)
      .catch(() => localStorage.removeItem(TOKEN_KEY))
      .finally(() => setLoading(false));
  }, []);

  async function logout() {
    try {
      await request("/api/auth/logout", { method: "POST" });
    } catch {
      // 即使网络中断，也清理当前浏览器中的令牌。
    }
    localStorage.removeItem(TOKEN_KEY);
    setUser(null);
  }

  if (loading) return <main className="container"><section className="card loading">正在检查登录状态…</section></main>;
  return user ? <Dashboard user={user} onLogout={logout} /> : <main className="container"><AuthForm onAuthenticated={setUser} /></main>;
}

export { App };
