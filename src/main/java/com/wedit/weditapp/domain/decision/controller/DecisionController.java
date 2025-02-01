package com.wedit.weditapp.domain.decision.controller;

import com.wedit.weditapp.domain.decision.dto.request.DecisionCreateRequestDto;
import com.wedit.weditapp.domain.decision.dto.response.DecisionResponseDto;
import com.wedit.weditapp.domain.decision.service.DecisionService;
import com.wedit.weditapp.global.response.GlobalResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/decisions")
@RequiredArgsConstructor
public class DecisionController {

    private final DecisionService decisionService;

    // 특정 청첩장에 참석의사 등록 API
    @Operation(summary = "참석의사 등록", description = "특정 청첩장에 참석의사를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "참석의사 등록 성공"),
            @ApiResponse(responseCode = "404", description = "청첩장을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @PostMapping
    public ResponseEntity<GlobalResponseDto<DecisionResponseDto>> createDecision(
            @Valid @RequestBody DecisionCreateRequestDto decisionCreateRequestDTO) {

        decisionService.createDecision(decisionCreateRequestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(GlobalResponseDto.success());
    }
}
