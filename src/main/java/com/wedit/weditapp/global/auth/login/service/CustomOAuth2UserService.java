package com.wedit.weditapp.global.auth.login.service;

import com.wedit.weditapp.domain.member.domain.Member;
import com.wedit.weditapp.domain.member.domain.repository.MemberRepository;
import com.wedit.weditapp.global.error.ErrorCode;
import com.wedit.weditapp.global.error.exception.CommonException;
import com.wedit.weditapp.global.auth.login.domain.CustomOAuth2User;
import com.wedit.weditapp.global.auth.login.dto.OAuthAttributes;
import com.wedit.weditapp.global.auth.login.userInfo.KakaoOAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("CustomOAuth2UserService.loadUser() - 카카오 OAuth2 로그인 요청 진입");

        // 1. DefaultOAuth2UserService를 사용해 카카오에서 사용자 정보(attributes)를 가져옴
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 2. registrationId, userNameAttributeName 추출 (카카오인 경우)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest
                .getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        // 3. 카카오 사용자 정보(attributes) 가져오기
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 카카오 전용 OAuthAttributes 생성 (ofKakao)
        OAuthAttributes extractAttributes =
                OAuthAttributes.ofKakao(userNameAttributeName, attributes);

        // 4. DB에서 Member 조회 or 생성
        Member member = getOrSaveMember(extractAttributes);

        // 5. DefaultOAuth2User를 상속한 CustomOAuth2User 반환 + USER로 설정
        return new CustomOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + member.getRole().name())),
                attributes,
                extractAttributes.getNameAttributeKey(),
                member.getEmail(),
                member.getRole(),
                member.getStatus()
        );
    }

    // DB에서 카카오 소셜 회원 식별자를 통해 Member 조회
    // 없으면 새로 생성 후 저장
    private Member getOrSaveMember(OAuthAttributes attributes) {
        KakaoOAuth2UserInfo kakaoInfo = (KakaoOAuth2UserInfo) attributes.getOauth2UserInfo();

        String nickname = validateOrSetDefaultNickname(kakaoInfo.getNickname());
        String email = validateOrThrowEmail(kakaoInfo.getEmail());

        return memberRepository.findByEmail(email).orElseGet(() -> saveNewMember(email, nickname));
    }

    // 닉네임 검증 메서드
    private String validateOrSetDefaultNickname(String nickname) {
        return (nickname == null || nickname.isEmpty()) ? "카카오사용자" : nickname;
    }

    // 이메일 검증 메서드
    private String validateOrThrowEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new CommonException(ErrorCode.EMAIL_NOT_PROVIDED);
        }
        return email;
    }

    // 새 멤버 저장 메서드
    private Member saveNewMember(String email, String nickname) {
        return memberRepository.save(Member.createUser(email, nickname));
    }
}
