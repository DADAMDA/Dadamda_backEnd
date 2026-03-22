package com.example.Oauth.bookmark.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "북마크 개수 응답")
public record BookmarkCountResponse(
        @Schema(description = "현재 사용자가 저장한 전체 북마크 개수", example = "12")
        long count
) {
}
