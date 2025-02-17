package com.wedit.weditapp.domain.comment.service;

import java.util.List;

import com.wedit.weditapp.domain.comment.domain.Comment;
import com.wedit.weditapp.domain.comment.domain.repository.CommentRepository;
import com.wedit.weditapp.domain.comment.dto.request.CommentCreateRequestDto;
import com.wedit.weditapp.domain.comment.dto.response.CommentResponseDto;
import com.wedit.weditapp.domain.comment.dto.response.PagedCommentResponseDto;
import com.wedit.weditapp.domain.invitation.domain.Invitation;
import com.wedit.weditapp.domain.invitation.domain.repository.InvitationRepository;
import com.wedit.weditapp.global.error.ErrorCode;
import com.wedit.weditapp.global.error.exception.CommonException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private static final int PAGE_SIZE = 4; // 한 페이지에 보여줄 방명록 개수

    private final CommentRepository commentRepository;
    private final InvitationRepository invitationRepository;

    // 방명록 조회
    public PagedCommentResponseDto findAllCommentsByInvitationId(Long invitationId, int page){

        // 잘못된 청첩장 id일 경우
        if (invitationId == null || invitationId <= 0){
            throw new CommonException(ErrorCode.INVALID_INVITATION_ID);
        }

        // id에 해당하는 청첩장이 존재하지 않을 경우
        boolean existsInvitation = invitationRepository.existsById(invitationId);
        if(!existsInvitation){
            throw new CommonException(ErrorCode.INVITATION_NOT_FOUND);
        }

        // 요청한 페이지가 1보다 작은 경우
        if(page < 1){
            throw new CommonException(ErrorCode.INVALID_PAGE_NUMBER);
        }

        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE, Sort.by("createdAt").descending());
        Page<Comment> commentPage = commentRepository.findByInvitationId(invitationId, pageable);

        // 빈 페이지의 경우
        if(commentPage.isEmpty()){
            throw new CommonException(ErrorCode.NO_MORE_COMMENTS);
        }

        // Comments 엔티티 -> DTO로 변환
        Page<CommentResponseDto> commentDTOPage = commentPage.map(CommentResponseDto::from);

        // 필요한 정보만으로 DTO 생성
        return PagedCommentResponseDto.of(commentDTOPage, commentPage.isLast(), page);
    }

    // 방명록 등록
    public Comment createComment(CommentCreateRequestDto commentCreateRequestDto) {

        // 잘못된 청첩장 id일 경우
        if (commentCreateRequestDto.getInvitationId() == null || commentCreateRequestDto.getInvitationId() <= 0) {
            throw new CommonException(ErrorCode.INVALID_INVITATION_ID);
        }
        Invitation invitation = invitationRepository.findById(commentCreateRequestDto.getInvitationId())
                .orElseThrow(() -> new CommonException(ErrorCode.INVITATION_NOT_FOUND)); // id에 해당하는 청첩장이 존재하지 않을 경우

        Comment comment = Comment.createComment(
                commentCreateRequestDto.getName(),
                commentCreateRequestDto.getContent(),
                invitation
        );

        return commentRepository.save(comment);
    }

    // 방명록 전체 삭제
    public void deleteComment(Invitation invitation) {
        List<Comment> comments = commentRepository.findByInvitation(invitation);

        if (comments == null || comments.isEmpty()) {
            return; // 댓글이 없거나 null이면 아무 작업도 하지 않음
        }

        commentRepository.deleteAll(comments);
    }
}
