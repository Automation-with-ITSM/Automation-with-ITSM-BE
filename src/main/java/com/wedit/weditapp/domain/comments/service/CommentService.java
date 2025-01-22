package com.wedit.weditapp.domain.comments.service;

import com.wedit.weditapp.domain.comments.domain.Comments;
import com.wedit.weditapp.domain.comments.domain.repository.CommentRepository;
import com.wedit.weditapp.domain.comments.dto.response.CommentResponseDTO;
import com.wedit.weditapp.domain.comments.dto.response.PagedCommentResponseDTO;
import com.wedit.weditapp.global.error.ErrorCode;
import com.wedit.weditapp.global.error.exception.CommonException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private static final int PAGE_SIZE = 4; // 한 페이지에 보여줄 방명록 개수

    private final CommentRepository commentRepository;

    public PagedCommentResponseDTO findAllCommentsByInvitationId(Long invitationId, int page){

//        boolean existsInvitation = invitationRepository.existById(invitationId);
//        if(!existsInvitation){
//            throw new CommonException(ErrorCode.INVITATION_NOT_FOUND);
//        }

        if(page < 1){
            throw new CommonException(ErrorCode.INVALID_PAGE_NUMBER);
        }


        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE, Sort.by("createdAt").descending());
        Page<Comments> commentPage = commentRepository.findByInvitationId(invitationId, pageable);

        if(commentPage.isEmpty()){
            throw new CommonException(ErrorCode.NO_MORE_COMMENTS);
        }

        // Comments 엔티티 -> DTO로 변환
        Page<CommentResponseDTO> commentDTOPage = commentPage.map(CommentResponseDTO::from);

        // 필요한 정보만으로 DTO 생성
        return PagedCommentResponseDTO.of(commentDTOPage, commentPage.isLast(), page);


    }
}
