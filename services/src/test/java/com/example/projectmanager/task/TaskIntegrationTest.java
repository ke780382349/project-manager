package com.example.projectmanager.task;

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
class TaskIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired RoleRepository roleRepository;
    @Autowired TaskRepository taskRepository;

    @Test
    void createsTaskWithAssigneeAndValidatesInput() throws Exception {
        String admin = login("admin", "admin");
        String projectId = createProject(admin, "任务验证项目");
        String adminId = read(mvc.perform(get("/api/auth/me").header("Authorization", admin))
                .andExpect(status().isOk()).andReturn()).get("id").asText();

        JsonNode created = read(mvc.perform(post("/api/tasks").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of(
                                "projectId", projectId,
                                "title", " 完成导出接口 ",
                                "description", "支持 CSV",
                                "assigneeId", adminId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.assigneeName").value("管理员"))
                .andExpect(jsonPath("$.creatorName").value("管理员"))
                .andReturn());
        String id = created.get("id").asText();
        assertThat(id).matches("[0-9A-F]{32}");
        assertThat(created.get("title").asText()).isEqualTo("完成导出接口");
        assertThat(taskRepository.findById(id)).isPresent();

        mvc.perform(post("/api/tasks").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("projectId", projectId, "title", "  "))))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/tasks").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of(
                                "projectId", projectId,
                                "title", "负责人不存在",
                                "assigneeId", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void hidesTasksOfInvisibleProjectForMember() throws Exception {
        String admin = login("admin", "admin");
        String member = createMember();
        String projectId = createProject(admin, "协作任务项目");

        mvc.perform(post("/api/tasks").header("Authorization", member)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("projectId", projectId, "title", "越权任务"))))
                .andExpect(status().isForbidden());

        String memberId = read(mvc.perform(get("/api/auth/me").header("Authorization", member))
                .andExpect(status().isOk()).andReturn()).get("id").asText();
        mvc.perform(post("/api/projects/{id}/members", projectId).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("userId", memberId))))
                .andExpect(status().isOk());

        JsonNode created = read(mvc.perform(post("/api/tasks").header("Authorization", member)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("projectId", projectId, "title", "成员的任务"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.creatorName").value("项目成员"))
                .andReturn());
        String id = created.get("id").asText();

        mvc.perform(get("/api/tasks").queryParam("projectId", projectId).header("Authorization", member))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(id));

        mvc.perform(delete("/api/tasks/{id}", id).header("Authorization", member))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/tasks").header("Authorization", member))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void updatesStatusAndAssigneeThenDeletes() throws Exception {
        String admin = login("admin", "admin");
        String projectId = createProject(admin, "状态流转项目");
        JsonNode created = read(mvc.perform(post("/api/tasks").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("projectId", projectId, "title", "待办任务"))))
                .andExpect(status().isCreated()).andReturn());
        String id = created.get("id").asText();

        mvc.perform(patch("/api/tasks/{id}", id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"待办任务\",\"status\":\"NOT_A_STATUS\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/tasks/{id}", id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"进行中任务\",\"description\":\"开发中\",\"status\":\"DOING\",\"assigneeId\":null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DOING"))
                .andExpect(jsonPath("$.assigneeName").doesNotExist());

        mvc.perform(delete("/api/tasks/{id}", id).header("Authorization", admin))
                .andExpect(status().isNoContent());
        assertThat(taskRepository.findById(id)).isEmpty();
    }

    private String createProject(String token, String name) throws Exception {
        JsonNode created = read(mvc.perform(post("/api/projects").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("name", name))))
                .andExpect(status().isCreated()).andReturn());
        return created.get("id").asText();
    }

    private String createMember() throws Exception {
        String admin = login("admin", "admin");
        String userRoleId = roleRepository.findByCodeIgnoreCase("USER").orElseThrow().getId();
        mvc.perform(post("/api/users").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of(
                                "username", "task-member",
                                "email", "task-member@example.com",
                                "displayName", "项目成员",
                                "password", "password123",
                                "roleId", userRoleId))))
                .andExpect(status().isCreated());
        return login("task-member", "password123");
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
