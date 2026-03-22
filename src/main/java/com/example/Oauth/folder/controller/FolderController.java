package com.example.Oauth.folder.controller;

import com.example.Oauth.folder.dto.CreateFolderRequest;
import com.example.Oauth.folder.dto.FolderResponse;
import com.example.Oauth.folder.service.FolderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
@Tag(name = "Folder", description = "폴더 API")
public class FolderController {

    private final FolderService folderService;

    @PostMapping
    @Operation(summary = "폴더 생성", description = "고정 userId=1 기준으로 폴더를 생성한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "폴더 생성 성공",
                    content = @Content(schema = @Schema(implementation = FolderResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ResponseEntity<FolderResponse> createFolder(@Valid @RequestBody CreateFolderRequest request) {
        FolderResponse folderResponse = folderService.createFolder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(folderResponse);
    }

    @GetMapping
    @Operation(summary = "폴더 목록 조회", description = "고정 userId=1 기준으로 폴더 목록을 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "폴더 목록 조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = FolderResponse.class))))
    })
    public ResponseEntity<List<FolderResponse>> getFolders(
            @Parameter(description = "조회할 폴더 수 제한 (미입력 시 전체 조회)", example = "6")
            @RequestParam(required = false) Integer limit
    ) {
        List<FolderResponse> folderResponses = folderService.getFolders(limit);
        return ResponseEntity.ok(folderResponses);
    }
}
