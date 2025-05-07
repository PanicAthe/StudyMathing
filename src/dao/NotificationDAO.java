package dao;

import dto.NotificationDTO;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

	// 특정 유저의 안 읽은 알림 목록 조회
	public List<NotificationDTO> findNotificationsByUserId(int userId) {
		List<NotificationDTO> notifications = new ArrayList<>();
		String sql = "SELECT * FROM notifications WHERE user_id = ? AND is_read = 0 ORDER BY created_at DESC";

		try (Connection conn = DBUtil.getConnection();
				PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null;) {
			if (pstmt == null)
				return notifications;

			pstmt.setInt(1, userId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					notifications.add(mapResultSetToNotification(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("[DB 오류] 알림 조회 중 오류 발생: " + e.getMessage());
		}

		return notifications;
	}

	// 모든 알림을 읽음 처리
	public void markAllNotificationsAsRead(int userId) {
		String sql = "UPDATE notifications SET is_read = 1 WHERE user_id = ? AND is_read = 0";

		try (Connection conn = DBUtil.getConnection();
				PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null;) {
			if (pstmt == null)
				return;

			pstmt.setInt(1, userId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("[DB 오류] 알림 읽음 처리 실패: " + e.getMessage());
		}
	}

	// ResultSet -> DTO 변환
	private NotificationDTO mapResultSetToNotification(ResultSet rs) throws SQLException {
		return NotificationDTO.builder().id(rs.getInt("id")).userId(rs.getInt("user_id")).type(rs.getInt("type"))
				.message(rs.getString("message")).isRead(rs.getInt("is_read") == 1)
				.createdAt(rs.getTimestamp("created_at")).build();
	}
}
