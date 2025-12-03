/**
 * REST API Controllers for the Motivise study blogging platform.
 *
 * <p>This package contains all HTTP endpoint handlers, organized by domain and resource type.
 * Controllers follow RESTful design principles and serve as the presentation layer,
 * delegating business logic to the service layer.
 *
 * <p><b>Controllers:</b>
 * <ul>
 *   <li>{@link at.fhtw.webenprjbackend.controller.AuthController}
 *       - User authentication and JWT token issuance ({@code /auth/*})</li>
 *   <li>{@link at.fhtw.webenprjbackend.controller.UserController}
 *       - User registration, profile management, and admin operations ({@code /users/*})</li>
 *   <li>{@link at.fhtw.webenprjbackend.controller.PostController}
 *       - Study post CRUD operations and search ({@code /posts/*})</li>
 *   <li>{@link at.fhtw.webenprjbackend.controller.MediaController}
 *       - File upload and media management ({@code /media/*})</li>
 * </ul>
 *
 * <p><b>Design Patterns & Conventions:</b>
 * <ul>
 *   <li><b>RESTful Resource Naming:</b> Plural nouns for collections ({@code /users}, {@code /posts})</li>
 *   <li><b>HTTP Method Semantics:</b>
 *     <ul>
 *       <li>GET - Retrieve resources (no side effects, idempotent)</li>
 *       <li>POST - Create new resources (returns 201 Created)</li>
 *       <li>PUT - Full resource update (idempotent)</li>
 *       <li>PATCH - Partial resource update</li>
 *       <li>DELETE - Remove resources (returns 204 No Content)</li>
 *     </ul>
 *   </li>
 *   <li><b>Data Transfer Objects (DTOs):</b> All request/response bodies use DTOs to
 *       decouple API contracts from domain entities</li>
 *   <li><b>OpenAPI/Swagger Documentation:</b> Comprehensive {@code @Operation},
 *       {@code @ApiResponses}, and {@code @Schema} annotations for automatic API documentation</li>
 *   <li><b>Validation:</b> {@code @Valid} annotation triggers Jakarta Bean Validation on request DTOs</li>
 *   <li><b>Authorization:</b> Spring Security {@code @PreAuthorize} annotations for
 *       declarative method-level security</li>
 * </ul>
 *
 * <p><b>Response Status Codes:</b>
 * <ul>
 *   <li><b>200 OK:</b> Successful GET, PUT, PATCH requests</li>
 *   <li><b>201 Created:</b> Successful POST requests (resource creation)</li>
 *   <li><b>204 No Content:</b> Successful DELETE requests</li>
 *   <li><b>400 Bad Request:</b> Validation errors, malformed input</li>
 *   <li><b>401 Unauthorized:</b> Missing or invalid authentication token</li>
 *   <li><b>403 Forbidden:</b> Authenticated but not authorized for operation</li>
 *   <li><b>404 Not Found:</b> Requested resource doesn't exist</li>
 *   <li><b>409 Conflict:</b> Uniqueness constraint violations (duplicate email/username)</li>
 * </ul>
 *
 * <p><b>Authentication & Authorization:</b>
 * <ul>
 *   <li><b>Public Endpoints:</b> {@code POST /auth/login}, {@code POST /users} (registration)</li>
 *   <li><b>Authenticated Endpoints:</b> Most endpoints require JWT Bearer token in Authorization header</li>
 *   <li><b>Admin-Only Endpoints:</b> User management, search, deletion require ADMIN role</li>
 *   <li><b>Self-Service:</b> {@code /users/me} endpoints allow users to manage their own data</li>
 *   <li><b>Resource-Level Authorization:</b> Post update/delete use custom permission evaluator
 *       to ensure users can only modify their own posts (or admins can modify any)</li>
 * </ul>
 *
 * <p><b>Error Handling:</b>
 * <ul>
 *   <li>Validation errors return 400 with detailed field-level error messages</li>
 *   <li>Authentication failures return 401 with generic error messages (security)</li>
 *   <li>Authorization failures return 403 with access denied messages</li>
 *   <li>Not found errors return 404 with resource-specific messages</li>
 *   <li>All errors follow consistent JSON structure with timestamp, status, error, message, path</li>
 * </ul>
 *
 * <p><b>CORS Configuration:</b>
 * <ul>
 *   <li>Configured in {@link at.fhtw.webenprjbackend.security.SecurityConfiguration}</li>
 *   <li>Development: Allows localhost:3000, 5173, 5176, 8080</li>
 *   <li>Production: Configurable via CORS_ALLOWED_ORIGINS environment variable</li>
 * </ul>
 *
 * @see at.fhtw.webenprjbackend.dto
 * @see at.fhtw.webenprjbackend.service
 * @see at.fhtw.webenprjbackend.security.SecurityConfiguration
 */
package at.fhtw.webenprjbackend.controller;
