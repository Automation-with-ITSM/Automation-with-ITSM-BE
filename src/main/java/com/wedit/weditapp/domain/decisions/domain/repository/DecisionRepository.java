package com.wedit.weditapp.domain.decisions.domain.repository;

import com.wedit.weditapp.domain.decisions.domain.Decision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DecisionRepository extends JpaRepository<Decision, Long> {
    // 총 방명록 수
    @Query("SELECT count(d) FROM Decision d WHERE d.invitation.id = :invitationId")
    Integer getTotalDecisionCount(@Param("invitationId") Long invitationId);

    // 신랑측 방명록 수
    @Query("SELECT count(d) FROM Decision d WHERE d.invitation.id = :invitationId AND d.side = 'GROOM'")
    Integer getGroomDecisionCount(@Param("invitationId") Long invitationId);

    // 신부측 방명록 수
    @Query("SELECT count(d) FROM Decision d WHERE d.invitation.id = :invitationId AND d.side = 'BRIDE'")
    Integer getBrideDecisionCount(@Param("invitationId") Long invitationId);

}
