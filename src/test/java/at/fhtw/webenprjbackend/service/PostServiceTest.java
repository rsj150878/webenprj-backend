package at.fhtw.webenprjbackend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import at.fhtw.webenprjbackend.dto.PostResponse;
import at.fhtw.webenprjbackend.entity.Post;
import at.fhtw.webenprjbackend.entity.Role;
import at.fhtw.webenprjbackend.entity.User;
import at.fhtw.webenprjbackend.repository.FollowRepository;
import at.fhtw.webenprjbackend.repository.PostLikeRepository;
import at.fhtw.webenprjbackend.repository.PostRepository;
import at.fhtw.webenprjbackend.repository.UserRepository;

class PostServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PostLikeRepository postLikeRepository;
    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private PostService postService;

    private User author;
    private Post postA;
    private Post postB;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        author = new User(
                "alice@example.com",
                "alice",
                "pw",
                "AT",
                null,
                Role.USER
        );
        postA = new Post("math", "a".repeat(20), null, author);
        postB = new Post("java", "b".repeat(20), null, author);
        postA.setSubject("math");
        postB.setSubject("java");
        postA.setContent("content A");
        postB.setContent("content B");
        // IDs for lookups
        var postAId = UUID.randomUUID();
        var postBId = UUID.randomUUID();
        try {
            var idField = Post.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(postA, postAId);
            idField.set(postB, postBId);
        } catch (NoSuchFieldException | IllegalAccessException ignored) { }
    }

    @Test
    void getAllPosts_usesBulkLikeCountsAndFlagsLikedPosts() {
        var currentUserId = UUID.randomUUID();
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Post> page = new PageImpl<>(List.of(postA, postB), pageRequest, 2);

        when(postRepository.findAllByOrderByCreatedAtDesc(pageRequest)).thenReturn(page);
        when(postLikeRepository.countLikesByPostIds(any()))
                .thenReturn(List.of(
                        new Object[]{postA.getId(), 3L},
                        new Object[]{postB.getId(), 1L}
                ));
        when(postLikeRepository.findLikedPostIds(currentUserId, List.of(postA.getId(), postB.getId())))
                .thenReturn(List.of(postB.getId()));

        Page<PostResponse> result = postService.getAllPosts(pageRequest, currentUserId);

        Map<UUID, PostResponse> byId = result.getContent().stream()
                .collect(java.util.stream.Collectors.toMap(PostResponse::id, p -> p));

        assertThat(byId.get(postA.getId()).likeCount()).isEqualTo(3);
        assertThat(byId.get(postA.getId()).likedByCurrentUser()).isFalse();

        assertThat(byId.get(postB.getId()).likeCount()).isEqualTo(1);
        assertThat(byId.get(postB.getId()).likedByCurrentUser()).isTrue();
    }
}
