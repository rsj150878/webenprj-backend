package at.fhtw.webenprjbackend.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import at.fhtw.webenprjbackend.dto.BookmarkCollectionResponse;
import at.fhtw.webenprjbackend.dto.BookmarkCreateResult;
import at.fhtw.webenprjbackend.dto.BookmarkRequest;
import at.fhtw.webenprjbackend.dto.BookmarkResponse;
import at.fhtw.webenprjbackend.dto.CollectionCreateRequest;
import at.fhtw.webenprjbackend.entity.BookmarkCollection;
import at.fhtw.webenprjbackend.entity.Post;
import at.fhtw.webenprjbackend.entity.PostBookmark;
import at.fhtw.webenprjbackend.entity.User;
import at.fhtw.webenprjbackend.repository.BookmarkCollectionRepository;
import at.fhtw.webenprjbackend.repository.PostBookmarkRepository;
import at.fhtw.webenprjbackend.repository.PostRepository;
import at.fhtw.webenprjbackend.repository.UserRepository;

/**
 * Service layer for bookmark and collection management.
 * Implements idempotent operations following the PostLike pattern.
 * Includes bulk query methods to prevent N+1 problems.
 */
@Service
@Transactional(readOnly = true)
public class BookmarkService {

    private final PostBookmarkRepository bookmarkRepository;
    private final BookmarkCollectionRepository collectionRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /** Constructor with DI. */
    public BookmarkService(
        PostBookmarkRepository bookmarkRepository,
        BookmarkCollectionRepository collectionRepository,
        PostRepository postRepository,
        UserRepository userRepository
    ) {
        this.bookmarkRepository = bookmarkRepository;
        this.collectionRepository = collectionRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    // ========== Bookmark Operations ==========

    /** Create a bookmark (idempotent - returns existing if duplicate) */
    @Transactional
    public BookmarkCreateResult createBookmark(UUID postId, UUID userId, BookmarkRequest request) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Idempotent - return existing if already bookmarked
        Optional<PostBookmark> existing = bookmarkRepository.findByUserAndPost(user, post);
        if (existing.isPresent()) {
            return new BookmarkCreateResult(mapToBookmarkResponse(existing.get()), false);
        }

        BookmarkCollection collection = null;
        if (request.collectionId() != null) {
            collection = collectionRepository.findById(request.collectionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found"));

            // Verify ownership
            if (!collection.getUser().getId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot add to another user's collection");
            }
        }

        PostBookmark bookmark = new PostBookmark(user, post, collection, request.notes());
        PostBookmark saved = bookmarkRepository.save(bookmark);
        return new BookmarkCreateResult(mapToBookmarkResponse(saved), true);
    }

    /**
     * Delete a bookmark (idempotent)
     */
    @Transactional
    public void deleteBookmark(UUID postId, UUID userId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        bookmarkRepository.deleteByUserAndPost(user, post);
    }

    /**
     * Update bookmark metadata (move to collection, update notes)
     */
    @Transactional
    public BookmarkResponse updateBookmark(UUID postId, UUID userId, BookmarkRequest request) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        PostBookmark bookmark = bookmarkRepository.findByUserAndPost(user, post)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bookmark not found"));

        BookmarkCollection collection = null;
        if (request.collectionId() != null) {
            collection = collectionRepository.findById(request.collectionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found"));

            if (!collection.getUser().getId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot add to another user's collection");
            }
        }

        bookmark.setCollection(collection);
        bookmark.setNotes(request.notes());
        PostBookmark saved = bookmarkRepository.save(bookmark);
        return mapToBookmarkResponse(saved);
    }

    /**
     * Get all bookmarks for a user
     */
    public Page<BookmarkResponse> getUserBookmarks(UUID userId, Pageable pageable) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Page<PostBookmark> bookmarks = bookmarkRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return bookmarks.map(this::mapToBookmarkResponse);
    }

    /**
     * Get bookmarks in a specific collection
     */
    public Page<BookmarkResponse> getCollectionBookmarks(UUID collectionId, UUID userId, Pageable pageable) {
        BookmarkCollection collection = collectionRepository.findById(collectionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found"));

        if (!collection.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot access another user's collection");
        }

        Page<PostBookmark> bookmarks = bookmarkRepository.findByUserAndCollectionOrderByCreatedAtDesc(
            collection.getUser(), collection, pageable);
        return bookmarks.map(this::mapToBookmarkResponse);
    }

    /**
     * Get uncategorized bookmarks
     */
    public Page<BookmarkResponse> getUncategorizedBookmarks(UUID userId, Pageable pageable) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Page<PostBookmark> bookmarks = bookmarkRepository.findByUserAndCollectionIsNullOrderByCreatedAtDesc(
            user, pageable);
        return bookmarks.map(this::mapToBookmarkResponse);
    }

    // ========== Collection Operations ==========

    /**
     * Create a collection
     */
    @Transactional
    public BookmarkCollectionResponse createCollection(UUID userId, CollectionCreateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (collectionRepository.existsByUserAndName(user, request.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Collection with this name already exists");
        }

        BookmarkCollection collection = new BookmarkCollection(
            user, request.name(), request.description(), request.color(), request.iconName());
        BookmarkCollection saved = collectionRepository.save(collection);
        return mapToCollectionResponse(saved);
    }

    /**
     * Update a collection
     */
    @Transactional
    public BookmarkCollectionResponse updateCollection(UUID collectionId, UUID userId, CollectionCreateRequest request) {
        BookmarkCollection collection = collectionRepository.findById(collectionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found"));

        if (!collection.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot update another user's collection");
        }

        // Check name uniqueness (excluding self)
        Optional<BookmarkCollection> existing = collectionRepository.findByUserAndName(
            collection.getUser(), request.name());
        if (existing.isPresent() && !existing.get().getId().equals(collectionId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Collection with this name already exists");
        }

        collection.setName(request.name());
        collection.setDescription(request.description());
        collection.setColor(request.color());
        collection.setIconName(request.iconName());

        BookmarkCollection saved = collectionRepository.save(collection);
        return mapToCollectionResponse(saved);
    }

    /**
     * Delete a collection (bookmarks become uncategorized)
     */
    @Transactional
    public void deleteCollection(UUID collectionId, UUID userId) {
        BookmarkCollection collection = collectionRepository.findById(collectionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found"));

        if (!collection.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete another user's collection");
        }

        // Bookmarks will have collection_id set to NULL (ON DELETE SET NULL)
        collectionRepository.delete(collection);
    }

    /**
     * Get all collections for a user
     */
    public List<BookmarkCollectionResponse> getUserCollections(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<BookmarkCollection> collections = collectionRepository.findByUserOrderByCreatedAtAsc(user);
        return collections.stream()
            .map(this::mapToCollectionResponse)
            .toList();
    }

    // ========== Bulk Queries for PostService Integration ==========

    /**
     * Fetch bookmark counts for multiple posts (prevents N+1 queries)
     */
    public Map<UUID, Long> fetchBookmarkCounts(List<Post> posts) {
        if (posts.isEmpty()) {
            return Map.of();
        }
        List<UUID> postIds = posts.stream().map(Post::getId).toList();
        return bookmarkRepository.countBookmarksByPostIds(postIds).stream()
            .collect(Collectors.toMap(
                row -> (UUID) row[0],
                row -> (Long) row[1]
            ));
    }

    /**
     * Fetch bookmarked post IDs for current user (prevents N+1 queries)
     */
    public Set<UUID> fetchBookmarkedPostIds(List<Post> posts, UUID currentUserId) {
        if (currentUserId == null || posts.isEmpty()) {
            return Set.of();
        }
        List<UUID> postIds = posts.stream().map(Post::getId).toList();
        return Set.copyOf(bookmarkRepository.findBookmarkedPostIds(currentUserId, postIds));
    }

    // ========== Private Mapping Methods ==========

    private BookmarkResponse mapToBookmarkResponse(PostBookmark bookmark) {
        // Create a minimal PostResponse for the bookmark
        Post post = bookmark.getPost();

        BookmarkCollectionResponse collection = bookmark.getCollection() != null
            ? mapToCollectionResponse(bookmark.getCollection())
            : null;

        return new BookmarkResponse(
            bookmark.getId(),
            createMinimalPostResponse(post),
            collection,
            bookmark.getNotes(),
            bookmark.getCreatedAt()
        );
    }

    private BookmarkCollectionResponse mapToCollectionResponse(BookmarkCollection collection) {
        long bookmarkCount = bookmarkRepository.countByCollection(collection);
        return new BookmarkCollectionResponse(
            collection.getId(),
            collection.getName(),
            collection.getDescription(),
            collection.getColor(),
            collection.getIconName(),
            bookmarkCount,
            collection.getCreatedAt(),
            collection.getUpdatedAt()
        );
    }

    /**
     * Create a minimal PostResponse without fetching like/bookmark/comment counts
     * This is used in BookmarkResponse to avoid circular dependencies
     */
    private at.fhtw.webenprjbackend.dto.PostResponse createMinimalPostResponse(Post post) {
        return new at.fhtw.webenprjbackend.dto.PostResponse(
            post.getId(),
            post.getParent() != null ? post.getParent().getId() : null, // parentId
            0L, // commentCount - not fetched in bookmark context
            post.getParent() != null && !post.getParent().isActive(), // parentDeleted
            "#" + post.getSubject(),
            post.getContent(),
            post.getImageUrl(),
            post.getCreatedAt(),
            post.getUpdatedAt(),
            post.getUser().getId(),
            post.getUser().getUsername(),
            post.getUser().getProfileImageUrl(),
            0L, // likeCount - not fetched in bookmark context
            false, // likedByCurrentUser - not fetched in bookmark context
            0L, // bookmarkCount - not fetched in bookmark context
            false // bookmarkedByCurrentUser - always false in bookmark list (we already know it's bookmarked)
        );
    }
}
