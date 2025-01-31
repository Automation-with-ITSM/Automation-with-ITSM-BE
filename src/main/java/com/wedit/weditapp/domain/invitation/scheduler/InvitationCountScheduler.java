package com.wedit.weditapp.domain.invitation.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.wedit.weditapp.domain.invitation.domain.Invitation;
import com.wedit.weditapp.domain.invitation.domain.repository.InvitationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvitationCountScheduler {

	private final InvitationRepository invitationRepository;

	@Scheduled(cron = "0 0 0 * * *")
	@Transactional
	public void clearDailyVisitorsCount() {
		try {
			List<Invitation> invitationList = invitationRepository.findAll();

			for (Invitation invitation : invitationList) {
				invitation.clearDailyVisitors();
			}
			log.info("Clear Daily Visitors Count");

		} catch (Exception e) {
			log.error("Fail to clear Daily Visitors Count : {}", e.getMessage());

		}
	}
}
