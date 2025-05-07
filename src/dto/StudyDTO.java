package dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudyDTO {
    private int id;
    private String name;
    private String description;
    private int isActive; //0: 비활성화, 1: 활성화
    private int locationType;  // 0: 온라인, 1: 오프라인
    private Double latitude; //nullable
    private Double longitude; //nullable
    private int createdBy;
    private java.sql.Timestamp createdAt;
    private Double distanceFromUser; // 테이블과 다른 필드. 정렬용.

}
