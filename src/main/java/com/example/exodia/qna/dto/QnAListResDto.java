//package com.beyond.teenkiri.qna.dto;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.time.LocalDateTime;
//
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//public class QnAListResDto {
//    private Long id;
//    private String questionUserName;
//    private String title;
//    private LocalDateTime createdTime;
//}

package com.example.exodia.qna.dto;

import com.example.exodia.department.domain.Department;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QnAListResDto {
    private Long id;
    private String questionUserName;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime answeredAt;
    private Department department;
    private Boolean secretBoard;
    private Boolean anonymous;
}