package com.wedit.weditapp.domain.image.dto.response;

import com.wedit.weditapp.domain.image.domain.Image;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ImageResponseDto {
	private String url;

	private int location;

	@Builder
	private ImageResponseDto(String url, int location) {
		this.url = url;
		this.location = location;
	}

	public static ImageResponseDto from(Image image){
		return ImageResponseDto.builder()
			.url(image.getUrl())
			.location(image.getLocation())
			.build();
	}
}
