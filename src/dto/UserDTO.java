package dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    private int id;
    private String email;
    private String password;
    private String name;
    private Integer age; //nullable
    private Integer gender; //0: 미선택, 1: 남성, 2: 여성
    private Double latitude; //nullable
    private Double longitude;//nullable
    private String bio; //nullable
    private java.sql.Timestamp createdAt;

}

