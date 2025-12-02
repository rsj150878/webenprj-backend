package at.fhtw.webenprjbackend.repository;

import at.fhtw.webenprjbackend.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MediaRepository  extends JpaRepository<Media, UUID> {
}
