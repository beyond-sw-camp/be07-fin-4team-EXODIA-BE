package com.example.exodia.userDelete.domain;

import com.example.exodia.common.domain.BaseTimeEntity;
import com.example.exodia.user.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class DeleteHistory  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String deletedBy;

    @Column(nullable = false)
    private String deleteReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User deletedUser;

    public DeleteHistory(String deletedBy, String deleteReason, User deletedUser) {
        this.deletedBy = deletedBy;
        this.deleteReason = deleteReason;
        this.deletedUser = deletedUser;
    }
}

