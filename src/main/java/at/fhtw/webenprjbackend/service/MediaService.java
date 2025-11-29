package at.fhtw.webenprjbackend.service;

import at.fhtw.webenprjbackend.entity.Media;
import at.fhtw.webenprjbackend.filestorage.FileStorage;
import at.fhtw.webenprjbackend.repository.MediaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class MediaService {

    private final MediaRepository mediaRepositoryRepository;
    private final FileStorage fileStorage;

    public Media upload(MultipartFile toUpload) {
        String externalId = fileStorage.upload(toUpload);

        Media cover = new Media();
        cover.setName(toUpload.getOriginalFilename());
        cover.setExternalId(externalId);
        cover.setContentType(toUpload.getContentType());

        return mediaRepositoryRepository.save(cover);
    }

    public Media findById(UUID id) {
        return mediaRepositoryRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
    }

    public Resource asResource(Media cover) {
        InputStream stream = fileStorage.load(cover.getExternalId());

        return new InputStreamResource(stream);
    }


}
