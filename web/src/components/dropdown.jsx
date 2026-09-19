import React from "react";

function OptionDropdown({ options, value, onChange, placeholder = "请选择", compact = false, disabled = false }) {
  const [open, setOpen] = React.useState(false);
  const containerRef = React.useRef(null);
  const selectedOption = options.find((option) => option.value === value);

  React.useEffect(() => {
    function closeOnOutsideClick(event) {
      if (!containerRef.current?.contains(event.target)) setOpen(false);
    }
    function closeOnEscape(event) {
      if (event.key === "Escape") setOpen(false);
    }
    document.addEventListener("mousedown", closeOnOutsideClick);
    document.addEventListener("keydown", closeOnEscape);
    return () => {
      document.removeEventListener("mousedown", closeOnOutsideClick);
      document.removeEventListener("keydown", closeOnEscape);
    };
  }, []);

  function selectOption(option) {
    onChange(option.value);
    setOpen(false);
  }

  return (
    <div className={`custom-select ${compact ? "custom-select-compact" : ""} ${open ? "custom-select-open" : ""}`} ref={containerRef}>
      <button
        aria-expanded={open}
        aria-haspopup="listbox"
        className="custom-select-trigger"
        disabled={disabled}
        onClick={() => setOpen((current) => !current)}
        type="button"
      >
        <span className="custom-select-value">{selectedOption?.label || placeholder}</span>
        <span className="custom-select-arrow" aria-hidden="true">⌄</span>
      </button>
      {open && <div className="custom-select-menu" role="listbox">
        {options.map((option) => <button
          aria-selected={option.value === value}
          className={`custom-select-option ${option.value === value ? "custom-select-option-active" : ""}`}
          key={option.value}
          onClick={() => selectOption(option)}
          role="option"
          type="button"
        >
          <span>{option.label}</span>
          {option.secondary && <small>{option.secondary}</small>}
          {option.value === value && <b aria-hidden="true">✓</b>}
        </button>)}
      </div>}
    </div>
  );
}

function RoleDropdown({ roles, value, onChange, compact = false, disabled = false }) {
  return (
    <OptionDropdown
      compact={compact}
      disabled={disabled}
      onChange={onChange}
      options={roles.map((role) => ({ value: role.id, label: role.name }))}
      placeholder="请选择角色"
      value={value}
    />
  );
}

export { OptionDropdown, RoleDropdown };
