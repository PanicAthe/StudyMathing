package dao;

import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudyTagDAO {

    // 스터디-태그 연결하기
    public void insertStudyTag(int studyId, int tagId) {
        String sql = "INSERT INTO study_tags (study_id, tag_id) VALUES (?, ?)";

        try (
            Connection conn = DBUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, studyId);
            pstmt.setInt(2, tagId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB 오류] 태그 연결 실패: " + e.getMessage());
        }
    }

    // 스터디에 연결된 모든 태그 삭제
    public void deleteStudyTags(int studyId) {
        String sql = "DELETE FROM study_tags WHERE study_id = ?";

        try (
            Connection conn = DBUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, studyId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB 오류] 태그 삭제 실패: " + e.getMessage());
        }
    }

    // 특정 스터디에 연결된 태그 ID 목록 조회
    public List<Integer> findTagIdsByStudyId(int studyId) {
        List<Integer> tagIds = new ArrayList<>();
        String sql = "SELECT tag_id FROM study_tags WHERE study_id = ?";

        try (
            Connection conn = DBUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, studyId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tagIds.add(rs.getInt("tag_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB 오류] 태그 ID 조회 실패: " + e.getMessage());
        }

        return tagIds;
    }

    // 특정 태그가 스터디에 연결되어 있는지 여부 확인
    public boolean existsStudyTag(int studyId, int tagId) {
        String sql = "SELECT COUNT(*) FROM study_tags WHERE study_id = ? AND tag_id = ?";

        try (
            Connection conn = DBUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, studyId);
            pstmt.setInt(2, tagId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("[DB 오류] 태그 존재 여부 확인 실패: " + e.getMessage());
        }

        return false;
    }
}
