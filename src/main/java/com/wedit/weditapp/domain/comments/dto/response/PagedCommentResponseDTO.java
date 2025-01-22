package com.wedit.weditapp.domain.comments.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PagedCommentResponseDTO {

    private List<CommentResponseDTO> comments;
    private Boolean isLast;
    private Integer currentPage;

    @Builder
    private PagedCommentResponseDTO(List<CommentResponseDTO> comments, Boolean isLast, Integer currentPage){
        this.comments = comments;
        this.isLast = isLast;
        this.currentPage = currentPage;
    }
}
