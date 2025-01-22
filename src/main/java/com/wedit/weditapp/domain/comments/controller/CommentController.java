package com.wedit.weditapp.domain.comments.controller;

import com.wedit.weditapp.domain.comments.dto.response.CommentResponseDTO;
import com.wedit.weditapp.domain.comments.dto.response.PagedCommentResponseDTO;
import com.wedit.weditapp.domain.comments.service.CommentService;
import com.wedit.weditapp.global.response.GlobalResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 특정 청첩장의 방명록 조회 API(무한 스크롤)
    @Operation(summary = "방명록 조회", description = "특정 청첩장의 방명록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "방명록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "청첩장을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/{invitationId}")
    public ResponseEntity<GlobalResponseDto<PagedCommentResponseDTO>> findAllComments(
            @PathVariable Long invitationId,
            @RequestParam(defaultValue = "1") int page){

        PagedCommentResponseDTO response = commentService.findAllCommentsByInvitationId(invitationId, page);
        return ResponseEntity.status(HttpStatus.OK).body(GlobalResponseDto.success(response));
    }
}
