package dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudyApplicationDTO {
    private int id;
    private int studyId;
    private int userId;
    private int status; // 0: 대기, 1: 수락, 2: 거절
    private String selfIntroduction; //nullable
    private java.sql.Timestamp createdAt;
}