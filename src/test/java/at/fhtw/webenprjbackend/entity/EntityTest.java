package at.fhtw.webenprjbackend.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for entity classes.
 */
@DisplayName("Entity Tests")
class EntityTest {

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
    @DisplayName("Post Entity")
    class PostEntityTests {

        @Test
        @DisplayName("isComment() should return true when parent is set")
        void isComment_withParent_returnsTrue() {
            // Arrange
            User user = new User("test@example.com", "testuser", "password", "AT",
                    "https://example.com/profile.png", Role.USER);
            Post parent = new Post("subject", "Parent content", null, user);
            Post comment = new Post("reply", "Reply content", null, user);
            setField(comment, "parent", parent);

            // Act & Assert
            assertThat(comment.isComment()).isTrue();
        }

        @Test
        @DisplayName("isComment() should return false when parent is null")
        void isComment_withoutParent_returnsFalse() {
            // Arrange
            User user = new User("test@example.com", "testuser", "password", "AT",
                    "https://example.com/profile.png", Role.USER);
            Post post = new Post("subject", "Content", null, user);

            // Act & Assert
            assertThat(post.isComment()).isFalse();
        }

        @Test
        @DisplayName("isDeleted() should return true when active is false")
        void isDeleted_whenInactive_returnsTrue() {
            // Arrange
            User user = new User("test@example.com", "testuser", "password", "AT",
                    "https://example.com/profile.png", Role.USER);
            Post post = new Post("subject", "Content", null, user);
            post.setActive(false);

            // Act & Assert
            assertThat(post.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("isDeleted() should return false when active is true")
        void isDeleted_whenActive_returnsFalse() {
            // Arrange
            User user = new User("test@example.com", "testuser", "password", "AT",
                    "https://example.com/profile.png", Role.USER);
            Post post = new Post("subject", "Content", null, user);
            // active defaults to true

            // Act & Assert
            assertThat(post.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("setImageUrl() should set image URL")
        void setImageUrl_setsValue() {
            // Arrange
            User user = new User("test@example.com", "testuser", "password", "AT",
                    "https://example.com/profile.png", Role.USER);
            Post post = new Post("subject", "Content", null, user);

            // Act
            post.setImageUrl("https://example.com/image.png");

            // Assert
            assertThat(post.getImageUrl()).isEqualTo("https://example.com/image.png");
        }

        @Test
        @DisplayName("setUser() should set user")
        void setUser_setsValue() {
            // Arrange
            User user1 = new User("user1@example.com", "user1", "password", "AT",
                    "https://example.com/profile.png", Role.USER);
            User user2 = new User("user2@example.com", "user2", "password", "DE",
                    "https://example.com/profile2.png", Role.USER);
            Post post = new Post("subject", "Content", null, user1);

            // Act
            post.setUser(user2);

            // Assert
            assertThat(post.getUser()).isEqualTo(user2);
        }

        @Test
        @DisplayName("setComments() should set comments list")
        void setComments_setsValue() {
            // Arrange
            User user = new User("test@example.com", "testuser", "password", "AT",
                    "https://example.com/profile.png", Role.USER);
            Post post = new Post("subject", "Content", null, user);
            Post comment = new Post("reply", "Reply content", null, user);
            List<Post> comments = new ArrayList<>();
            comments.add(comment);

            // Act
            post.setComments(comments);

            // Assert
            assertThat(post.getComments()).hasSize(1);
            assertThat(post.getComments().get(0)).isEqualTo(comment);
        }

        @Test
        @DisplayName("getComments() should return comments list")
        void getComments_returnsValue() {
            // Arrange
            User user = new User("test@example.com", "testuser", "password", "AT",
                    "https://example.com/profile.png", Role.USER);
            Post post = new Post("subject", "Content", null, user);

            // Act & Assert
            assertThat(post.getComments()).isNotNull();
            assertThat(post.getComments()).isEmpty();
        }

        @Test
        @DisplayName("no-arg constructor should create empty post")
        void noArgConstructor_createsPost() {
            // Act
            Post post = new Post();

            // Assert
            assertThat(post).isNotNull();
            assertThat(post.getId()).isNull();
            assertThat(post.getSubject()).isNull();
        }

        @Test
        @DisplayName("constructor should set all fields")
        void constructor_setsAllFields() {
            // Arrange
            User user = new User("test@example.com", "testuser", "password", "AT",
                    "https://example.com/profile.png", Role.USER);

            // Act
            Post post = new Post("webdev", "Learning web development", "https://image.com/pic.png", user);

            // Assert
            assertThat(post.getSubject()).isEqualTo("webdev");
            assertThat(post.getContent()).isEqualTo("Learning web development");
            assertThat(post.getImageUrl()).isEqualTo("https://image.com/pic.png");
            assertThat(post.getUser()).isEqualTo(user);
            assertThat(post.isActive()).isTrue(); // default value
        }
    }

    @Nested
    @DisplayName("User Entity")
    class UserEntityTests {

        @Test
        @DisplayName("no-arg constructor should create empty user")
        void noArgConstructor_createsUser() {
            // Act
            User user = new User();

            // Assert
            assertThat(user).isNotNull();
            assertThat(user.getId()).isNull();
        }

        @Test
        @DisplayName("6-arg constructor should set fields and active=true")
        void sixArgConstructor_setsFieldsAndActive() {
            // Act
            User user = new User(
                    "test@example.com",
                    "testuser",
                    "password123",
                    "AT",
                    "https://example.com/profile.png",
                    Role.USER
            );

            // Assert
            assertThat(user.getEmail()).isEqualTo("test@example.com");
            assertThat(user.getUsername()).isEqualTo("testuser");
            assertThat(user.getPassword()).isEqualTo("password123");
            assertThat(user.getCountryCode()).isEqualTo("AT");
            assertThat(user.getProfileImageUrl()).isEqualTo("https://example.com/profile.png");
            assertThat(user.getRole()).isEqualTo(Role.USER);
            assertThat(user.isActive()).isTrue();
            assertThat(user.getSalutation()).isNull();
        }

        @Test
        @DisplayName("7-arg constructor should set all fields including salutation")
        void sevenArgConstructor_setsAllFieldsIncludingSalutation() {
            // Act
            User user = new User(
                    "test@example.com",
                    "testuser",
                    "password123",
                    "AT",
                    "https://example.com/profile.png",
                    "Dr.",
                    Role.ADMIN
            );

            // Assert
            assertThat(user.getEmail()).isEqualTo("test@example.com");
            assertThat(user.getUsername()).isEqualTo("testuser");
            assertThat(user.getPassword()).isEqualTo("password123");
            assertThat(user.getCountryCode()).isEqualTo("AT");
            assertThat(user.getProfileImageUrl()).isEqualTo("https://example.com/profile.png");
            assertThat(user.getSalutation()).isEqualTo("Dr.");
            assertThat(user.getRole()).isEqualTo(Role.ADMIN);
            assertThat(user.isActive()).isTrue();
        }

        @Test
        @DisplayName("toString() should not include password")
        void toString_doesNotIncludePassword() {
            // Arrange
            User user = new User(
                    "test@example.com",
                    "testuser",
                    "secretPassword123",
                    "AT",
                    "https://example.com/profile.png",
                    Role.USER
            );
            setField(user, "id", UUID.randomUUID());

            // Act
            String result = user.toString();

            // Assert
            assertThat(result).doesNotContain("secretPassword123");
            assertThat(result).doesNotContain("password");
            assertThat(result).contains("email='test@example.com'");
            assertThat(result).contains("username='testuser'");
            assertThat(result).contains("role=USER");
        }

        @Test
        @DisplayName("setters should update fields")
        void setters_updateFields() {
            // Arrange
            User user = new User();

            // Act
            user.setEmail("new@example.com");
            user.setUsername("newuser");
            user.setPassword("newpassword");
            user.setCountryCode("DE");
            user.setProfileImageUrl("https://new.com/pic.png");
            user.setSalutation("Prof.");
            user.setRole(Role.ADMIN);
            user.setActive(false);

            // Assert
            assertThat(user.getEmail()).isEqualTo("new@example.com");
            assertThat(user.getUsername()).isEqualTo("newuser");
            assertThat(user.getPassword()).isEqualTo("newpassword");
            assertThat(user.getCountryCode()).isEqualTo("DE");
            assertThat(user.getProfileImageUrl()).isEqualTo("https://new.com/pic.png");
            assertThat(user.getSalutation()).isEqualTo("Prof.");
            assertThat(user.getRole()).isEqualTo(Role.ADMIN);
            assertThat(user.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("BookmarkCollection Entity")
    class BookmarkCollectionEntityTests {

        @Test
        @DisplayName("3-arg constructor should set basic fields")
        void threeArgConstructor_setsBasicFields() {
            // Arrange
            User user = new User("test@example.com", "testuser", "password", "AT",
                    "https://example.com/profile.png", Role.USER);

            // Act
            BookmarkCollection collection = new BookmarkCollection(user, "Study Tips", "My favorite study tips");

            // Assert
            assertThat(collection.getUser()).isEqualTo(user);
            assertThat(collection.getName()).isEqualTo("Study Tips");
            assertThat(collection.getDescription()).isEqualTo("My favorite study tips");
            assertThat(collection.getColor()).isNull();
            assertThat(collection.getIconName()).isNull();
        }

        @Test
        @DisplayName("5-arg constructor should set all fields")
        void fiveArgConstructor_setsAllFields() {
            // Arrange
            User user = new User("test@example.com", "testuser", "password", "AT",
                    "https://example.com/profile.png", Role.USER);

            // Act
            BookmarkCollection collection = new BookmarkCollection(
                    user, "Math Finals", "Math study resources", "#FF5733", "calculator"
            );

            // Assert
            assertThat(collection.getUser()).isEqualTo(user);
            assertThat(collection.getName()).isEqualTo("Math Finals");
            assertThat(collection.getDescription()).isEqualTo("Math study resources");
            assertThat(collection.getColor()).isEqualTo("#FF5733");
            assertThat(collection.getIconName()).isEqualTo("calculator");
        }

        @Test
        @DisplayName("no-arg constructor should create empty collection")
        void noArgConstructor_createsCollection() {
            // Act
            BookmarkCollection collection = new BookmarkCollection();

            // Assert
            assertThat(collection).isNotNull();
            assertThat(collection.getId()).isNull();
            assertThat(collection.getName()).isNull();
        }

        @Test
        @DisplayName("setters should update fields")
        void setters_updateFields() {
            // Arrange
            User user = new User("test@example.com", "testuser", "password", "AT",
                    "https://example.com/profile.png", Role.USER);
            BookmarkCollection collection = new BookmarkCollection();

            // Act
            collection.setUser(user);
            collection.setName("Updated Name");
            collection.setDescription("Updated Description");
            collection.setColor("#00FF00");
            collection.setIconName("book");

            // Assert
            assertThat(collection.getUser()).isEqualTo(user);
            assertThat(collection.getName()).isEqualTo("Updated Name");
            assertThat(collection.getDescription()).isEqualTo("Updated Description");
            assertThat(collection.getColor()).isEqualTo("#00FF00");
            assertThat(collection.getIconName()).isEqualTo("book");
        }
    }

    @Nested
    @DisplayName("PostBookmark Entity")
    class PostBookmarkEntityTests {

        @Test
        @DisplayName("2-arg constructor should set user and post only")
        void twoArgConstructor_setsUserAndPost() {
            // Arrange
            User user = new User("test@example.com", "testuser", "password", "AT",
                    "https://example.com/profile.png", Role.USER);
            Post post = new Post("subject", "Content", null, user);

            // Act
            PostBookmark bookmark = new PostBookmark(user, post);

            // Assert
            assertThat(bookmark.getUser()).isEqualTo(user);
            assertThat(bookmark.getPost()).isEqualTo(post);
            assertThat(bookmark.getCollection()).isNull();
            assertThat(bookmark.getNotes()).isNull();
        }

        @Test
        @DisplayName("4-arg constructor should set all fields")
        void fourArgConstructor_setsAllFields() {
            // Arrange
            User user = new User("test@example.com", "testuser", "password", "AT",
                    "https://example.com/profile.png", Role.USER);
            Post post = new Post("subject", "Content", null, user);
            BookmarkCollection collection = new BookmarkCollection(user, "Study Tips", "Tips");

            // Act
            PostBookmark bookmark = new PostBookmark(user, post, collection, "Important for exam!");

            // Assert
            assertThat(bookmark.getUser()).isEqualTo(user);
            assertThat(bookmark.getPost()).isEqualTo(post);
            assertThat(bookmark.getCollection()).isEqualTo(collection);
            assertThat(bookmark.getNotes()).isEqualTo("Important for exam!");
        }

        @Test
        @DisplayName("no-arg constructor should create empty bookmark")
        void noArgConstructor_createsBookmark() {
            // Act
            PostBookmark bookmark = new PostBookmark();

            // Assert
            assertThat(bookmark).isNotNull();
            assertThat(bookmark.getId()).isNull();
            assertThat(bookmark.getUser()).isNull();
            assertThat(bookmark.getPost()).isNull();
        }

        @Test
        @DisplayName("setters should update fields")
        void setters_updateFields() {
            // Arrange
            User user = new User("test@example.com", "testuser", "password", "AT",
                    "https://example.com/profile.png", Role.USER);
            Post post = new Post("subject", "Content", null, user);
            BookmarkCollection collection = new BookmarkCollection(user, "Study Tips", "Tips");
            PostBookmark bookmark = new PostBookmark();

            // Act
            bookmark.setUser(user);
            bookmark.setPost(post);
            bookmark.setCollection(collection);
            bookmark.setNotes("My notes");

            // Assert
            assertThat(bookmark.getUser()).isEqualTo(user);
            assertThat(bookmark.getPost()).isEqualTo(post);
            assertThat(bookmark.getCollection()).isEqualTo(collection);
            assertThat(bookmark.getNotes()).isEqualTo("My notes");
        }
    }

    @Nested
    @DisplayName("Role Enum")
    class RoleEnumTests {

        @Test
        @DisplayName("should have USER and ADMIN values")
        void role_hasExpectedValues() {
            // Assert
            assertThat(Role.values()).containsExactlyInAnyOrder(Role.USER, Role.ADMIN);
        }

        @Test
        @DisplayName("valueOf should return correct role")
        void valueOf_returnsCorrectRole() {
            // Assert
            assertThat(Role.valueOf("USER")).isEqualTo(Role.USER);
            assertThat(Role.valueOf("ADMIN")).isEqualTo(Role.ADMIN);
        }
    }
}
