package com.wedit.weditapp.domain.image.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.wedit.weditapp.domain.image.domain.Image;
import com.wedit.weditapp.domain.image.domain.repository.ImageRepository;
import com.wedit.weditapp.domain.invitation.domain.Invitation;
import com.wedit.weditapp.domain.shared.S3Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImageService {
	private final S3Service s3Service;
	private final ImageRepository imageRepository;
	/**
	 * S3에 이미지를 업로드하고 URL을 반환하며, DB에 저장.
	 *
	 * @param images       업로드할 MultipartFile 리스트
	 * @param invitation   연관된 Invitation 엔티티
	 */
	@Transactional
	public void saveImages(List<MultipartFile> images, Invitation invitation) {
		if (images.size() != 4) {
			throw new IllegalArgumentException("Exactly 4 images are required.");
		}

		int location = 1; // 이미지 위치 인덱스
		for (MultipartFile image : images) {
			String imageUrl = s3Service.upload(image); // S3 업로드 후 URL 반환

			// 이미지 엔티티 생성 및 저장
			Image imageEntity = Image.builder()
				.url(imageUrl)
				.location(location++)
				.invitation(invitation)
				.build();

			imageRepository.save(imageEntity);
		}
	}
}


