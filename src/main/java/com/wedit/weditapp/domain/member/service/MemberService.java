package com.wedit.weditapp.domain.member.service;

import com.wedit.weditapp.domain.member.domain.Member;
import com.wedit.weditapp.domain.member.domain.repository.MemberRepository;
import com.wedit.weditapp.domain.member.dto.LoginRequestDto;
import com.wedit.weditapp.domain.member.dto.MemberRequestDto;
import com.wedit.weditapp.domain.member.dto.MemberResponseDto;
import com.wedit.weditapp.domain.member.dto.TokenResponseDto;
import com.wedit.weditapp.global.error.ErrorCode;
import com.wedit.weditapp.global.error.exception.CommonException;
import com.wedit.weditapp.global.auth.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

    // [로그인 관련]
    public Member getOrCreateSocialMember(String email, String nickname) {
        return memberRepository.findByEmail(email)
                .orElseGet(() -> {
                    // 새 엔티티 생성
                    Member newMember = Member.createUser(email, nickname);
                    return memberRepository.save(newMember);
                });
    }

    // [모든 회원 조회]
    public List<MemberResponseDto> findAllMembers() {
        return memberRepository.findAll()
                .stream()
                .map(MemberResponseDto::from)
                .collect(Collectors.toList());
    }

    // [단일 회원 조회]
    public MemberResponseDto findMember(Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.USER_NOT_FOUND));
        return MemberResponseDto.from(member);
    }
}
