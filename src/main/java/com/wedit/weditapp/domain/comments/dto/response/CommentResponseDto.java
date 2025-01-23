package com.wedit.weditapp.domain.comments.dto.response;

import com.wedit.weditapp.domain.comments.domain.Comment;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentResponseDto {
    private Long commentId;
    private String name;
    private String content;

    @Builder
    private CommentResponseDto(Long commentId, String name, String content){
        this.commentId = commentId;
        this.name = name;
        this.content = content;
    }

    public static CommentResponseDto from (Comment comment){
        return CommentResponseDto.builder()
                .commentId(comment.getId())
                .name(comment.getName())
                .content(comment.getContent())
                .build();
    }

}
