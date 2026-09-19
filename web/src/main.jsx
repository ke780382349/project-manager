import React from "react";
import { createRoot } from "react-dom/client";
import "./styles.css";

const TOKEN_KEY = "project_manager_token";
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "";

async function request(path, options = {}) {
  const token = localStorage.getItem(TOKEN_KEY);
  const headers = { ...(options.body ? { "Content-Type": "application/json" } : {}), ...options.headers };
  if (token) headers.Authorization = `Bearer ${token}`;

  const response = await fetch(`${API_BASE_URL}${path}`, { ...options, headers });
  const text = await response.text();
  let data = {};
  try {
    data = text ? JSON.parse(text) : {};
  } catch {
    data = { message: text };
  }
  if (!response.ok) {
    throw new Error(data.detail || data.message || data.error || "请求失败");
  }
  return data;
}

function AuthForm({ onAuthenticated }) {
  const [registerMode, setRegisterMode] = React.useState(false);
  const [account, setAccount] = React.useState("");
  const [password, setPassword] = React.useState("");
  const [displayName, setDisplayName] = React.useState("");
  const [message, setMessage] = React.useState("");
  const [submitting, setSubmitting] = React.useState(false);

  async function submit(event) {
    event.preventDefault();
    setMessage("");
    setSubmitting(true);
    try {
      const payload = registerMode
        ? { email: account.trim(), password, displayName: displayName.trim() }
        : { account: account.trim(), password };
      const data = await request(`/api/auth/${registerMode ? "register" : "login"}`, {
        method: "POST",
        body: JSON.stringify(payload),
      });
      localStorage.setItem(TOKEN_KEY, data.token);
      onAuthenticated(data.user);
    } catch (error) {
      setMessage(error.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="auth-card card">
      <p className="eyebrow">PROJECT MANAGER</p>
      <h1>统一登录</h1>
      <p className="muted">登录一次，后续项目、需求和 Bug 模块共用同一身份。</p>
      <div className="tabs" role="tablist">
        <button className={`tab ${!registerMode ? "active" : ""}`} onClick={() => { setRegisterMode(false); setMessage(""); }} type="button">登录</button>
        <button className={`tab ${registerMode ? "active" : ""}`} onClick={() => { setRegisterMode(true); setMessage(""); }} type="button">注册</button>
      </div>
      <form onSubmit={submit}>
        {registerMode && (
          <label>显示名称
            <input value={displayName} onChange={(event) => setDisplayName(event.target.value)} maxLength="80" required autoComplete="name" />
          </label>
        )}
        <label>邮箱
          <input type={registerMode ? "email" : "text"} value={account} onChange={(event) => setAccount(event.target.value)} required autoComplete={registerMode ? "email" : "username"} placeholder={registerMode ? "name@example.com" : "邮箱或账号，例如 admin"} />
        </label>
        <label>密码
          <input type="password" value={password} onChange={(event) => setPassword(event.target.value)} required minLength={registerMode ? 8 : undefined} autoComplete={registerMode ? "new-password" : "current-password"} placeholder={registerMode ? "至少 8 位" : "请输入密码"} />
        </label>
        <button className="primary" disabled={submitting} type="submit">{submitting ? "处理中…" : registerMode ? "注册并进入系统" : "登录"}</button>
      </form>
      <p className="message">{message}</p>
    </section>
  );
}

function Dashboard({ user, onLogout }) {
  return (
    <section className="dashboard-card card">
      <div className="dashboard-header">
        <div>
          <p className="eyebrow">WORKSPACE</p>
          <h1>欢迎回来，{user.displayName}</h1>
          <p className="muted">{user.email} · {user.role}</p>
        </div>
        <button className="secondary" onClick={onLogout} type="button">退出登录</button>
      </div>
      <div className="empty-state">
        <h2>用户模块已就绪</h2>
        <p>项目、需求、Bug 和任务模块将在后续加入。</p>
      </div>
    </section>
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
  return <main className="container">{user ? <Dashboard user={user} onLogout={logout} /> : <AuthForm onAuthenticated={setUser} />}</main>;
}

createRoot(document.getElementById("root")).render(<React.StrictMode><App /></React.StrictMode>);
