package at.fhtw.webenprjbackend.service;

import java.io.InputStream;
import java.util.UUID;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import at.fhtw.webenprjbackend.entity.Media;
import at.fhtw.webenprjbackend.filestorage.FileStorage;
import at.fhtw.webenprjbackend.repository.MediaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor

public class MediaService {

    private final MediaRepository mediaRepository;
    private final FileStorage fileStorage;

    public Media upload(MultipartFile toUpload) {
        String externalId = fileStorage.upload(toUpload);

        Media media = new Media();
        media.setName(toUpload.getOriginalFilename());
        media.setExternalId(externalId);
        media.setContentType(toUpload.getContentType());

        return mediaRepository.save(media);
    }

    public Media findById(UUID id) {
        return mediaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Media not found with id: " + id));
    }

    public Resource asResource(Media cover) {
        InputStream stream = fileStorage.load(cover.getExternalId());

        return new InputStreamResource(stream);
    }

    public void delete(UUID id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Media not found"));

        fileStorage.delete(media.getExternalId());
        mediaRepository.delete(media);

    }


}
