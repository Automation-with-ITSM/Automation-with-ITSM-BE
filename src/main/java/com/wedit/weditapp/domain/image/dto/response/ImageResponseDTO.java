package com.wedit.weditapp.domain.image.dto.response;

import com.wedit.weditapp.domain.image.domain.Image;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

public class ImageResponseDTO {
	@NotBlank(message = "url cannot be blank")
	private String url;

	@NotNull(message = "location cannot be null")
	private int location;

	@Builder
	private ImageResponseDTO(String url, int location) {
		this.url = url;
		this.location = location;
	}

	public static ImageResponseDTO from(Image image){
		return ImageResponseDTO.builder()
			.url(image.getUrl())
			.location(image.getLocation())
			.build();
	}
}
