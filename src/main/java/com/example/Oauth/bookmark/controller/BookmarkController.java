package com.example.Oauth.bookmark.controller;

import com.example.Oauth.bookmark.dto.BookmarkResponse;
import com.example.Oauth.bookmark.dto.CreateBookmarkRequest;
import com.example.Oauth.bookmark.service.BookmarkService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
@Tag(name = "Bookmark", description = "북마크 API")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping
    @Operation(summary = "북마크 생성", description = "URL을 입력받아 메타데이터를 크롤링한 뒤 북마크를 생성한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "북마크 생성 성공",
                    content = @Content(schema = @Schema(implementation = BookmarkResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "폴더를 찾을 수 없음"),
            @ApiResponse(responseCode = "502", description = "메타데이터 크롤링 실패")
    })
    public ResponseEntity<BookmarkResponse> createBookmark(@Valid @RequestBody CreateBookmarkRequest request) {
        BookmarkResponse bookmarkResponse = bookmarkService.createBookmark(request);
        ResponseEntity<BookmarkResponse> response = ResponseEntity.status(HttpStatus.CREATED).body(bookmarkResponse);
        return response;
    }

    @GetMapping
    @Operation(summary = "북마크 목록 조회", description = "folderId 기준으로 북마크 목록을 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "북마크 목록 조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = BookmarkResponse.class)))),
            @ApiResponse(responseCode = "404", description = "폴더를 찾을 수 없음")
    })
    public ResponseEntity<List<BookmarkResponse>> getBookmarks(
            @Parameter(description = "조회할 폴더 ID", example = "1")
            @RequestParam Long folderId
    ) {
        List<BookmarkResponse> bookmarkResponses = bookmarkService.getBookmarks(folderId);
        ResponseEntity<List<BookmarkResponse>> response = ResponseEntity.ok(bookmarkResponses);
        return response;
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "북마크 삭제", description = "북마크 ID로 북마크를 삭제한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "북마크 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "북마크를 찾을 수 없음")
    })
    public ResponseEntity<Void> deleteBookmark(
            @Parameter(description = "삭제할 북마크 ID", example = "10")
            @PathVariable Long id
    ) {
        bookmarkService.deleteBookmark(id);
        ResponseEntity<Void> response = ResponseEntity.noContent().build();
        return response;
    }
}
