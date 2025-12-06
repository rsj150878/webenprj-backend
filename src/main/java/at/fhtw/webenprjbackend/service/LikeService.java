package at.fhtw.webenprjbackend.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import at.fhtw.webenprjbackend.entity.Post;
import at.fhtw.webenprjbackend.entity.PostLike;
import at.fhtw.webenprjbackend.entity.User;
import at.fhtw.webenprjbackend.repository.PostLikeRepository;
import at.fhtw.webenprjbackend.repository.PostRepository;
import at.fhtw.webenprjbackend.repository.UserRepository;

@Service
@Transactional
public class LikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public LikeService(PostLikeRepository postLikeRepository, PostRepository postRepository, UserRepository userRepository) {
        this.postLikeRepository = postLikeRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public void like(UUID postId, UUID userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (postLikeRepository.existsByUserAndPost(user, post)) {
            return; // idempotent
        }
        postLikeRepository.save(new PostLike(user, post));
    }

    public void unlike(UUID postId, UUID userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        postLikeRepository.deleteByUserAndPost(user, post);
    }
}
