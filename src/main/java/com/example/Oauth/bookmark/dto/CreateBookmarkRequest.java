package com.example.Oauth.bookmark.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

@Schema(description = "북마크 생성 요청")
public record CreateBookmarkRequest(
        @Schema(description = "폴더 ID", example = "1")
        @NotNull(message = "folderId is required")
        @Positive(message = "folderId must be positive")
        Long folderId,
        @Schema(description = "저장할 URL", example = "https://example.com")
        @NotBlank(message = "url is required")
        @Pattern(regexp = "https?://.+", message = "url must start with http:// or https://")
        String url
) {
}
