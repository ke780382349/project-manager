import React from "react";

function PlaceholderPage({ item }) {
  return (
    <div className="placeholder-page">
      <span className="placeholder-icon">{item.icon}</span>
      <h1>{item.label}</h1>
      <p>这个模块已经加入系统导航，后续将接入对应的业务功能。</p>
    </div>
  );
}

export { PlaceholderPage };
