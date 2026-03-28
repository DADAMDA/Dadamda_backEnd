package com.example.Oauth.folder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "폴더 생성 요청")
public record CreateFolderRequest(
        @Schema(description = "폴더 이름", example = "읽을거리")
        @NotBlank(message = "name is required")
        @Size(max = 100, message = "name must be at most 100 characters")
        String name
) {
}
