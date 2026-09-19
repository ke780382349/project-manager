import React from "react";
import { TOKEN_KEY, request } from "../api.js";

function AuthForm({ onAuthenticated }) {
  const [account, setAccount] = React.useState("");
  const [password, setPassword] = React.useState("");
  const [message, setMessage] = React.useState("");
  const [submitting, setSubmitting] = React.useState(false);

  async function submit(event) {
    event.preventDefault();
    setMessage("");
    setSubmitting(true);
    try {
      const data = await request("/api/auth/login", {
        method: "POST",
        body: JSON.stringify({ account: account.trim(), password }),
      });
      localStorage.setItem(TOKEN_KEY, data.token);
      onAuthenticated(data.user);
    } catch (error) {
      setMessage(error instanceof TypeError ? "无法连接服务端，请确认后端运行在 8080 端口" : error.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="auth-card card">
      <p className="eyebrow">PROJECT MANAGER</p>
      <h1>统一登录</h1>
      <p className="muted">请使用管理员为你开通的账号登录系统。</p>
      <form onSubmit={submit}>
        <label>账号或邮箱
          <input type="text" value={account} onChange={(event) => setAccount(event.target.value)} required autoComplete="username" placeholder="请输入账号或邮箱" />
        </label>
        <label>密码
          <input type="password" value={password} onChange={(event) => setPassword(event.target.value)} required autoComplete="current-password" placeholder="请输入密码" />
        </label>
        <button className="primary" disabled={submitting} type="submit">{submitting ? "登录中…" : "登录"}</button>
      </form>
      <p className="message">{message}</p>
    </section>
  );
}

export { AuthForm };
