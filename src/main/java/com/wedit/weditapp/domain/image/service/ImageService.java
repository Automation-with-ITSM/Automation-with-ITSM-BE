package com.wedit.weditapp.domain.image.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.wedit.weditapp.domain.image.domain.Image;
import com.wedit.weditapp.domain.image.domain.repository.ImageRepository;
import com.wedit.weditapp.domain.image.dto.response.ImageResponseDto;
import com.wedit.weditapp.domain.invitation.domain.Invitation;
import com.wedit.weditapp.domain.shared.S3Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ImageService {
	private final S3Service s3Service;
	private final ImageRepository imageRepository;

	// S3에 이미지를 업로드하고 URL을 반환하며, DB에 저장
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

	// 특정 청첩장 사진들을 조회하여 DTO 리스트로 변환
	public List<ImageResponseDto> getImages(Invitation invitation) {
		List<Image> image = imageRepository.findByInvitation(invitation);

		return image.stream() // 스트림 생성
			.map(ImageResponseDto::from) // entity -> DTO
			.collect(Collectors.toList()); // 리스트로 수집
	}

	// 이미지 업데이트
	public void updateImages(List<MultipartFile> newImages, Invitation invitation) {
		if (newImages.size() != 4) {
			throw new IllegalArgumentException("Exactly 4 images are required.");
		}

		// 기존 이미지 삭제
		List<Image> existingImages = imageRepository.findByInvitation(invitation);
		existingImages.forEach(image -> {
			s3Service.removeFile(image.getUrl()); // S3에서 파일 삭제
			imageRepository.delete(image); // DB에서 이미지 삭제
		});

		// 새로운 이미지 업로드 및 저장
		saveImages(newImages, invitation);
	}

	// 이미지 삭제
	public void deleteImages(Invitation invitation) {
		// 청첩장 관련 이미지 조회
		List<Image> images = imageRepository.findByInvitation(invitation);

		// S3에서 이미지 삭제 및 DB에서 데이터 삭제
		if (!images.isEmpty()) {
			images.forEach(image -> {
				s3Service.removeFile(image.getUrl()); // S3에서 삭제
			});
			imageRepository.deleteAll(images); // DB에서 이미지 삭제
		}
	}
}
