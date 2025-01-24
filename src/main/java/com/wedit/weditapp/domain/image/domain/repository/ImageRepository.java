package com.wedit.weditapp.domain.image.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wedit.weditapp.domain.image.domain.Image;
import com.wedit.weditapp.domain.invitation.domain.Invitation;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ImageRepository extends JpaRepository<Image, Long> {
	// 특정 Invitation의 모든 이미지를 location 순서대로 조회
	List<Image> findByInvitation(Invitation invitation);
}
