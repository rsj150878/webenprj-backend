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
            BookmarkRequest request = new BookmarkRequest(null, "My notes");

            when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(bookmarkRepository.findByUserAndPost(testUser, testPost)).thenReturn(Optional.empty());
            when(bookmarkRepository.save(any(PostBookmark.class))).thenAnswer(invocation -> {
                PostBookmark bookmark = invocation.getArgument(0);
                setField(bookmark, "id", UUID.randomUUID());
                setField(bookmark, "createdAt", LocalDateTime.now());
                return bookmark;
            });

            BookmarkCreateResult result = bookmarkService.createBookmark(postId, userId, request);

            assertThat(result).isNotNull();
            assertThat(result.created()).isTrue();
            assertThat(result.bookmark()).isNotNull();
            verify(bookmarkRepository).save(any(PostBookmark.class));
        }

        @Test
        @DisplayName("should return existing bookmark if already bookmarked (idempotent)")
        void createBookmark_alreadyExists_returnsExisting() {
            BookmarkRequest request = new BookmarkRequest(null, "My notes");
            PostBookmark existingBookmark = new PostBookmark(testUser, testPost, null, "Old notes");
            setField(existingBookmark, "id", UUID.randomUUID());
            setField(existingBookmark, "createdAt", LocalDateTime.now());

            when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(bookmarkRepository.findByUserAndPost(testUser, testPost)).thenReturn(Optional.of(existingBookmark));

            BookmarkCreateResult result = bookmarkService.createBookmark(postId, userId, request);

            assertThat(result).isNotNull();
            assertThat(result.created()).isFalse();
            assertThat(result.bookmark()).isNotNull();
            verify(bookmarkRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when post not found")
        void createBookmark_postNotFound_throwsException() {
            BookmarkRequest request = new BookmarkRequest(null, "My notes");
            when(postRepository.findById(postId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookmarkService.createBookmark(postId, userId, request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Post not found");
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void createBookmark_userNotFound_throwsException() {
            BookmarkRequest request = new BookmarkRequest(null, "My notes");
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
            BookmarkRequest request = new BookmarkRequest(collectionId, "My notes");

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
            BookmarkRequest request = new BookmarkRequest(collectionId, "My notes");

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

        @Test
        @DisplayName("should throw exception when post not found")
        void deleteBookmark_postNotFound_throwsException() {
            when(postRepository.findById(postId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookmarkService.deleteBookmark(postId, userId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Post not found");

            verify(bookmarkRepository, never()).deleteByUserAndPost(any(), any());
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void deleteBookmark_userNotFound_throwsException() {
            when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookmarkService.deleteBookmark(postId, userId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("User not found");

            verify(bookmarkRepository, never()).deleteByUserAndPost(any(), any());
        }
    }

    @Nested
    @DisplayName("updateBookmark()")
    class UpdateBookmarkTests {

        @Test
        @DisplayName("should update bookmark successfully")
        void updateBookmark_success() {
            BookmarkRequest request = new BookmarkRequest(null, "Updated notes");
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
            BookmarkRequest request = new BookmarkRequest(null, "Updated notes");

            when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(bookmarkRepository.findByUserAndPost(testUser, testPost)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookmarkService.updateBookmark(postId, userId, request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Bookmark not found");
        }

        @Test
        @DisplayName("should throw exception when collection not found")
        void updateBookmark_collectionNotFound_throwsException() {
            UUID collectionId = UUID.randomUUID();
            BookmarkRequest request = new BookmarkRequest(collectionId, "Notes");
            PostBookmark bookmark = new PostBookmark(testUser, testPost, null, "Old notes");
            setField(bookmark, "id", UUID.randomUUID());
            setField(bookmark, "createdAt", LocalDateTime.now());

            when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(bookmarkRepository.findByUserAndPost(testUser, testPost)).thenReturn(Optional.of(bookmark));
            when(collectionRepository.findById(collectionId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookmarkService.updateBookmark(postId, userId, request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Collection not found");
        }

        @Test
        @DisplayName("should throw exception when collection belongs to another user")
        void updateBookmark_collectionForbidden_throwsException() {
            UUID collectionId = UUID.randomUUID();
            User otherUser = createTestUser(UUID.randomUUID(), "other", "other@example.com");
            BookmarkCollection collection = new BookmarkCollection(otherUser, "Other's Collection", null, null, null);
            setField(collection, "id", collectionId);

            BookmarkRequest request = new BookmarkRequest(collectionId, "Notes");
            PostBookmark bookmark = new PostBookmark(testUser, testPost, null, "Old notes");
            setField(bookmark, "id", UUID.randomUUID());
            setField(bookmark, "createdAt", LocalDateTime.now());

            when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(bookmarkRepository.findByUserAndPost(testUser, testPost)).thenReturn(Optional.of(bookmark));
            when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));

            assertThatThrownBy(() -> bookmarkService.updateBookmark(postId, userId, request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Cannot add to another user's collection");
        }

        @Test
        @DisplayName("should update bookmark with collection")
        void updateBookmark_withCollection_success() {
            UUID collectionId = UUID.randomUUID();
            BookmarkCollection collection = new BookmarkCollection(testUser, "My Collection", null, null, null);
            setField(collection, "id", collectionId);

            BookmarkRequest request = new BookmarkRequest(collectionId, "Updated notes");
            PostBookmark bookmark = new PostBookmark(testUser, testPost, null, "Old notes");
            setField(bookmark, "id", UUID.randomUUID());
            setField(bookmark, "createdAt", LocalDateTime.now());

            when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(bookmarkRepository.findByUserAndPost(testUser, testPost)).thenReturn(Optional.of(bookmark));
            when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));
            when(bookmarkRepository.save(any(PostBookmark.class))).thenReturn(bookmark);

            BookmarkResponse result = bookmarkService.updateBookmark(postId, userId, request);

            assertThat(result).isNotNull();
            verify(bookmarkRepository).save(argThat(bm -> bm.getCollection() == collection));
        }

        @Test
        @DisplayName("should throw exception when post not found")
        void updateBookmark_postNotFound_throwsException() {
            BookmarkRequest request = new BookmarkRequest(null, "Notes");

            when(postRepository.findById(postId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookmarkService.updateBookmark(postId, userId, request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Post not found");
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void updateBookmark_userNotFound_throwsException() {
            BookmarkRequest request = new BookmarkRequest(null, "Notes");

            when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookmarkService.updateBookmark(postId, userId, request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("User not found");
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

        @Test
        @DisplayName("should throw exception when user not found")
        void getUserBookmarks_userNotFound_throwsException() {
            Pageable pageable = PageRequest.of(0, 10);

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookmarkService.getUserBookmarks(userId, pageable))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("User not found");
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

        @Test
        @DisplayName("should throw exception when user not found")
        void createCollection_userNotFound_throwsException() {
            CollectionCreateRequest request = new CollectionCreateRequest("My Collection", "Description", "#FF0000", "bookmark");

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookmarkService.createCollection(userId, request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("User not found");
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

        @Test
        @DisplayName("should throw exception when user not found")
        void getUserCollections_userNotFound_throwsException() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookmarkService.getUserCollections(userId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("getCollectionBookmarks()")
    class GetCollectionBookmarksTests {

        @Test
        @DisplayName("should return bookmarks for collection")
        void getCollectionBookmarks_success() {
            UUID collectionId = UUID.randomUUID();
            BookmarkCollection collection = new BookmarkCollection(testUser, "My Collection", null, null, null);
            setField(collection, "id", collectionId);

            PostBookmark bookmark = new PostBookmark(testUser, testPost, collection, "Notes");
            setField(bookmark, "id", UUID.randomUUID());
            setField(bookmark, "createdAt", LocalDateTime.now());

            Pageable pageable = PageRequest.of(0, 10);
            Page<PostBookmark> bookmarkPage = new PageImpl<>(List.of(bookmark), pageable, 1);

            when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));
            when(bookmarkRepository.findByUserAndCollectionOrderByCreatedAtDesc(testUser, collection, pageable))
                    .thenReturn(bookmarkPage);

            Page<BookmarkResponse> result = bookmarkService.getCollectionBookmarks(collectionId, userId, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("should throw exception when collection not found")
        void getCollectionBookmarks_collectionNotFound_throwsException() {
            UUID collectionId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 10);

            when(collectionRepository.findById(collectionId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookmarkService.getCollectionBookmarks(collectionId, userId, pageable))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Collection not found");
        }

        @Test
        @DisplayName("should throw exception when accessing another user's collection")
        void getCollectionBookmarks_notOwner_throwsException() {
            UUID collectionId = UUID.randomUUID();
            User otherUser = createTestUser(UUID.randomUUID(), "other", "other@example.com");
            BookmarkCollection collection = new BookmarkCollection(otherUser, "Other's Collection", null, null, null);
            setField(collection, "id", collectionId);

            Pageable pageable = PageRequest.of(0, 10);

            when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));

            assertThatThrownBy(() -> bookmarkService.getCollectionBookmarks(collectionId, userId, pageable))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Cannot access another user's collection");
        }
    }

    @Nested
    @DisplayName("getUncategorizedBookmarks()")
    class GetUncategorizedBookmarksTests {

        @Test
        @DisplayName("should return uncategorized bookmarks")
        void getUncategorizedBookmarks_success() {
            PostBookmark bookmark = new PostBookmark(testUser, testPost, null, "Notes");
            setField(bookmark, "id", UUID.randomUUID());
            setField(bookmark, "createdAt", LocalDateTime.now());

            Pageable pageable = PageRequest.of(0, 10);
            Page<PostBookmark> bookmarkPage = new PageImpl<>(List.of(bookmark), pageable, 1);

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(bookmarkRepository.findByUserAndCollectionIsNullOrderByCreatedAtDesc(testUser, pageable))
                    .thenReturn(bookmarkPage);

            Page<BookmarkResponse> result = bookmarkService.getUncategorizedBookmarks(userId, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("should return empty page when no uncategorized bookmarks")
        void getUncategorizedBookmarks_empty() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<PostBookmark> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(bookmarkRepository.findByUserAndCollectionIsNullOrderByCreatedAtDesc(testUser, pageable))
                    .thenReturn(emptyPage);

            Page<BookmarkResponse> result = bookmarkService.getUncategorizedBookmarks(userId, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void getUncategorizedBookmarks_userNotFound_throwsException() {
            Pageable pageable = PageRequest.of(0, 10);

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookmarkService.getUncategorizedBookmarks(userId, pageable))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("updateCollection()")
    class UpdateCollectionTests {

        @Test
        @DisplayName("should update collection successfully")
        void updateCollection_success() {
            UUID collectionId = UUID.randomUUID();
            BookmarkCollection collection = new BookmarkCollection(testUser, "Old Name", "Old Desc", "#000000", "old-icon");
            setField(collection, "id", collectionId);
            setField(collection, "createdAt", LocalDateTime.now());

            CollectionCreateRequest request = new CollectionCreateRequest("New Name", "New Desc", "#FF0000", "new-icon");

            when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));
            when(collectionRepository.findByUserAndName(testUser, "New Name")).thenReturn(Optional.empty());
            when(collectionRepository.save(any(BookmarkCollection.class))).thenReturn(collection);
            when(bookmarkRepository.countByCollection(collection)).thenReturn(0L);

            BookmarkCollectionResponse result = bookmarkService.updateCollection(collectionId, userId, request);

            assertThat(result).isNotNull();
            verify(collectionRepository).save(collection);
        }

        @Test
        @DisplayName("should allow updating with same name")
        void updateCollection_sameName_success() {
            UUID collectionId = UUID.randomUUID();
            BookmarkCollection collection = new BookmarkCollection(testUser, "My Collection", "Old Desc", "#000000", "old-icon");
            setField(collection, "id", collectionId);
            setField(collection, "createdAt", LocalDateTime.now());

            CollectionCreateRequest request = new CollectionCreateRequest("My Collection", "New Desc", "#FF0000", "new-icon");

            when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));
            when(collectionRepository.findByUserAndName(testUser, "My Collection")).thenReturn(Optional.of(collection));
            when(collectionRepository.save(any(BookmarkCollection.class))).thenReturn(collection);
            when(bookmarkRepository.countByCollection(collection)).thenReturn(0L);

            BookmarkCollectionResponse result = bookmarkService.updateCollection(collectionId, userId, request);

            assertThat(result).isNotNull();
            verify(collectionRepository).save(collection);
        }

        @Test
        @DisplayName("should throw exception when collection not found")
        void updateCollection_notFound_throwsException() {
            UUID collectionId = UUID.randomUUID();
            CollectionCreateRequest request = new CollectionCreateRequest("New Name", null, null, null);

            when(collectionRepository.findById(collectionId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookmarkService.updateCollection(collectionId, userId, request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Collection not found");
        }

        @Test
        @DisplayName("should throw exception when updating another user's collection")
        void updateCollection_forbidden_throwsException() {
            UUID collectionId = UUID.randomUUID();
            User otherUser = createTestUser(UUID.randomUUID(), "other", "other@example.com");
            BookmarkCollection collection = new BookmarkCollection(otherUser, "Other's Collection", null, null, null);
            setField(collection, "id", collectionId);

            CollectionCreateRequest request = new CollectionCreateRequest("New Name", null, null, null);

            when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));

            assertThatThrownBy(() -> bookmarkService.updateCollection(collectionId, userId, request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Cannot update another user's collection");
        }

        @Test
        @DisplayName("should throw exception when name already exists for another collection")
        void updateCollection_nameConflict_throwsException() {
            UUID collectionId = UUID.randomUUID();
            UUID existingCollectionId = UUID.randomUUID();

            BookmarkCollection collection = new BookmarkCollection(testUser, "My Collection", null, null, null);
            setField(collection, "id", collectionId);

            BookmarkCollection existingCollection = new BookmarkCollection(testUser, "Existing Name", null, null, null);
            setField(existingCollection, "id", existingCollectionId);

            CollectionCreateRequest request = new CollectionCreateRequest("Existing Name", null, null, null);

            when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));
            when(collectionRepository.findByUserAndName(testUser, "Existing Name")).thenReturn(Optional.of(existingCollection));

            assertThatThrownBy(() -> bookmarkService.updateCollection(collectionId, userId, request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Collection with this name already exists");
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
