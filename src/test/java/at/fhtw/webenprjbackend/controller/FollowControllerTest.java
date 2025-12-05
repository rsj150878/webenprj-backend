package at.fhtw.webenprjbackend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import at.fhtw.webenprjbackend.dto.UserResponse;
import at.fhtw.webenprjbackend.service.FollowService;

@WebMvcTest(FollowController.class)
class FollowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FollowService followService;

    @Test
    @WithMockUser(roles = "USER")
    void follow_returnsNoContent() throws Exception {
        UUID userId = UUID.randomUUID();
        mockMvc.perform(post("/users/{id}/follow", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void unfollow_returnsNoContent() throws Exception {
        UUID userId = UUID.randomUUID();
        mockMvc.perform(delete("/users/{id}/follow", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void getFollowers_returnsPage() throws Exception {
        UserResponse user = new UserResponse(
                UUID.randomUUID(), "a@b.com", "u1", "AT", null, "USER", null, null, 0, 0);
        Page<UserResponse> page = new PageImpl<>(List.of(user), PageRequest.of(0, 5), 1);
        org.mockito.Mockito.when(followService.getFollowers(any(UUID.class), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/users/{id}/followers", UUID.randomUUID())
                        .param("page", "0")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(followService).getFollowers(any(UUID.class), any(Pageable.class));
    }

    @Test
    void getFollowing_returnsPage() throws Exception {
        UserResponse user = new UserResponse(
                UUID.randomUUID(), "a@b.com", "u1", "AT", null, "USER", null, null, 0, 0);
        Page<UserResponse> page = new PageImpl<>(List.of(user), PageRequest.of(0, 5), 1);
        org.mockito.Mockito.when(followService.getFollowing(any(UUID.class), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/users/{id}/following", UUID.randomUUID())
                        .param("page", "0")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(followService).getFollowing(any(UUID.class), any(Pageable.class));
    }
}
