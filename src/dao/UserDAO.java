package dao;

import dto.UserDTO;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

	// 이메일 존재 여부 체크
	public boolean isEmailExists(String email) {
		String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
		try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, email);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	// 회원 등록
	public void insertUser(UserDTO user) {
		String sql = "INSERT INTO users (email, password, name) VALUES (?, ?, ?)";
		try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, user.getEmail());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getName());
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 이메일로 사용자 찾기
	public UserDTO findByEmail(String email) {
		return findUser("SELECT * FROM users WHERE email = ?", email);
	}

	// ID로 사용자 찾기
	public UserDTO findById(int id) {
		return findUser("SELECT * FROM users WHERE id = ?", id);
	}

	private UserDTO findUser(String sql, Object param) {
		try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			if (param instanceof String)
				pstmt.setString(1, (String) param);
			else if (param instanceof Integer)
				pstmt.setInt(1, (Integer) param);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return mapResultSetToUser(rs);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void updateProfile(int userId, String name, Integer age, String bio, Double latitude, Double longitude,
			Integer gender) {
		String sql = """
				    UPDATE users SET
				        name = COALESCE(?, name),
				        age = COALESCE(?, age),
				        bio = NVL2(?, ?, bio),
				        latitude = COALESCE(?, latitude),
				        longitude = COALESCE(?, longitude),
				        gender = COALESCE(?, gender)
				    WHERE id = ?
				""";

		try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, name);
			pstmt.setObject(2, age, Types.INTEGER);
			pstmt.setString(3, bio); // NVL2 조건
			pstmt.setString(4, bio); // 실제 값
			pstmt.setObject(5, latitude, Types.DOUBLE);
			pstmt.setObject(6, longitude, Types.DOUBLE);
			pstmt.setObject(7, gender, Types.INTEGER);
			pstmt.setInt(8, userId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 비밀번호 업데이트
	public void updatePassword(int userId, String newPassword) {
		String sql = "UPDATE users SET password = ? WHERE id = ?";
		try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, newPassword);
			pstmt.setInt(2, userId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 사용자 삭제
	public void deleteUser(int userId) {
		String sql = "DELETE FROM users WHERE id = ?";
		try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, userId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 유저 관심 태그 ID 목록 가져오기
	public List<Integer> findTagIdsByUserId(int userId) {
		List<Integer> tags = new ArrayList<>();
		String sql = "SELECT tag_id FROM user_tags WHERE user_id = ?";
		try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, userId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					tags.add(rs.getInt("tag_id"));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tags;
	}

	// 유저 위치 정보 가져오기
	public Double[] findUserLocationById(int userId) {
		String sql = "SELECT LATITUDE, LONGITUDE FROM USERS WHERE ID = ?";

		try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, userId);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					double latRaw = rs.getDouble("LATITUDE");
					Double latitude = rs.wasNull() ? null : latRaw;

					double lonRaw = rs.getDouble("LONGITUDE");
					Double longitude = rs.wasNull() ? null : lonRaw;

					return new Double[] { latitude, longitude };
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	// ResultSet -> UserDTO 매핑
	private UserDTO mapResultSetToUser(ResultSet rs) throws SQLException {
		return UserDTO.builder().id(rs.getInt("id")).email(rs.getString("email")).password(rs.getString("password"))
				.name(rs.getString("name")).age(rs.getObject("age") != null ? rs.getInt("age") : null)
				.gender(rs.getObject("gender") != null ? rs.getInt("gender") : null)
				.latitude(rs.getObject("latitude") != null ? rs.getDouble("latitude") : null)
				.longitude(rs.getObject("longitude") != null ? rs.getDouble("longitude") : null)
				.bio(rs.getString("bio")).createdAt(rs.getTimestamp("created_at")).build();
	}
}