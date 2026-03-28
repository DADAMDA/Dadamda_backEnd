package com.example.Oauth.folder.dto;

import com.example.Oauth.folder.entity.Folder;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "폴더 응답")
public record FolderResponse(
        @Schema(description = "폴더 ID", example = "1")
        Long id,
        @Schema(description = "폴더 이름", example = "읽을거리")
        String name
) {
    public static FolderResponse from(Folder folder) {
        return new FolderResponse(
                folder.getId(),
                folder.getName()
        );
    }
}