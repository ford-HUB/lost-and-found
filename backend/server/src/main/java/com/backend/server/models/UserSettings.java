package com.backend.server.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "user_settings")

public class UserSettings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settings_id")
    private Long settingsId;

    @OneToOne
    @JoinColumn(
        name = "accounts",
        referencedColumnName = "account_id",
        nullable = false
    )
    private Account account;

    @Column(
        name = "profile_visibility",
        nullable = false,
        length = 20
    )
    private String profileVisibility; // public, items-only, private

    @Column(
        name = "show_email",
        nullable = false
    )
    private Boolean showEmail;

    @Column(
        name = "show_phone",
        nullable = false
    )
    private Boolean showPhone;

    @Column(
        name = "show_in_search",
        nullable = false
    )
    private Boolean showInSearch;

    @Column(
        name = "allow_messages",
        nullable = false
    )
    private Boolean allowMessages;

    @Column(
        name = "updated_at",
        nullable = true
    )
    private LocalDateTime updatedAt;

    public UserSettings() {} // initialize constructor

    public UserSettings(Long settingsId, Account account, String profileVisibility, Boolean showEmail, 
                        Boolean showPhone, Boolean showInSearch, Boolean allowMessages, LocalDateTime updatedAt) {
        this.settingsId = settingsId;
        this.account = account;
        this.profileVisibility = profileVisibility;
        this.showEmail = showEmail;
        this.showPhone = showPhone;
        this.showInSearch = showInSearch;
        this.allowMessages = allowMessages;
        this.updatedAt = updatedAt;
    }

    // @getters
    public Long getSettingsId() { return settingsId; }
    public Account getAccount() { return account; }
    public String getProfileVisibility() { return profileVisibility; }
    public Boolean getShowEmail() { return showEmail; }
    public Boolean getShowPhone() { return showPhone; }
    public Boolean getShowInSearch() { return showInSearch; }
    public Boolean getAllowMessages() { return allowMessages; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // @setters
    public void setSettingsId(Long settingsId) { this.settingsId = settingsId; }
    public void setAccount(Account account) { this.account = account; }
    public void setProfileVisibility(String profileVisibility) { this.profileVisibility = profileVisibility; }
    public void setShowEmail(Boolean showEmail) { this.showEmail = showEmail; }
    public void setShowPhone(Boolean showPhone) { this.showPhone = showPhone; }
    public void setShowInSearch(Boolean showInSearch) { this.showInSearch = showInSearch; }
    public void setAllowMessages(Boolean allowMessages) { this.allowMessages = allowMessages; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

}

