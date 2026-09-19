package com.example.projectmanager.user;

import java.util.Map;
import java.util.Set;

/** Stable UUIDs used by the built-in permissions and method security rules. */
public final class PermissionIds {

    public static final String USER_MANAGE = "188F3A564F4243468F849927D46E8295";
    public static final String ROLE_MANAGE = "029A7C6E986F453398F5B093AED62B61";
    public static final String PROJECT_VIEW = "62A98D51041D4C928B63398D01084EE1";
    public static final String PROJECT_MANAGE = "3D5CBDC92B1E4F99B4F0A7A8159C4D51";
    public static final String TASK_VIEW = "DCEC1AAA8B9A4C358EEADD5E70F5F97E";
    public static final String TASK_MANAGE = "374EA345C503406AB1F1A140038C71FC";
    public static final String REQUIREMENT_MANAGE = "2549192438C64A56A4B14CFEF84BA20A";
    public static final String BUG_MANAGE = "0C516212DB664C7EA342DA7D437E92C0";
    public static final String CHANGE_MANAGE = "C033958CD54F46C38337D0C181EB13F2";
    public static final String RELEASE_MANAGE = "16612BDAB3D64E38863D252B69698FC7";
    public static final String REPORT_VIEW = "A803E73212404898A1527F4EFED32341";

    private PermissionIds() {
    }

    public static Set<String> all() {
        return Set.of(
                USER_MANAGE, ROLE_MANAGE, PROJECT_VIEW, PROJECT_MANAGE,
                TASK_VIEW, TASK_MANAGE, REQUIREMENT_MANAGE, BUG_MANAGE,
                CHANGE_MANAGE, RELEASE_MANAGE, REPORT_VIEW
        );
    }

    public static Map<String, String> names() {
        return Map.ofEntries(
                Map.entry(USER_MANAGE, "用户管理"),
                Map.entry(ROLE_MANAGE, "角色管理"),
                Map.entry(PROJECT_VIEW, "查看项目"),
                Map.entry(PROJECT_MANAGE, "管理项目"),
                Map.entry(TASK_VIEW, "查看任务"),
                Map.entry(TASK_MANAGE, "管理任务"),
                Map.entry(REQUIREMENT_MANAGE, "管理需求"),
                Map.entry(BUG_MANAGE, "管理 Bug"),
                Map.entry(CHANGE_MANAGE, "管理变更"),
                Map.entry(RELEASE_MANAGE, "管理发布"),
                Map.entry(REPORT_VIEW, "查看报表")
        );
    }
}
