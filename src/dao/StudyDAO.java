package dao;

import dto.StudyDTO;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudyDAO {

	public List<StudyDTO> findStudiesByUserId(int userId) {
		String sql = "SELECT * FROM studies WHERE created_by = ? ORDER BY created_at DESC";
		return executeStudyListQuery(sql, pstmt -> pstmt.setInt(1, userId));
	}

	public List<StudyDTO> findAllStudiesOrderByDistance(int userId) {
		String sql = "SELECT * FROM studies WHERE is_active = 1";
		return executeStudyListQuery(sql, null);
	}

	public List<StudyDTO> findAllStudiesOrderByRecommend(int userId) {
		String sql = "SELECT * FROM studies WHERE is_active = 1";
		return executeStudyListQuery(sql, null);
	}

	public void deleteStudyTags(int studyId) {
		String sql = "DELETE FROM study_tags WHERE study_id = ?";
		executeUpdate(sql, pstmt -> pstmt.setInt(1, studyId));
	}

	public StudyDTO findById(int studyId) {
		String sql = "SELECT * FROM studies WHERE id = ?";
		try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, studyId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return mapResultSetToStudy(rs);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<Integer> findTagIdsByStudyId(int studyId) {
		List<Integer> tagIds = new ArrayList<>();
		String sql = "SELECT tag_id FROM study_tags WHERE study_id = ?";
		try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, studyId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					tagIds.add(rs.getInt("tag_id"));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tagIds;
	}

	public int insertStudy(StudyDTO study) {
		String sql = "INSERT INTO studies (name, description, is_active, location_type, latitude, longitude, created_by) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?)";
		String seqSql = "SELECT studies_seq.CURRVAL FROM dual";
		int generatedId = 0;

		try (Connection conn = DBUtil.getConnection()) {
			conn.setAutoCommit(false);

			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
				setInsertStudyParams(pstmt, study);
				pstmt.executeUpdate();
			}

			try (PreparedStatement pstmtSeq = conn.prepareStatement(seqSql); ResultSet rs = pstmtSeq.executeQuery()) {
				if (rs.next()) {
					generatedId = rs.getInt(1);
				}
			}

			conn.commit();
			System.out.println("스터디가 생성되었습니다!");
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return generatedId;
	}

	public void updateStudy(int studyId, String name, String desc, Integer isActive, Integer locationType,
			Double latitude, Double longitude) {
		String sql = """
				    UPDATE studies SET
				        name = COALESCE(?, name),
				        description = NVL2(?, ?, description),
				        is_active = COALESCE(?, is_active),
				        location_type = COALESCE(?, location_type),
				        latitude = COALESCE(?, latitude),
				        longitude = COALESCE(?, longitude)
				    WHERE id = ?
				""";

		try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, name);
			pstmt.setString(2, desc); // NVL2 조건
			pstmt.setString(3, desc); // 실제 값
			pstmt.setInt(4, isActive); 
			pstmt.setObject(5, locationType, Types.INTEGER);
			pstmt.setObject(6, latitude, Types.DOUBLE);
			pstmt.setObject(7, longitude, Types.DOUBLE);
			pstmt.setInt(8, studyId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void deleteStudy(int studyId) {
		String sql = "DELETE FROM studies WHERE id = ?";
		executeUpdate(sql, pstmt -> pstmt.setInt(1, studyId));
	}

	private List<StudyDTO> executeStudyListQuery(String sql, SQLConsumer<PreparedStatement> consumer) {
		List<StudyDTO> list = new ArrayList<>();
		try (Connection conn = DBUtil.getConnection();
				PreparedStatement pstmt = (consumer != null) ? conn.prepareStatement(sql) : null;
				Statement stmt = (consumer == null) ? conn.createStatement() : null;
				ResultSet rs = (consumer != null) ? executeQuery(pstmt, consumer) : stmt.executeQuery(sql)) {

			while (rs.next()) {
				list.add(mapResultSetToStudy(rs));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	private ResultSet executeQuery(PreparedStatement pstmt, SQLConsumer<PreparedStatement> consumer)
			throws SQLException {
		consumer.accept(pstmt);
		return pstmt.executeQuery();
	}

	private void executeUpdate(String sql, SQLConsumer<PreparedStatement> consumer) {
		try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			consumer.accept(pstmt);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void setInsertStudyParams(PreparedStatement pstmt, StudyDTO study) throws SQLException {
		pstmt.setString(1, study.getName());
		pstmt.setString(2, study.getDescription());
		pstmt.setInt(3, study.getIsActive());
		pstmt.setInt(4, study.getLocationType());
		if (study.getLatitude() != null) {
			pstmt.setDouble(5, study.getLatitude());
		} else {
			pstmt.setNull(5, Types.DOUBLE);
		}
		if (study.getLongitude() != null) {
			pstmt.setDouble(6, study.getLongitude());
		} else {
			pstmt.setNull(6, Types.DOUBLE);
		}
		pstmt.setInt(7, study.getCreatedBy());
	}

	private StudyDTO mapResultSetToStudy(ResultSet rs) throws SQLException {
		return StudyDTO.builder().id(rs.getInt("id")).name(rs.getString("name"))
				.description(rs.getString("description")).isActive(rs.getInt("is_active"))
				.locationType(rs.getInt("location_type"))
				.latitude(rs.getObject("latitude") != null ? rs.getDouble("latitude") : null)
				.longitude(rs.getObject("longitude") != null ? rs.getDouble("longitude") : null)
				.createdBy(rs.getInt("created_by")).createdAt(rs.getTimestamp("created_at")).build();
	}

	@FunctionalInterface
	private interface SQLConsumer<T> {
		void accept(T t) throws SQLException;
	}
}
