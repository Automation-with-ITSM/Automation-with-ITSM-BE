package com.wedit.weditapp.domain.comments.dto.response;

import com.wedit.weditapp.domain.comments.domain.Comments;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

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

    public static PagedCommentResponseDTO of(Page<CommentResponseDTO> comments, Boolean isLast, Integer currentPage){
        return PagedCommentResponseDTO.builder()
                .comments(comments.getContent())
                .isLast(isLast)
                .currentPage(currentPage)
                .build();


    }
}
