package com.hekademos.hekademos_backend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exercises")
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "video_url", nullable = false)
    private String videoUrl;

    @Column(name = "youtubeUrl", nullable = true)
    private String youtubeUrl;

    @Column(name = "youtube_video_id")
    private String youtubeVideoId;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "is_short", columnDefinition = "boolean default false")
    private Boolean isShort = false;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;

    public Exercise() {
    }

    public Exercise(String name, String videoUrl, String youtubeUrl, Boolean isShort) {
        this.name = name;
        this.videoUrl = videoUrl;
        this.youtubeUrl = youtubeUrl;
        this.isShort = isShort;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getYoutubeVideoId() {
        return youtubeVideoId;
    }

    public void setYoutubeVideoId(String youtubeVideoId) {
        this.youtubeVideoId = youtubeVideoId;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Boolean getIsShort() {
        return isShort;
    }

    public void setIsShort(Boolean isShort) {
        this.isShort = isShort;
    }

    public LocalDateTime getSyncedAt() {
        return syncedAt;
    }

    public void setSyncedAt(LocalDateTime syncedAt) {
        this.syncedAt = syncedAt;
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    @Override
    public String toString() {
        return "Exercise [id=" + id + ", name=" + name + ", videoUrl=" + videoUrl + ", youtubeUrl=" + youtubeUrl
                + ", youtubeVideoId=" + youtubeVideoId + ", thumbnailUrl=" + thumbnailUrl + ", publishedAt="
                + publishedAt + ", isShort=" + isShort + ", syncedAt=" + syncedAt + "]";
    }
}