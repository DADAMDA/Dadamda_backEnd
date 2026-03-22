package com.example.Oauth.folder.repository;

import com.example.Oauth.folder.entity.Folder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FolderRepository extends JpaRepository<Folder, Long> {

    List<Folder> findAllByUserIdOrderByCreatedAtAsc(Long userId);

    @Query("""
        select f from Folder f
        where f.user.id = :userId
        order by f.createdAt asc
        limit :limit
        """)
    List<Folder> findTopByUserIdOrderByCreatedAtAsc(@Param("userId") Long userId, @Param("limit") int limit);

    Optional<Folder> findByIdAndUserId(Long id, Long userId);
}
