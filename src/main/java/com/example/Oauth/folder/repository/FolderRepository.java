package com.example.Oauth.folder.repository;

import com.example.Oauth.folder.entity.Folder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderRepository extends JpaRepository<Folder, Long> {

    List<Folder> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Folder> findByIdAndUserId(Long id, Long userId);
}
