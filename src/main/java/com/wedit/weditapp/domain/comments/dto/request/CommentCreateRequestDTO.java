package com.wedit.weditapp.domain.comments.dto.request;

import com.wedit.weditapp.domain.comments.domain.Comments;
import com.wedit.weditapp.domain.invitation.domain.Invitation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentCreateRequestDTO {

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    @NotNull(message = "청첩장 ID는 필수입니다.")
    private Long invitationId;
}
