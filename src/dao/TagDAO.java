package dao;

import java.sql.*;
import util.DBUtil;

public class TagDAO {

	public TagDAO() {
	}

	// 태그 이름으로 id 찾기
	public Integer findTagIdByName(String name) throws SQLException {
		String sql = "SELECT id FROM tags WHERE name = ?";
		try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, name);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("id");
				}
			}
		}
		return null;
	}

	// id로 태그 이름 찾기
	public String findTagNameById(int id) throws SQLException {
		String sql = "SELECT name FROM tags WHERE id = ?";
		try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, id);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getString("name");
				}
			}
		}
		return null;
	}

	// 태그 삽입
	public Integer insertTag(String tagName) throws SQLException {
		try (Connection conn = DBUtil.getConnection()) {
			conn.setAutoCommit(false); // 수동 커밋 모드

			try {
				// 1. 먼저 존재하는지 확인
				String checkSql = "SELECT id FROM tags WHERE name = ?";
				try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
					checkStmt.setString(1, tagName);
					try (ResultSet checkRs = checkStmt.executeQuery()) {
						if (checkRs.next()) {
							conn.rollback();
							return checkRs.getInt("id");
						}
					}
				}

				// 2. 바로 NEXTVAL 사용해서 삽입
				String insertSql = "INSERT INTO tags (id, name) VALUES (tags_seq.NEXTVAL, ?)";
				try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, new String[] { "id" })) {
					insertStmt.setString(1, tagName);
					insertStmt.executeUpdate();

					// 3. 새로 생성된 id 가져오기
					try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
						if (generatedKeys.next()) {
							conn.commit();
							return generatedKeys.getInt(1);
						}
					}
				}

				conn.commit();
				return null;

			} catch (SQLException e) {
				conn.rollback();
				throw e;
			} finally {
				conn.setAutoCommit(true); // 자동 커밋 복구
			}
		}
	}

}
