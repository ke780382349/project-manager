package com.example.projectmanager.requirement;

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
class RequirementIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired RoleRepository roleRepository;
    @Autowired RequirementRepository requirementRepository;

    @Test
    void createsRequirementBoundToProjectAndValidatesInput() throws Exception {
        String admin = login("admin", "admin");
        String projectId = createProject(admin, "需求池验证项目");

        JsonNode created = read(mvc.perform(post("/api/requirements").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of(
                                "projectId", projectId,
                                "title", " 支持导出报表 ",
                                "description", "按项目导出 CSV"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.projectName").value("需求池验证项目"))
                .andExpect(jsonPath("$.creatorName").value("管理员"))
                .andReturn());
        String id = created.get("id").asText();
        assertThat(id).matches("[0-9A-F]{32}");
        assertThat(created.get("title").asText()).isEqualTo("支持导出报表");
        assertThat(requirementRepository.findById(id)).isPresent();

        mvc.perform(post("/api/requirements").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("projectId", projectId, "title", "  "))))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/requirements").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of(
                                "projectId", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                                "title", "不存在的项目"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void viewerCanSubmitButOnlyManagerCanReview() throws Exception {
        String admin = login("admin", "admin");
        String member = createMember();
        String projectId = createProject(admin, "协作项目");

        mvc.perform(post("/api/requirements").header("Authorization", member)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("projectId", projectId, "title", "越权提交"))))
                .andExpect(status().isForbidden());

        JsonNode memberProfile = read(mvc.perform(get("/api/auth/me").header("Authorization", member))
                .andExpect(status().isOk()).andReturn());
        mvc.perform(post("/api/projects/{id}/members", projectId).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("userId", memberProfile.get("id").asText()))))
                .andExpect(status().isOk());

        JsonNode created = read(mvc.perform(post("/api/requirements").header("Authorization", member)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("projectId", projectId, "title", "成员提出的需求"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.creatorName").value("项目成员"))
                .andReturn());
        String id = created.get("id").asText();

        mvc.perform(get("/api/requirements").header("Authorization", member))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/requirements").queryParam("projectId", projectId).header("Authorization", member))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));
        mvc.perform(patch("/api/requirements/{id}", id).header("Authorization", member)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"成员改状态\",\"status\":\"ACCEPTED\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/requirements/{id}", id).header("Authorization", member))
                .andExpect(status().isForbidden());
    }

    @Test
    void updatesStatusAndDeletesRequirement() throws Exception {
        String admin = login("admin", "admin");
        String projectId = createProject(admin, "评审项目");
        JsonNode created = read(mvc.perform(post("/api/requirements").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("projectId", projectId, "title", "待评审需求"))))
                .andExpect(status().isCreated()).andReturn());
        String id = created.get("id").asText();

        mvc.perform(patch("/api/requirements/{id}", id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"待评审需求\",\"status\":\"NOT_A_STATUS\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/requirements/{id}", id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"已接受需求\",\"description\":\"评审通过\",\"status\":\"ACCEPTED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.title").value("已接受需求"));

        mvc.perform(delete("/api/requirements/{id}", id).header("Authorization", admin))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/requirements").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        assertThat(requirementRepository.findById(id)).isEmpty();
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
                                "username", "req-member",
                                "email", "req-member@example.com",
                                "displayName", "项目成员",
                                "password", "password123",
                                "roleId", userRoleId))))
                .andExpect(status().isCreated());
        return login("req-member", "password123");
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
