package com.example.Oauth.bookmark.entity;

import com.example.Oauth.folder.entity.Folder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "bookmarks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id", nullable = false)
    private Folder folder;

    @Column(nullable = false, length = 2000)
    private String url;

    @Column(length = 500)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(length = 1000)
    private String thumbnail;

    @Column(length = 255)
    private String domain;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Bookmark(Folder folder, String url, String title, String description, String thumbnail, String domain) {
        this.folder = folder;
        this.url = url;
        this.title = title;
        this.description = description;
        this.thumbnail = thumbnail;
        this.domain = domain;
    }

    public static Bookmark create(
            Folder folder,
            String url,
            String title,
            String description,
            String thumbnail,
            String domain
    ) {
        return new Bookmark(folder, url, title, description, thumbnail, domain);
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }
}
