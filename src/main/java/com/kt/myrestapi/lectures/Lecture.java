package com.kt.myrestapi.lectures;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kt.myrestapi.security.userinfo.UserInfo;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder @AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of="id")
@Entity
@Table(name = "lectures")
public class Lecture {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;
    private String description;

    private LocalDateTime beginEnrollmentDateTime;
    private LocalDateTime closeEnrollmentDateTime;
    private LocalDateTime beginLectureDateTime;
    private LocalDateTime endLectureDateTime;
    
    private String location;
    private int basePrice;
    private int maxPrice;
    private int limitOfEnrollment;

    private boolean offline;
    private boolean free;

    @Enumerated(EnumType.STRING)
    private LectureStatus lectureStatus = LectureStatus.DRAFT;

    public void update() {
        // Update free
        if (this.basePrice == 0 && this.maxPrice == 0) {
            this.free = true;   //무료 강의
        } else {
            this.free = false;  //유료 강의
        }
        // Update offline
        if (this.location == null || this.location.isBlank()) {
            this.offline = false;    //온라인 강의
        } else {
            this.offline = true;
        }
    }

    @ManyToOne
    private UserInfo userInfo;

}    