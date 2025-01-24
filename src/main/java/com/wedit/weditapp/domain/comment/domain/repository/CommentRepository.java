package com.wedit.weditapp.domain.comment.domain.repository;

import com.wedit.weditapp.domain.comment.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByInvitationId(Long invitationId, Pageable pageable);

}
