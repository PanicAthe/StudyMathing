package dao;

import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserTagDAO {

    // 유저-태그 연결 추가
    public void insertUserTag(int userId, int tagId) {
        String sql = "INSERT INTO user_tags (user_id, tag_id) VALUES (?, ?)";

        try (
            Connection conn = DBUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, tagId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) { // ORA-00001: unique constraint 위반
                System.out.println("⚠️ 이미 연결된 태그입니다. (tagId = " + tagId + ")");
            } else {
                System.err.println("[DB 오류] 태그 연결 실패: " + e.getMessage());
            }
        }
    }

    // 유저의 모든 관심 태그 삭제
    public void deleteUserTags(int userId) {
        String sql = "DELETE FROM user_tags WHERE user_id = ?";

        try (
            Connection conn = DBUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB 오류] 관심 태그 삭제 실패: " + e.getMessage());
        }
    }

    // 유저의 관심 태그 id 리스트 가져오기
    public List<Integer> findTagIdsByUserId(int userId) {
        List<Integer> tagIds = new ArrayList<>();
        String sql = "SELECT tag_id FROM user_tags WHERE user_id = ?";

        try (
            Connection conn = DBUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tagIds.add(rs.getInt("tag_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB 오류] 관심 태그 조회 실패: " + e.getMessage());
        }

        return tagIds;
    }
}
