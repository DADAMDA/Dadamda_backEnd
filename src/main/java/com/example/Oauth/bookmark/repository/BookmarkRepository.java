package com.example.Oauth.bookmark.repository;

import com.example.Oauth.bookmark.entity.Bookmark;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    @Query("""
            select bookmark
            from Bookmark bookmark
            join fetch bookmark.folder folder
            where folder.id = :folderId
              and folder.user.id = :userId
            order by bookmark.createdAt desc
            """)
    List<Bookmark> findBookmarks(@Param("folderId") Long folderId, @Param("userId") Long userId);

    @Query("""
            select bookmark
            from Bookmark bookmark
            join fetch bookmark.folder folder
            where bookmark.id = :bookmarkId
              and folder.user.id = :userId
            """)
    Optional<Bookmark> findOwnedBookmark(@Param("bookmarkId") Long bookmarkId, @Param("userId") Long userId);

    long countByFolderUserId(Long userId);

    @Query("""
        select bookmark
        from Bookmark bookmark
        join fetch bookmark.folder folder
        where folder.user.id = :userId
        order by bookmark.createdAt desc
        limit 1
        """)
    Optional<Bookmark> findLatestBookmark(@Param("userId") Long userId);
}
