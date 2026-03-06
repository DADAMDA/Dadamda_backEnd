package com.example.Oauth.folder.dto;

import com.example.Oauth.folder.entity.Folder;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "폴더 응답")
public record FolderResponse(
        @Schema(description = "폴더 ID", example = "1")
        Long id,
        @Schema(description = "폴더 이름", example = "읽을거리")
        String name,
        @Schema(description = "생성 시각", example = "2026-03-06T10:15:30Z")
        Instant createdAt,
        @Schema(description = "수정 시각", example = "2026-03-06T10:15:30Z")
        Instant updatedAt
) {

    public static FolderResponse from(Folder folder) {
        return new FolderResponse(
                folder.getId(),
                folder.getName(),
                folder.getCreatedAt(),
                folder.getUpdatedAt()
        );
    }
}
