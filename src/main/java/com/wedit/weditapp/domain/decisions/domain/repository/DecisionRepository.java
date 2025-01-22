package com.wedit.weditapp.domain.decisions.domain.repository;

import com.wedit.weditapp.domain.decisions.domain.Decisions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DecisionRepository extends JpaRepository<Decisions, Long> {
    @Query("select count(d) from Decisions d where d.invitation.id = :invitationId")
    Integer getAllDecisionCount(@Param("invitationId") Long invitationId);

    @Query("select count(d) from Decisions d where d.invitation.id = :invitationId and d.side = 'GROOM'")
    Integer getGroomDecisionCount(@Param("invitationId") Long invitationId);

    @Query("select count(d) from Decisions d where d.invitation.id = :invitationId and d.side = 'BRIDE'")
    Integer getBrideDecisionCount(@Param("invitationId") Long invitationId);

}
