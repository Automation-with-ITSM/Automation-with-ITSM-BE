package com.wedit.weditapp.domain.member.domain;

import com.wedit.weditapp.domain.shared.BaseTimeEntity;
import com.wedit.weditapp.domain.shared.MemberRole;
import com.wedit.weditapp.domain.shared.MemberStatus;
import com.wedit.weditapp.global.error.ErrorCode;
import com.wedit.weditapp.global.error.exception.CommonException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MemberStatus status;

    @Column(name = "refresh_token")
    private String refreshToken;

    // Builder를 통해서만 객체를 생성하도록 (일반 생성자는 protected)
    @Builder
    private Member(String email, String name) {
        this.email = email;
        this.name = name;
        this.role = MemberRole.USER;
        this.status = MemberStatus.ACTIVE;
    }

    // 사용자 생성 Method
    public static Member createUser(String email, String name) {
        return Member.builder()
                .email(email)
                .name(name)
                .build();
    }

    // 사용자 이메일 변경 Method
    public void updateEmail(String newEmail) {
        if (newEmail == null || newEmail.trim().isEmpty()) {
            throw new CommonException(ErrorCode.EMPTY_FIELD);
        }
        this.email = newEmail;
    }

    // 사용자 이름 변경 Method
    public void updateName(String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new CommonException(ErrorCode.EMPTY_FIELD);
        }
        this.name = newName;
    }

    // 사용자 상태 변경 Method
    public void deactivate() {
        this.status = MemberStatus.INACTIVE;
    }

    // 사용자 역할 변경 Method
    public void updateRole(MemberRole newRole) {
        if (newRole == null) {
            throw new CommonException(ErrorCode.EMPTY_FIELD);
        }
        this.role = newRole;
    }

    // Refresh Token 업데이트
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // 스프링 시큐리티 인증에 필요한 권한 정보 반환
    public List<? extends GrantedAuthority> getAuthorities() {
        // 예: ROLE_USER, ROLE_ADMIN
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }
}
