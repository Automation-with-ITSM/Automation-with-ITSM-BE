package com.wedit.weditapp.domain.invitation.domain.repository;

import java.util.Optional;

import com.wedit.weditapp.domain.invitation.domain.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
	Optional<Invitation> findByUniqueId(String uniqueId);
}

