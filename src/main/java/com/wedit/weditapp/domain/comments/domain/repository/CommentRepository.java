package com.wedit.weditapp.domain.comments.domain.repository;

import com.wedit.weditapp.domain.comments.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByInvitationId(Long invitationId, Pageable pageable);
}
