package com.wedit.weditapp.domain.image.domain;

import com.wedit.weditapp.domain.invitation.domain.Invitation;
import com.wedit.weditapp.domain.shared.BaseTimeEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(nullable = false)
	private String url;

	@Column(nullable = false)
	private int location;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE) // 초대장 삭제 시 이미지도 삭제
	@JoinColumn(name = "invitation_id", nullable = false)
	private Invitation invitation;

	@Builder
	private Image(String url, int location, Invitation invitation) {
		this.url = url;
		this.location = location;
		this.invitation = invitation;
	}

	public Image createImage(String url, int location, Invitation invitation) {
		return Image.builder()
			.url(url)
			.location(location)
			.invitation(invitation)
			.build();
	}

	public void updateImage(String url, int location, Invitation invitation) {
		this.url = url;
		this.location = location;
		this.invitation = invitation;
	}
}
