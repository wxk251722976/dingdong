package com.dingdong.dto.checkin;

import lombok.Data;
import java.time.LocalTime;

@Data
public class TaskDTO {
    private Long id;
    private Long creatorId;
    private Long targetUserId;
    private String title;
    private LocalTime remindTime;
    private String repeatDays; // "1,2,3,4,5"
}
