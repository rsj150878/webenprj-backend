package at.fhtw.webenprjbackend.controller;

import at.fhtw.webenprjbackend.entity.Media;
import at.fhtw.webenprjbackend.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/medias")
public class MediaController {

    private final Logger log = LoggerFactory.getLogger(MediaController.class);

    private final MediaService mediaService;

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Media upload(@RequestParam("file") MultipartFile toUpload) {
        log.info("Uploading media file ");
        return mediaService.upload(toUpload);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> retrieve(@PathVariable UUID id) {
        Media media = mediaService.findById(id);

        Resource resource = mediaService.asResource(media);
        MediaType mediaType = MediaType.parseMediaType(media.getContentType());

        return ResponseEntity
                .ok()
                .contentType(mediaType)
                .body(resource);
    }
}



