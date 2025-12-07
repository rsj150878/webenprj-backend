package at.fhtw.webenprjbackend.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "media")
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name="content_type")
    private String contentType;

    @Column(name="external_id")
    private String externalId;

    @Column(name="name")
    private String name;

    @Column(name="create_user")
    @CreatedBy
    private UUID createUser;

    @Column(name="update_user")
    @LastModifiedBy
    private UUID updateUser;

    @Column(name="create_timestamp")
    @CreatedDate
    private LocalDateTime createTimestamp;

    @Column(name="update_timestamp")
    @LastModifiedDate
    private LocalDateTime updateTimestamp;
}
