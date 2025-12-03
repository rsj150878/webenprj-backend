/**
 * JPA entity classes representing the domain model of the Motivise platform.
 *
 * <p>This package contains all persistent entities that map to database tables.
 * Entities define the core domain model and relationships, managed by Hibernate/JPA.
 *
 * <p><b>Entities:</b>
 * <ul>
 *   <li>{@link at.fhtw.webenprjbackend.entity.User}
 *       - Registered users with authentication and profile data</li>
 *   <li>{@link at.fhtw.webenprjbackend.entity.Post}
 *       - Study posts created by users for sharing learning progress</li>
 *   <li>{@link at.fhtw.webenprjbackend.entity.Media}
 *       - File metadata for uploaded images and attachments</li>
 *   <li>{@link at.fhtw.webenprjbackend.entity.Role}
 *       - Enum defining user roles (USER, ADMIN)</li>
 * </ul>
 *
 * <p><b>Database Schema:</b>
 * <ul>
 *   <li><b>users:</b> User accounts, authentication, profile information</li>
 *   <li><b>posts:</b> Study posts with subjects, content, and image URLs</li>
 *   <li><b>media:</b> Metadata for uploaded files (future feature)</li>
 *   <li><b>Relationships:</b> Post N:1 User (many posts per user)</li>
 * </ul>
 *
 * <p><b>Entity Design Patterns:</b>
 * <ul>
 *   <li><b>UUID Primary Keys:</b> All entities use UUID for globally unique, non-sequential IDs</li>
 *   <li><b>Automatic Timestamps:</b> {@code @CreationTimestamp} and {@code @UpdateTimestamp}
 *       for audit trail</li>
 *   <li><b>Enum Types:</b> Roles stored as strings ({@code @Enumerated(EnumType.STRING)})
 *       for database readability</li>
 *   <li><b>Validation Separation:</b> Database constraints enforced via JPA annotations,
 *       input validation handled by DTOs</li>
 *   <li><b>No Business Logic:</b> Entities are pure data holders - no complex methods,
 *       just getters/setters</li>
 * </ul>
 *
 * <p><b>Database Migration:</b>
 * <ul>
 *   <li>Schema managed by Flyway migration scripts in {@code src/main/resources/db/migration/}</li>
 *   <li>Entities must match migration definitions</li>
 *   <li>Never modify existing migrations - create new ones for schema changes</li>
 *   <li>Version format: {@code V{version}__{description}.sql} (e.g., {@code V1__create_users_table.sql})</li>
 * </ul>
 *
 * <p><b>Relationships:</b>
 * <ul>
 *   <li><b>Post -> User:</b> {@code @ManyToOne} - Each post has exactly one author
 *       <ul>
 *         <li>Fetch: Eager (default) - user info often needed with posts</li>
 *         <li>Cascade: None - user deletion handled separately</li>
 *         <li>Optional: false - posts must have an author</li>
 *       </ul>
 *   </li>
 * </ul>
 *
 * <p><b>Auditing:</b>
 * <ul>
 *   <li>All entities have {@code createdAt} and {@code updatedAt} timestamps</li>
 *   <li>Automatically managed by Hibernate annotations</li>
 *   <li>{@code createdAt} is immutable ({@code updatable = false})</li>
 *   <li>{@code updatedAt} changes on every save</li>
 * </ul>
 *
 * <p><b>Future Enhancements:</b>
 * <ul>
 *   <li><b>OffsetDateTime:</b> Consider migrating from LocalDateTime for timezone support</li>
 *   <li><b>Soft Deletion:</b> Add {@code deletedAt} timestamp for soft deletes</li>
 *   <li><b>Auditing:</b> Implement {@code createdBy} and {@code modifiedBy} fields</li>
 *   <li><b>Versioning:</b> Add {@code @Version} for optimistic locking</li>
 * </ul>
 *
 * @see at.fhtw.webenprjbackend.repository
 * @see at.fhtw.webenprjbackend.service
 * @see at.fhtw.webenprjbackend.dto
 */
package at.fhtw.webenprjbackend.entity;
