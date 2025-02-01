package com.wedit.weditapp.domain.invitation.dto.response;

import com.wedit.weditapp.domain.decision.dto.response.CountResponseDto;
import com.wedit.weditapp.domain.invitation.domain.Invitation;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StatisticsDto {

	private Integer dailyVisitors;
	private Integer totalVisitors;
	private CountResponseDto attendees;

	@Builder
	private StatisticsDto (Integer dailyVisitors, Integer totalVisitors, CountResponseDto attendees) {
		this.dailyVisitors = dailyVisitors;
		this.totalVisitors = totalVisitors;
		this.attendees = attendees;
	}

	public static StatisticsDto of(Invitation invitation, CountResponseDto countResponseDto) {
		return StatisticsDto.builder()
			.dailyVisitors(invitation.getDailyVisitors())
			.totalVisitors(invitation.getTotalVisitors())
			.attendees(countResponseDto)
			.build();
	}

}
