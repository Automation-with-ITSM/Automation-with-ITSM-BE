package com.wedit.weditapp.domain.comments.domain.repository;

import com.wedit.weditapp.domain.comments.domain.Comments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CommentRepository extends JpaRepository<Comments, Long> {
    Page<Comments> findByInvitationId(Long invitationId, Pageable pageable);
}
