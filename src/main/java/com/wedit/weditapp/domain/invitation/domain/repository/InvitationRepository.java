package com.wedit.weditapp.domain.invitation.domain.repository;

import java.util.List;
import java.util.Optional;

import com.wedit.weditapp.domain.invitation.domain.Invitation;
import com.wedit.weditapp.domain.member.domain.Member;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
	Optional<Invitation> findByUniqueId(String uniqueId);

	Optional<Invitation> findByIdAndMember(Long InvitationId, Member member);

	// 해당 회원의 청첩장 생성일 순으로 조회
	List<Invitation> findByMemberIdOrderByCreatedAtAsc(Long memberId);
}

