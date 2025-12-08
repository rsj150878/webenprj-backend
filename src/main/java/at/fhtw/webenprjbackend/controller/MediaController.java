package at.fhtw.webenprjbackend.controller;

import at.fhtw.webenprjbackend.entity.Media;
import at.fhtw.webenprjbackend.filestorage.FileUploadValidator;
import at.fhtw.webenprjbackend.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * REST Controller for media file uploads and retrieval.
 * Handles secure file uploads with validation and serves media files.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/medias")
@Tag(name = "Media", description = "File upload and download operations with security validation")
public class MediaController {

    private final Logger log = LoggerFactory.getLogger(MediaController.class);

    private final MediaService mediaService;
    private final FileUploadValidator fileUploadValidator;

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Upload a file",
            description = "Upload an image or PDF file. Validates file type, size, and content. " +
                    "Allowed types: jpg, jpeg, png, gif, pdf. Maximum size: 10MB. Requires authentication.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "File uploaded successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Media.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid file - type not supported, file too large, or malicious content detected",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Unsupported File Type",
                                            value = "{\"timestamp\":\"2024-12-03T10:30:00.123\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"File type '.exe' is not allowed for security reasons\",\"path\":\"/medias\"}"
                                    ),
                                    @ExampleObject(
                                            name = "File Too Large",
                                            value = "{\"timestamp\":\"2024-12-03T10:30:00.123\",\"status\":413,\"error\":\"Payload Too Large\",\"message\":\"File size (15 MB) exceeds maximum allowed size (10 MB)\",\"path\":\"/medias\"}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"timestamp\":\"2024-12-03T10:30:00.123\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Full authentication is required\",\"path\":\"/medias\"}"
                            )
                    )
            )
    })
    public Media upload(
            @Parameter(
                    description = "File to upload (jpg, jpeg, png, gif, pdf). Max 10MB.",
                    required = true
            )
            @RequestParam("file") MultipartFile toUpload) {

        log.info("Received file upload request: filename={}, size={}, contentType={}",
                toUpload.getOriginalFilename(),
                toUpload.getSize(),
                toUpload.getContentType());

        // Validate file before processing
        fileUploadValidator.validate(toUpload);

        log.info("File validation passed, proceeding with upload");
        return mediaService.upload(toUpload);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Retrieve a file",
            description = "Download a previously uploaded file by its ID. Public endpoint."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "File retrieved successfully",
                    content = @Content(
                            mediaType = "application/octet-stream"
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "File not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"timestamp\":\"2024-12-03T10:30:00.123\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Media not found\",\"path\":\"/medias/{id}\"}"
                            )
                    )
            )
    })
    @PreAuthorize("hasPermission(#id, 'at.fhtw.webenprjbackend.entity.Media', 'read')")
    public ResponseEntity<Resource> retrieve(
            @Parameter(description = "Media UUID", required = true)
            @PathVariable UUID id) {

        log.info("Retrieving media file: id={}", id);

        Media media = mediaService.findById(id);
        Resource resource = mediaService.asResource(media);
        MediaType mediaType = MediaType.parseMediaType(media.getContentType());

        return ResponseEntity
                .ok()
                .contentType(mediaType)
                .body(resource);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(#id, 'at.fhtw.webenprjbackend.entity.Media', 'delete')")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Media UUID", required = true)
            @PathVariable UUID id) {

        log.info("Deleting media file: id={}", id);


        mediaService.delete(id);


        return ResponseEntity.noContent().build();
    }
}



