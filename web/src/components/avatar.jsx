import React from "react";

function UserAvatar({ user, small = false }) {
  const name = user?.displayName || user?.username || user?.email || "用户";
  return <span className={`avatar ${small ? "avatar-small" : ""}`}>{name.slice(0, 1).toUpperCase()}</span>;
}

export { UserAvatar };
