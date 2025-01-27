package com.wedit.weditapp.domain.comment.controller;

import com.wedit.weditapp.domain.comment.dto.request.CommentCreateRequestDto;
import com.wedit.weditapp.domain.comment.dto.response.CommentResponseDto;
import com.wedit.weditapp.domain.comment.dto.response.PagedCommentResponseDto;
import com.wedit.weditapp.domain.comment.service.CommentService;
import com.wedit.weditapp.global.response.GlobalResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 특정 청첩장의 방명록 조회 API(무한 스크롤)
    @Operation(summary = "방명록 조회", description = "특정 청첩장의 방명록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "방명록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "청첩장을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/{invitationId}")
    public ResponseEntity<GlobalResponseDto<PagedCommentResponseDto>> findAllComments(
            @PathVariable Long invitationId,
            @RequestParam(defaultValue = "1") int page){

        PagedCommentResponseDto response = commentService.findAllCommentsByInvitationId(invitationId, page);
        return ResponseEntity.status(HttpStatus.OK).body(GlobalResponseDto.success(response));
    }

    // 특정 청첩장에 방명록 등록 API
    @Operation(summary = "방명록 등록", description = "특정 청첩장에 방명록을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "방명록 등록 성공"),
            @ApiResponse(responseCode = "404", description = "청첩장을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @PostMapping
    public ResponseEntity<GlobalResponseDto<CommentResponseDto>> createComment(
            @Valid @RequestBody CommentCreateRequestDto commentCreateRequestDTO) {

        commentService.createComment(commentCreateRequestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(GlobalResponseDto.success());
    }
}
