//package com.example.exodia.user.domain;
//
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import javax.persistence.*;
//import java.time.LocalDateTime;
//
//@Getter
//@NoArgsConstructor
//@AllArgsConstructor
//@Entity
//@Table(name = "user_delete_history")
//public class UserDeleteHistory {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;
//
//    private String deletedBy;
//    private String deleteReason;
//
//    private LocalDateTime deletedAt;
//
//    public UserDeleteHistory(User user, String deletedBy, String deleteReason) {
//        this.user = user;
//        this.deletedBy = deletedBy;
//        this.deleteReason = deleteReason;
//        this.deletedAt = LocalDateTime.now();
//    }
//}
