package at.fhtw.webenprjbackend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
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
import org.springframework.test.web.servlet.MockMvc;

import at.fhtw.webenprjbackend.dto.PostResponse;
import at.fhtw.webenprjbackend.service.PostService;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @Test
    void getAllPosts_returnsPagedResults() throws Exception {
        PostResponse sample = new PostResponse(
                UUID.randomUUID(),
                "#webengineering",
                "content",
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                UUID.randomUUID(),
                "alice"
        );
        Page<PostResponse> page = new PageImpl<>(List.of(sample), PageRequest.of(1, 5), 6);
        when(postService.getAllPosts(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/posts")
                        .param("page", "1")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(postService).getAllPosts(PageRequest.of(1, 5));
    }
}
