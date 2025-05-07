package dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationDTO {
    private int id;
    private int userId; 
    private int type; //0: 가입신청 도착 알림, 1: 신청결과 알림
    private String message;
    private boolean isRead; // false: 읽지 않음, true: 읽음
    private java.sql.Timestamp createdAt;

}
