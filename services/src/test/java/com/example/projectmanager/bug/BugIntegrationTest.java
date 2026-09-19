package com.example.projectmanager.bug;

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
class BugIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired RoleRepository roleRepository;
    @Autowired BugRepository bugRepository;

    @Test
    void createsBugWithDefaultSeverityAndStatus() throws Exception {
        String admin = login("admin", "admin");
        String projectId = createProject(admin, "Bug 验证项目");

        JsonNode created = read(mvc.perform(post("/api/bugs").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of(
                                "projectId", projectId,
                                "title", " 导出缺少成员列 ",
                                "description", "复现：点击导出"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.severity").value("MEDIUM"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.reporterName").value("管理员"))
                .andReturn());
        String id = created.get("id").asText();
        assertThat(id).matches("[0-9A-F]{32}");
        assertThat(created.get("title").asText()).isEqualTo("导出缺少成员列");
        assertThat(bugRepository.findById(id)).isPresent();

        mvc.perform(post("/api/bugs").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("projectId", projectId, "title", "  "))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reporterCanSubmitButOnlyManagerCanResolve() throws Exception {
        String admin = login("admin", "admin");
        String member = createMember();
        String projectId = createProject(admin, "协作 Bug 项目");

        mvc.perform(post("/api/bugs").header("Authorization", member)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("projectId", projectId, "title", "越权 Bug"))))
                .andExpect(status().isForbidden());

        String memberId = read(mvc.perform(get("/api/auth/me").header("Authorization", member))
                .andExpect(status().isOk()).andReturn()).get("id").asText();
        mvc.perform(post("/api/projects/{id}/members", projectId).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("userId", memberId))))
                .andExpect(status().isOk());

        JsonNode created = read(mvc.perform(post("/api/bugs").header("Authorization", member)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of(
                                "projectId", projectId,
                                "title", "登录页报错",
                                "severity", "HIGH"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.severity").value("HIGH"))
                .andExpect(jsonPath("$.reporterName").value("项目成员"))
                .andReturn());
        String id = created.get("id").asText();

        mvc.perform(get("/api/bugs").queryParam("projectId", projectId).header("Authorization", member))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mvc.perform(patch("/api/bugs/{id}", id).header("Authorization", member)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"登录页报错\",\"severity\":\"HIGH\",\"status\":\"FIXED\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/bugs/{id}", id).header("Authorization", member))
                .andExpect(status().isForbidden());

        mvc.perform(patch("/api/bugs/{id}", id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"登录页报错\",\"description\":\"已定位\",\"severity\":\"HIGH\",\"status\":\"FIXED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FIXED"));
        mvc.perform(delete("/api/bugs/{id}", id).header("Authorization", admin))
                .andExpect(status().isNoContent());
        assertThat(bugRepository.findById(id)).isEmpty();
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
                                "username", "bug-member",
                                "email", "bug-member@example.com",
                                "displayName", "项目成员",
                                "password", "password123",
                                "roleId", userRoleId))))
                .andExpect(status().isCreated());
        return login("bug-member", "password123");
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
