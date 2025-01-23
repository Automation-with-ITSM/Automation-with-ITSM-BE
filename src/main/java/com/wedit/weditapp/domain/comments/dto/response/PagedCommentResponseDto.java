package com.wedit.weditapp.domain.comments.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@NoArgsConstructor
public class PagedCommentResponseDto {

    private List<CommentResponseDto> comments;
    private Boolean isLast;
    private Integer currentPage;

    @Builder
    private PagedCommentResponseDto(List<CommentResponseDto> comments, Boolean isLast, Integer currentPage){
        this.comments = comments;
        this.isLast = isLast;
        this.currentPage = currentPage;
    }

    public static PagedCommentResponseDto of(Page<CommentResponseDto> comments, Boolean isLast, Integer currentPage){
        return PagedCommentResponseDto.builder()
                .comments(comments.getContent())
                .isLast(isLast)
                .currentPage(currentPage)
                .build();


    }
}
