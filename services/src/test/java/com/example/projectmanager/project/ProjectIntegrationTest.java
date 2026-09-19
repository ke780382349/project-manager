package com.example.projectmanager.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.projectmanager.user.RoleRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProjectIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired RoleRepository roleRepository;
    @Autowired ProjectRepository projectRepository;

    @Test
    void createsProjectWithOwnerAsMemberAndValidatesInput() throws Exception {
        String admin = login("admin", "admin");
        JsonNode created = read(mvc.perform(post("/api/projects")
                        .header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of(
                                "name", " 官网改版 ",
                                "description", " 第三季度上线 "))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.owner.displayName").value("管理员"))
                .andExpect(jsonPath("$.members[0].owner").value(true))
                .andReturn());
        String id = created.get("id").asText();
        assertThat(id).matches("[0-9A-F]{32}");
        assertThat(created.get("name").asText()).isEqualTo("官网改版");
        assertThat(created.get("description").asText()).isEqualTo("第三季度上线");
        assertThat(projectRepository.findById(id)).isPresent();

        mvc.perform(post("/api/projects").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"  \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listsOnlyVisibleProjectsAndEnforcesManagePermission() throws Exception {
        String admin = login("admin", "admin");
        String member = createMember();

        JsonNode created = read(mvc.perform(post("/api/projects").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"内部工具\"}"))
                .andExpect(status().isCreated()).andReturn());
        String id = created.get("id").asText();

        mvc.perform(get("/api/projects").header("Authorization", member))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/projects/{id}", id).header("Authorization", member))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/projects").header("Authorization", member)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"越权创建\"}"))
                .andExpect(status().isForbidden());

        JsonNode memberProfile = read(mvc.perform(get("/api/auth/me").header("Authorization", member))
                .andExpect(status().isOk()).andReturn());
        mvc.perform(post("/api/projects/{id}/members", id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("userId", memberProfile.get("id").asText()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members.length()").value(2));

        mvc.perform(get("/api/projects").header("Authorization", member))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(id));
        mvc.perform(patch("/api/projects/{id}", id).header("Authorization", member)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"越权修改\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/projects/{id}", id).header("Authorization", member))
                .andExpect(status().isForbidden());
    }

    @Test
    void updatesProjectAndMembersThenDeletes() throws Exception {
        String admin = login("admin", "admin");
        String member = createMember();
        JsonNode memberProfile = read(mvc.perform(get("/api/auth/me").header("Authorization", member))
                .andExpect(status().isOk()).andReturn());
        String memberId = memberProfile.get("id").asText();

        JsonNode created = read(mvc.perform(post("/api/projects").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"数据平台\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.members.length()").value(1)).andReturn());
        String id = created.get("id").asText();
        String ownerId = created.get("owner").get("id").asText();

        mvc.perform(post("/api/projects/{id}/members", id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("userId", memberId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members.length()").value(2));
        // 重复添加同一成员返回冲突。
        mvc.perform(post("/api/projects/{id}/members", id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("userId", memberId))))
                .andExpect(status().isConflict());
        // 添加不存在的用户返回 400。
        mvc.perform(post("/api/projects/{id}/members", id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF\"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(patch("/api/projects/{id}", id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"数据平台二期\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("数据平台二期"));

        // 负责人不能被移除。
        mvc.perform(delete("/api/projects/{id}/members/{userId}", id, ownerId).header("Authorization", admin))
                .andExpect(status().isBadRequest());
        // 移除普通成员后只剩负责人。
        mvc.perform(delete("/api/projects/{id}/members/{userId}", id, memberId).header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members.length()").value(1))
                .andExpect(jsonPath("$.members[0].id").value(ownerId));
        // 重复移除返回 404。
        mvc.perform(delete("/api/projects/{id}/members/{userId}", id, memberId).header("Authorization", admin))
                .andExpect(status().isNotFound());

        mvc.perform(delete("/api/projects/{id}", id).header("Authorization", admin))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/projects/{id}", id).header("Authorization", admin))
                .andExpect(status().isNotFound());
        assertThat(projectRepository.findById(id)).isEmpty();
    }

    private String createMember() throws Exception {
        String admin = login("admin", "admin");
        String userRoleId = roleRepository.findByCodeIgnoreCase("USER").orElseThrow().getId();
        mvc.perform(post("/api/users").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of(
                                "username", "pm-member",
                                "email", "pm-member@example.com",
                                "displayName", "项目成员",
                                "password", "password123",
                                "roleId", userRoleId))))
                .andExpect(status().isCreated());
        return login("pm-member", "password123");
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
