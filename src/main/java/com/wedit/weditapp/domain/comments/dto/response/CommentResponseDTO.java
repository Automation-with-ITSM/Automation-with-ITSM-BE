package com.wedit.weditapp.domain.comments.dto.response;

import com.wedit.weditapp.domain.comments.domain.Comments;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentResponseDTO {
    private Long commentId;
    private String name;
    private String content;

    @Builder
    private CommentResponseDTO(Long commentId, String name, String content){
        this.commentId = commentId;
        this.name = name;
        this.content = content;
    }

    public static CommentResponseDTO from (Comments comment){
        return CommentResponseDTO.builder()
                .commentId(comment.getId())
                .name(comment.getName())
                .content(comment.getContent())
                .build();
    }

}
