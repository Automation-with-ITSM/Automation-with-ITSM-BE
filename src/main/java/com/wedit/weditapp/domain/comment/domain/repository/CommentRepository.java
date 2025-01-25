package com.wedit.weditapp.domain.comment.domain.repository;

import java.util.List;

import com.wedit.weditapp.domain.comment.domain.Comment;
import com.wedit.weditapp.domain.image.domain.Image;
import com.wedit.weditapp.domain.invitation.domain.Invitation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByInvitationId(Long invitationId, Pageable pageable);

    List<Comment> findByInvitation(Invitation invitation);
}
