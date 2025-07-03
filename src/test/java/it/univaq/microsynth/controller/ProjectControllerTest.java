package it.univaq.microsynth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.univaq.microsynth.domain.dto.DocumentResponseDTO;
import it.univaq.microsynth.domain.dto.PaginatedRequestDTO;
import it.univaq.microsynth.domain.dto.ProjectDTO;
import it.univaq.microsynth.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
@Import(ProjectControllerTest.MockConfig.class)
class ProjectControllerTest {

    @TestConfiguration
    static class MockConfig {
        @Bean
        public ProjectService projectService() {
            return Mockito.mock(ProjectService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectService projectService;

//    @Test
//    @WithMockUser(username = "testuser")
//    void testGetUserProjects() throws Exception {
//        Mockito.when(projectService.getUserProjects(eq("testuser"), any()))
//                .thenReturn(ResponseEntity.ok());
//
//        mockMvc.perform(get("/api/project"))
//                .andExpect(status().isOk());
//    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetProjectById() throws Exception {
        ProjectDTO project = new ProjectDTO();
        project.setId("1");
        project.setUserName("testuser");

        Mockito.when(projectService.getProjectById(eq("1"), eq("testuser")))
                .thenReturn(ResponseEntity.ok(project));

        mockMvc.perform(get("/api/project/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCreateProject() throws Exception {
        ProjectDTO input = new ProjectDTO();
        input.setName("New Project");

        DocumentResponseDTO response = new DocumentResponseDTO();
        response.setId("123");

        Mockito.when(projectService.createProject(any(ProjectDTO.class), eq("testuser")))
                .thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(post("/api/project")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"));
    }

    @Test
    void testUpdateProject() throws Exception {
        ProjectDTO updateDTO = new ProjectDTO();
        updateDTO.setName("Updated");

        DocumentResponseDTO response = new DocumentResponseDTO();
        response.setId("1");

        Mockito.when(projectService.updateProject(eq("1"), any(ProjectDTO.class)))
                .thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(put("/api/project/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    void testDeleteProject() throws Exception {
        Mockito.when(projectService.deleteProject("1"))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(delete("/api/project/1"))
                .andExpect(status().isOk());
    }
}
