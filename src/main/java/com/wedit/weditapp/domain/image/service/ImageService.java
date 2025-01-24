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
import com.wedit.weditapp.global.error.ErrorCode;
import com.wedit.weditapp.global.error.exception.CommonException;

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
		// 이미지 리스트가 null인지 확인
		if (images == null || images.size() != 4) {
			throw new CommonException(ErrorCode.INVALID_INPUT_VALUE);
		}

		int location = 1; // 이미지 위치 인덱스
		for (MultipartFile image : images) {
			String imageUrl;
			try {
				imageUrl = s3Service.upload(image); // S3 업로드 후 URL 반환
			} catch (Exception e) {
				throw new CommonException(ErrorCode.IMAGE_UPLOAD_FAILED);
			}

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
		// 새로운 이미지 리스트가 null인지 확인
		if (newImages == null || newImages.size() != 4) {
			throw new CommonException(ErrorCode.INVALID_INPUT_VALUE);
		}

		// 기존 이미지 삭제
		List<Image> existingImages = imageRepository.findByInvitation(invitation);
		existingImages.forEach(image -> {
			try {
				s3Service.removeFile(image.getUrl()); // S3에서 파일 삭제
			} catch (Exception e) {
				throw new CommonException(ErrorCode.IMAGE_UPLOAD_FAILED);
			}
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
		images.forEach(image -> {
			try {
				s3Service.removeFile(image.getUrl()); // S3에서 삭제
			} catch (Exception e) {
				throw new CommonException(ErrorCode.IMAGE_UPLOAD_FAILED);
			}
		});

		if (!images.isEmpty()) {
			imageRepository.deleteAll(images); // DB에서 이미지 삭제
		}
	}
}
