package dao;

import dto.StudyApplicationDTO;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudyApplicationDAO {

	public void insertApplication(StudyApplicationDTO application) {
		String sql = "INSERT INTO study_applications (study_id, user_id, status, self_introduction) VALUES (?, ?, ?, ?)";

		try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, application.getStudyId());
			pstmt.setInt(2, application.getUserId());
			pstmt.setInt(3, application.getStatus());
			pstmt.setString(4, application.getSelfIntroduction());
			pstmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("[DB 오류] 가입 신청 실패: " + e.getMessage());
		}
	}

	public List<StudyApplicationDTO> findApplicationsByStudyId(int studyId) {
		List<StudyApplicationDTO> applications = new ArrayList<>();
		String sql = "SELECT * FROM study_applications WHERE study_id = ? ORDER BY created_at DESC";

		try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, studyId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					applications.add(mapResultSetToApplication(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("[DB 오류] 스터디 신청 목록 조회 실패: " + e.getMessage());
		}
		return applications;
	}

	public List<StudyApplicationDTO> findApplicationsByUserId(int userId) {
		List<StudyApplicationDTO> applications = new ArrayList<>();
		String sql = "SELECT * FROM study_applications WHERE user_id = ? ORDER BY created_at DESC";

		try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					applications.add(mapResultSetToApplication(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("[DB 오류] 유저 신청 조회 실패: " + e.getMessage());
		}
		return applications;
	}

	public List<StudyApplicationDTO> findAcceptedApplicationsByUserId(int userId) {
		List<StudyApplicationDTO> applications = new ArrayList<>();
		String sql = "SELECT * FROM study_applications WHERE user_id = ? AND status = 1";

		try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					applications.add(mapResultSetToApplication(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("[DB 오류] 수락된 신청 조회 실패: " + e.getMessage());
		}
		return applications;
	}

	public void deleteApplication(int userId, int studyId) {
		String sql = "DELETE FROM study_applications WHERE user_id = ? AND study_id = ?";

		try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			pstmt.setInt(2, studyId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("[DB 오류] 신청 삭제 실패: " + e.getMessage());
		}
	}

	public void updateApplicationStatus(int applicationId, int newStatus) {
		String sql = "UPDATE study_applications SET status = ? WHERE id = ?";

		try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, newStatus);
			pstmt.setInt(2, applicationId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("[DB 오류] 신청 상태 변경 실패: " + e.getMessage());
		}
	}

	public boolean existsApplication(int studyId, int userId) {
		String sql = "SELECT COUNT(*) FROM study_applications WHERE study_id = ? AND user_id = ?";

		try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, studyId);
			pstmt.setInt(2, userId);
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next() && rs.getInt(1) > 0;
			}
		} catch (SQLException e) {
			System.err.println("[DB 오류] 신청 존재 여부 확인 실패: " + e.getMessage());
		}
		return false;
	}

	public boolean existsAcceptedApplication(int userId, int studyId) {
		String sql = "SELECT COUNT(*) FROM study_applications WHERE user_id = ? AND study_id = ? AND status = 1";

		try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			pstmt.setInt(2, studyId);
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next() && rs.getInt(1) > 0;
			}
		} catch (SQLException e) {
			System.err.println("[DB 오류] 수락된 신청 존재 여부 확인 실패: " + e.getMessage());
		}
		return false;
	}

	public StudyApplicationDTO findById(int applicationId) {
		String sql = "SELECT * FROM study_applications WHERE id = ?";

		try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, applicationId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return mapResultSetToApplication(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("[DB 오류] 신청 단건 조회 실패: " + e.getMessage());
		}
		return null;
	}

	private StudyApplicationDTO mapResultSetToApplication(ResultSet rs) throws SQLException {
		return StudyApplicationDTO.builder().id(rs.getInt("id")).studyId(rs.getInt("study_id"))
				.userId(rs.getInt("user_id")).status(rs.getInt("status"))
				.selfIntroduction(rs.getString("self_introduction")).createdAt(rs.getTimestamp("created_at")).build();
	}
}
