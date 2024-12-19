package com.learning_managment_system.dto;

public class CourseDTO {
    private Long id;
    private String title;
    private String description;
    private String duration;
    private String mediaFileUrl;

    // Constructor
    public CourseDTO(Long id, String title, String description, String duration, String mediaFileUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.mediaFileUrl = mediaFileUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getMediaFileUrl() {
        return mediaFileUrl;
    }

    public void setMediaFileUrl(String mediaFileUrl) {
        this.mediaFileUrl = mediaFileUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Getters and Setters
}
