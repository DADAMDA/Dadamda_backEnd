package com.example.Oauth.bookmark.service;

import com.example.Oauth.bookmark.crawler.BookmarkMetadata;
import com.example.Oauth.bookmark.crawler.MetadataCrawler;
import com.example.Oauth.bookmark.dto.BookmarkResponse;
import com.example.Oauth.bookmark.dto.CreateBookmarkRequest;
import com.example.Oauth.bookmark.entity.Bookmark;
import com.example.Oauth.bookmark.repository.BookmarkRepository;
import com.example.Oauth.common.exception.ResourceNotFoundException;
import com.example.Oauth.common.user.FixedCurrentUserProvider;
import com.example.Oauth.folder.entity.Folder;
import com.example.Oauth.folder.service.FolderService;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 북마크 생성, 조회, 삭제와 폴더 소유권 검증을 담당하는 서비스.
 * <p>
 * URL 검증과 HTML 파싱은 {@link MetadataCrawler}에 위임하고, 본 서비스는 엔티티 생성과 저장 흐름만 조율한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final FolderService folderService;
    private final MetadataCrawler metadataCrawler;
    private final FixedCurrentUserProvider currentUserProvider;

    /**
     * 현재 사용자의 폴더에 URL 기반 북마크를 생성한다.
     *
     * @param request 북마크 생성 요청 DTO
     * @return 저장된 북마크 응답 DTO
     */
    @Transactional
    public BookmarkResponse createBookmark(CreateBookmarkRequest request) {
        URI normalizedUri = metadataCrawler.parseUrl(request.url());
        Folder folder = folderService.getFolder(request.folderId());
        BookmarkMetadata metadata = metadataCrawler.crawl(normalizedUri.toString());

        Bookmark bookmark = Bookmark.create(
                folder,
                normalizedUri.toString(),
                metadata.title(),
                metadata.description(),
                metadata.thumbnail(),
                metadata.domain()
        );

        Bookmark savedBookmark = bookmarkRepository.save(bookmark);
        BookmarkResponse bookmarkResponse = BookmarkResponse.from(savedBookmark);
        return bookmarkResponse;
    }

    /**
     * 현재 사용자의 특정 폴더에 속한 북마크 목록을 최신순으로 조회한다.
     *
     * @param folderId 조회할 폴더 ID
     * @return 북마크 응답 DTO 목록
     */
    public List<BookmarkResponse> getBookmarks(Long folderId) {
        if (folderId == null || folderId <= 0) {
            throw new IllegalArgumentException("folderId must be positive");
        }

        folderService.getFolder(folderId);

        List<Bookmark> bookmarks = bookmarkRepository.findBookmarks(folderId, currentUserProvider.getCurrentUserId());
        List<BookmarkResponse> bookmarkResponses = bookmarks
                .stream()
                .map(BookmarkResponse::from)
                .toList();
        return bookmarkResponses;
    }

    /**
     * 현재 사용자가 소유한 북마크를 삭제한다.
     *
     * @param bookmarkId 삭제할 북마크 ID
     * @return 삭제 완료 여부를 표현하기 위한 반환값 없는 처리
     */
    @Transactional
    public void deleteBookmark(Long bookmarkId) {
        if (bookmarkId == null || bookmarkId <= 0) {
            throw new IllegalArgumentException("bookmarkId must be positive");
        }

        Bookmark bookmark = bookmarkRepository.findOwnedBookmark(bookmarkId, currentUserProvider.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark not found: " + bookmarkId));

        bookmarkRepository.delete(bookmark);
        /*민석테스트 수정*/
    }
}
