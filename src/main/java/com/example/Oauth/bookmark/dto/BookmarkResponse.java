package com.example.Oauth.bookmark.dto;

import com.example.Oauth.bookmark.entity.Bookmark;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "북마크 응답")
public record BookmarkResponse(
        @Schema(description = "북마크 ID", example = "10")
        Long id,
        @Schema(description = "폴더 ID", example = "1")
        Long folderId,
        @Schema(description = "원본 URL", example = "https://example.com")
        String url,
        @Schema(description = "페이지 제목", example = "Example Domain")
        String title,
        @Schema(description = "페이지 설명", example = "This domain is for use in illustrative examples.")
        String description,
        @Schema(description = "썸네일 URL", example = "https://example.com/thumbnail.png")
        String thumbnail,
        @Schema(description = "도메인", example = "example.com")
        String domain

) {

    public static BookmarkResponse from(Bookmark bookmark) {
        return new BookmarkResponse(
                bookmark.getId(),
                bookmark.getFolder().getId(),
                bookmark.getUrl(),
                bookmark.getTitle(),
                bookmark.getDescription(),
                bookmark.getThumbnail(),
                bookmark.getDomain()
        );
    }
}
