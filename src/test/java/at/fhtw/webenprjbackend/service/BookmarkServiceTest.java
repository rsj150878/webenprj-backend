package at.fhtw.webenprjbackend.service;

import at.fhtw.webenprjbackend.dto.*;
import at.fhtw.webenprjbackend.entity.*;
import at.fhtw.webenprjbackend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookmarkService")
class BookmarkServiceTest {

    @Mock
    private PostBookmarkRepository bookmarkRepository;

    @Mock
    private BookmarkCollectionRepository collectionRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    private BookmarkService bookmarkService;

    private User testUser;
    private Post testPost;
    private UUID userId;
    private UUID postId;

    @BeforeEach
    void setUp() {
        bookmarkService = new BookmarkService(bookmarkRepository, collectionRepository, postRepository, userRepository);

        userId = UUID.randomUUID();
        postId = UUID.randomUUID();

        testUser = createTestUser(userId, "testuser", "test@example.com");
        testPost = createTestPost(postId, "webdev", "Learning Spring Boot!", testUser);
    }

    private User createTestUser(UUID id, String username, String email) {
        User user = new User(email, username, "hashedPassword", "AT",
                "https://example.com/profile.png", "Dr.", Role.USER);
        setField(user, "id", id);
        setField(user, "createdAt", LocalDateTime.now());
        return user;
    }

    private Post createTestPost(UUID id, String subject, String content, User user) {
        Post post = new Post(subject, content, null, user);
        setField(post, "id", id);
        setField(post, "createdAt", LocalDateTime.now());
        return post;
    }

    private void setField(Object obj, String fieldName, Object value) {
        try {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }

    @Nested
    @DisplayName("createBookmark()")
    class CreateBookmarkTests {

        @Test
        @DisplayName("should create bookmark successfully")
        void createBookmark_success() {
            BookmarkCreateRequest request = new BookmarkCreateRequest(null, "My notes");

            when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(bookmarkRepository.findByUserAndPost(testUser, testPost)).thenReturn(Optional.empty());
            when(bookmarkRepository.save(any(PostBookmark.class))).thenAnswer(invocation -> {
                PostBookmark bookmark = invocation.getArgument(0);
                setField(bookmark, "id", UUID.randomUUID());
                setField(bookmark, "createdAt", LocalDateTime.now());
                return bookmark;
            });

            BookmarkResponse result = bookmarkService.createBookmark(postId, userId, request);

            assertThat(result).isNotNull();
            verify(bookmarkRepository).save(any(PostBookmark.class));
        }

        @Test
        @DisplayName("should return existing bookmark if already bookmarked (idempotent)")
        void createBookmark_alreadyExists_returnsExisting() {
            BookmarkCreateRequest request = new BookmarkCreateRequest(null, "My notes");
            PostBookmark existingBookmark = new PostBookmark(testUser, testPost, null, "Old notes");
            setField(existingBookmark, "id", UUID.randomUUID());
            setField(existingBookmark, "createdAt", LocalDateTime.now());

            when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(bookmarkRepository.findByUserAndPost(testUser, testPost)).thenReturn(Optional.of(existingBookmark));

            BookmarkResponse result = bookmarkService.createBookmark(postId, userId, request);

            assertThat(result).isNotNull();
            verify(bookmarkRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when post not found")
        void createBookmark_postNotFound_throwsException() {
            BookmarkCreateRequest request = new BookmarkCreateRequest(null, "My notes");
            when(postRepository.findById(postId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookmarkService.createBookmark(postId, userId, request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Post not found");
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void createBookmark_userNotFound_throwsException() {
            BookmarkCreateRequest request = new BookmarkCreateRequest(null, "My notes");
            when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookmarkService.createBookmark(postId, userId, request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("should throw exception when collection not found")
        void createBookmark_collectionNotFound_throwsException() {
            UUID collectionId = UUID.randomUUID();
            BookmarkCreateRequest request = new BookmarkCreateRequest(collectionId, "My notes");

            when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(bookmarkRepository.findByUserAndPost(testUser, testPost)).thenReturn(Optional.empty());
            when(collectionRepository.findById(collectionId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookmarkService.createBookmark(postId, userId, request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Collection not found");
        }

        @Test
        @DisplayName("should throw exception when collection belongs to another user")
        void createBookmark_collectionForbidden_throwsException() {
            UUID collectionId = UUID.randomUUID();
            User otherUser = createTestUser(UUID.randomUUID(), "other", "other@example.com");
            BookmarkCollection collection = new BookmarkCollection(otherUser, "Other's Collection", null, null, null);
            setField(collection, "id", collectionId);
            BookmarkCreateRequest request = new BookmarkCreateRequest(collectionId, "My notes");

            when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(bookmarkRepository.findByUserAndPost(testUser, testPost)).thenReturn(Optional.empty());
            when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));

            assertThatThrownBy(() -> bookmarkService.createBookmark(postId, userId, request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Cannot add to another user's collection");
        }
    }

    @Nested
    @DisplayName("deleteBookmark()")
    class DeleteBookmarkTests {

        @Test
        @DisplayName("should delete bookmark successfully")
        void deleteBookmark_success() {
            when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            bookmarkService.deleteBookmark(postId, userId);

            verify(bookmarkRepository).deleteByUserAndPost(testUser, testPost);
        }
    }

    @Nested
    @DisplayName("updateBookmark()")
    class UpdateBookmarkTests {

        @Test
        @DisplayName("should update bookmark successfully")
        void updateBookmark_success() {
            BookmarkUpdateRequest request = new BookmarkUpdateRequest(null, "Updated notes");
            PostBookmark bookmark = new PostBookmark(testUser, testPost, null, "Old notes");
            setField(bookmark, "id", UUID.randomUUID());
            setField(bookmark, "createdAt", LocalDateTime.now());

            when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(bookmarkRepository.findByUserAndPost(testUser, testPost)).thenReturn(Optional.of(bookmark));
            when(bookmarkRepository.save(any(PostBookmark.class))).thenReturn(bookmark);

            BookmarkResponse result = bookmarkService.updateBookmark(postId, userId, request);

            assertThat(result).isNotNull();
            verify(bookmarkRepository).save(bookmark);
        }

        @Test
        @DisplayName("should throw exception when bookmark not found")
        void updateBookmark_notFound_throwsException() {
            BookmarkUpdateRequest request = new BookmarkUpdateRequest(null, "Updated notes");

            when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(bookmarkRepository.findByUserAndPost(testUser, testPost)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookmarkService.updateBookmark(postId, userId, request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Bookmark not found");
        }
    }

    @Nested
    @DisplayName("getUserBookmarks()")
    class GetUserBookmarksTests {

        @Test
        @DisplayName("should return paginated bookmarks")
        void getUserBookmarks_success() {
            Pageable pageable = PageRequest.of(0, 10);
            PostBookmark bookmark = new PostBookmark(testUser, testPost, null, "Notes");
            setField(bookmark, "id", UUID.randomUUID());
            setField(bookmark, "createdAt", LocalDateTime.now());
            Page<PostBookmark> bookmarkPage = new PageImpl<>(List.of(bookmark), pageable, 1);

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(bookmarkRepository.findByUserOrderByCreatedAtDesc(testUser, pageable)).thenReturn(bookmarkPage);

            Page<BookmarkResponse> result = bookmarkService.getUserBookmarks(userId, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("createCollection()")
    class CreateCollectionTests {

        @Test
        @DisplayName("should create collection successfully")
        void createCollection_success() {
            CollectionCreateRequest request = new CollectionCreateRequest("My Collection", "Description", "#FF0000", "bookmark");

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(collectionRepository.existsByUserAndName(testUser, "My Collection")).thenReturn(false);
            when(collectionRepository.save(any(BookmarkCollection.class))).thenAnswer(invocation -> {
                BookmarkCollection collection = invocation.getArgument(0);
                setField(collection, "id", UUID.randomUUID());
                setField(collection, "createdAt", LocalDateTime.now());
                return collection;
            });
            when(bookmarkRepository.countByCollection(any())).thenReturn(0L);

            BookmarkCollectionResponse result = bookmarkService.createCollection(userId, request);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("My Collection");
        }

        @Test
        @DisplayName("should throw exception when collection name already exists")
        void createCollection_nameExists_throwsException() {
            CollectionCreateRequest request = new CollectionCreateRequest("Existing", "Description", "#FF0000", "bookmark");

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(collectionRepository.existsByUserAndName(testUser, "Existing")).thenReturn(true);

            assertThatThrownBy(() -> bookmarkService.createCollection(userId, request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Collection with this name already exists");
        }
    }

    @Nested
    @DisplayName("deleteCollection()")
    class DeleteCollectionTests {

        @Test
        @DisplayName("should delete collection successfully")
        void deleteCollection_success() {
            UUID collectionId = UUID.randomUUID();
            BookmarkCollection collection = new BookmarkCollection(testUser, "My Collection", null, null, null);
            setField(collection, "id", collectionId);

            when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));

            bookmarkService.deleteCollection(collectionId, userId);

            verify(collectionRepository).delete(collection);
        }

        @Test
        @DisplayName("should throw exception when collection not found")
        void deleteCollection_notFound_throwsException() {
            UUID collectionId = UUID.randomUUID();
            when(collectionRepository.findById(collectionId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookmarkService.deleteCollection(collectionId, userId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Collection not found");
        }

        @Test
        @DisplayName("should throw exception when deleting another user's collection")
        void deleteCollection_forbidden_throwsException() {
            UUID collectionId = UUID.randomUUID();
            User otherUser = createTestUser(UUID.randomUUID(), "other", "other@example.com");
            BookmarkCollection collection = new BookmarkCollection(otherUser, "Other's Collection", null, null, null);
            setField(collection, "id", collectionId);

            when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));

            assertThatThrownBy(() -> bookmarkService.deleteCollection(collectionId, userId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Cannot delete another user's collection");
        }
    }

    @Nested
    @DisplayName("getUserCollections()")
    class GetUserCollectionsTests {

        @Test
        @DisplayName("should return user collections")
        void getUserCollections_success() {
            BookmarkCollection collection = new BookmarkCollection(testUser, "My Collection", "Desc", "#FF0000", "bookmark");
            setField(collection, "id", UUID.randomUUID());
            setField(collection, "createdAt", LocalDateTime.now());

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(collectionRepository.findByUserOrderByCreatedAtAsc(testUser)).thenReturn(List.of(collection));
            when(bookmarkRepository.countByCollection(collection)).thenReturn(5L);

            List<BookmarkCollectionResponse> result = bookmarkService.getUserCollections(userId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("My Collection");
            assertThat(result.get(0).bookmarkCount()).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("fetchBookmarkCounts()")
    class FetchBookmarkCountsTests {

        @Test
        @DisplayName("should return empty map for empty list")
        void fetchBookmarkCounts_emptyList_returnsEmpty() {
            Map<UUID, Long> result = bookmarkService.fetchBookmarkCounts(List.of());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return bookmark counts for posts")
        void fetchBookmarkCounts_success() {
            List<Object[]> countResults = new ArrayList<>();
            countResults.add(new Object[]{postId, 5L});
            when(bookmarkRepository.countBookmarksByPostIds(anyList()))
                    .thenReturn(countResults);

            Map<UUID, Long> result = bookmarkService.fetchBookmarkCounts(List.of(testPost));

            assertThat(result).containsEntry(postId, 5L);
        }
    }

    @Nested
    @DisplayName("fetchBookmarkedPostIds()")
    class FetchBookmarkedPostIdsTests {

        @Test
        @DisplayName("should return empty set when user is null")
        void fetchBookmarkedPostIds_nullUser_returnsEmpty() {
            Set<UUID> result = bookmarkService.fetchBookmarkedPostIds(List.of(testPost), null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty set for empty list")
        void fetchBookmarkedPostIds_emptyList_returnsEmpty() {
            Set<UUID> result = bookmarkService.fetchBookmarkedPostIds(List.of(), userId);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return bookmarked post IDs")
        void fetchBookmarkedPostIds_success() {
            when(bookmarkRepository.findBookmarkedPostIds(eq(userId), anyList()))
                    .thenReturn(List.of(postId));

            Set<UUID> result = bookmarkService.fetchBookmarkedPostIds(List.of(testPost), userId);

            assertThat(result).contains(postId);
        }
    }
}
