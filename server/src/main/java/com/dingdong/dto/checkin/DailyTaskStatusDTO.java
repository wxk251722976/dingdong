package com.dingdong.dto.checkin;

import com.dingdong.entity.checkin.CheckInLog;
import com.dingdong.entity.checkin.CheckInTask;
import lombok.Data;

@Data
public class DailyTaskStatusDTO {
    private CheckInTask task;
    private CheckInLog log; // Can be null
    private String status; // "PENDING", "COMPLETED", "MISSED"
}
