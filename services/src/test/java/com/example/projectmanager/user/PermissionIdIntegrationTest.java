package com.example.projectmanager.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PermissionIdIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired PermissionRepository permissionRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired AdminAccountInitializer initializer;

    @Test
    void createsWithoutCodeAndPreservesRoleAssignmentWhenRenamed() throws Exception {
        String admin = login("admin", "admin");
        JsonNode created = read(mvc.perform(post("/api/permissions")
                        .header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("name", "导出项目", "description", "导出权限"))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.code").doesNotExist()).andReturn());
        String id = created.get("id").asText();
        assertThat(id).matches("[0-9A-F]{32}");
        assertThat(permissionRepository.findById(id)).isPresent();

        JsonNode role = read(mvc.perform(post("/api/roles")
                        .header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("name", "导出人员", "permissionIds", List.of(id)))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.permissions[0].id").value(id))
                .andExpect(jsonPath("$.permissions[0].code").doesNotExist()).andReturn());
        mvc.perform(patch("/api/permissions/{id}", id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"批量导出\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
        Role savedRole = roleRepository.findById(role.get("id").asText()).orElseThrow();
        assertThat(savedRole.getPermissions()).extracting(Permission::getId).containsExactly(id);
        assertThat(savedRole.getPermissions()).extracting(Permission::getName).containsExactly("批量导出");
        mvc.perform(delete("/api/permissions/{id}", id).header("Authorization", admin))
                .andExpect(status().isConflict());
        mvc.perform(delete("/api/roles/{id}", savedRole.getId()).header("Authorization", admin))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/api/permissions/{id}", id).header("Authorization", admin))
                .andExpect(status().isNoContent());
    }

    @Test
    void authorizesByAssignedIdAndAppliesRevocationToExistingToken() throws Exception {
        String admin = login("admin", "admin");
        // A custom permission with the same name must not grant system access.
        Permission sameName = permissionRepository.save(new Permission("用户管理", null, false));
        Role role = roleRepository.save(new Role("权限验证", null, Set.of(sameName)));
        userRepository.save(new User("permission-check", "permission-check@example.com",
                passwordEncoder.encode("password123"), "验证用户", role.getId()));
        String member = login("permission-check", "password123");
        assertAccess(member, 403, 403, 403);

        updateRole(admin, role.getId(), List.of(PermissionIds.USER_MANAGE));
        assertAccess(member, 200, 200, 403);
        mvc.perform(get("/api/auth/me").header("Authorization", member))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissions[0]").value(PermissionIds.USER_MANAGE));
        mvc.perform(post("/api/roles").header("Authorization", member)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"不能创建\",\"permissionIds\":[]}"))
                .andExpect(status().isForbidden());

        updateRole(admin, role.getId(), List.of(PermissionIds.ROLE_MANAGE));
        assertAccess(member, 403, 200, 200);
        mvc.perform(post("/api/permissions").header("Authorization", member)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"可创建权限\"}"))
                .andExpect(status().isCreated());
        updateRole(admin, role.getId(), List.of());
        assertAccess(member, 403, 403, 403);
    }

    @Test
    void initializationKeepsIdsStableAndLoginReturnsIds() throws Exception {
        Set<String> ids = PermissionIds.all();
        initializer.run();
        initializer.run();
        assertThat(permissionRepository.findAll()).extracting(Permission::getId)
                .containsExactlyInAnyOrderElementsOf(ids);
        String admin = login("admin", "admin");
        JsonNode me = read(mvc.perform(get("/api/auth/me").header("Authorization", admin))
                .andExpect(status().isOk()).andReturn());
        assertThat(json.convertValue(me.get("permissions"), String[].class))
                .containsExactlyInAnyOrderElementsOf(ids);
        assertAccess(admin, 200, 200, 200);
        for (String id : ids) {
            assertThat(id).matches("[0-9A-F]{32}");
        }
    }

    @Test
    void rejectsOldPermissionCodesAndUnknownIds() throws Exception {
        String admin = login("admin", "admin");
        for (String invalid : List.of("USER_MANAGE", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF")) {
            mvc.perform(post("/api/roles").header("Authorization", admin)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsBytes(Map.of("name", "无效角色", "permissionIds", List.of(invalid)))))
                    .andExpect(status().isBadRequest());
        }
    }

    private void updateRole(String admin, String roleId, List<String> ids) throws Exception {
        mvc.perform(patch("/api/roles/{id}", roleId).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("name", "权限验证", "permissionIds", ids))))
                .andExpect(status().isOk());
    }

    private void assertAccess(String token, int users, int roles, int permissions) throws Exception {
        mvc.perform(get("/api/users").header("Authorization", token)).andExpect(status().is(users));
        mvc.perform(get("/api/roles").header("Authorization", token)).andExpect(status().is(roles));
        mvc.perform(get("/api/permissions").header("Authorization", token)).andExpect(status().is(permissions));
    }

    private String login(String account, String password) throws Exception {
        JsonNode response = read(mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("account", account, "password", password))))
                .andExpect(status().isOk()).andReturn());
        return "Bearer " + response.get("token").asText();
    }

    private JsonNode read(MvcResult result) throws Exception {
        return json.readTree(result.getResponse().getContentAsByteArray());
    }
}
