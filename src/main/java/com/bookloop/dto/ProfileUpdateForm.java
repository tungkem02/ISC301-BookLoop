package com.bookloop.dto;

import jakarta.validation.constraints.*;

/**
 * Form object for updating a user's profile.
 * Only editable fields are exposed — role, email (as login key),
 * and password are NOT included here.
 */
public class ProfileUpdateForm {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    private String fullName;

    @Size(max = 150)
    private String university;

    @Size(max = 100)
    private String campus;

    @Size(max = 20)
    private String phone;

    private String avatarUrl;

    // --- Getters and Setters ---

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
