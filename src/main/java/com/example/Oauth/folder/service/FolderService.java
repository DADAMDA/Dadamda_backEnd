package com.example.Oauth.folder.service;

import com.example.Oauth.common.user.FixedCurrentUserProvider;
import com.example.Oauth.folder.dto.CreateFolderRequest;
import com.example.Oauth.folder.dto.FolderResponse;
import com.example.Oauth.folder.entity.Folder;
import com.example.Oauth.folder.repository.FolderRepository;
import com.example.Oauth.common.exception.ResourceNotFoundException;
import com.example.Oauth.user.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 고정 사용자 기준의 폴더 생성 및 조회를 담당하는 서비스.
 * <p>
 * 현재 프로토타입 단계에서는 인증 대신 {@code userId=1} 사용자를 기준으로 동작한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FolderService {

    private final FolderRepository folderRepository;
    private final FixedCurrentUserProvider currentUserProvider;

    /**
     * 현재 사용자 기준으로 새 폴더를 생성한다.
     *
     * @param request 폴더 생성 요청 DTO
     * @return 생성된 폴더 응답 DTO
     */
    @Transactional
    public FolderResponse createFolder(CreateFolderRequest request) {
        User currentUser = currentUserProvider.getCurrentUser();
        Folder folder = Folder.create(currentUser, request.name().trim());
        Folder savedFolder = folderRepository.save(folder);
        return FolderResponse.from(savedFolder);
    }

    /**
     * 현재 사용자가 소유한 폴더 목록을 최신순으로 조회한다.
     *
     * @return 폴더 응답 DTO 목록
     */
    public List<FolderResponse> getFolders() {
        List<Folder> folders = folderRepository.findAllByUserIdOrderByCreatedAtDesc(currentUserProvider.getCurrentUserId());
        List<FolderResponse> folderResponses = folders.stream()
                .map(FolderResponse::from)
                .toList();
        return folderResponses;
    }

    /**
     * 현재 사용자가 소유한 폴더를 조회한다.
     *
     * @param folderId 폴더 ID
     * @return 현재 사용자 소유의 폴더 엔티티
     */
    public Folder getFolder(Long folderId) {
        if (folderId == null || folderId <= 0) {
            throw new IllegalArgumentException("folderId must be positive");
        }

        return folderRepository.findByIdAndUserId(folderId, currentUserProvider.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found: " + folderId));
    }
}
