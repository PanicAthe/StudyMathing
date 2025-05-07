package service;

import dao.*;
import dto.*;
import util.DistanceUtil;

import java.sql.SQLException;
import java.util.*;

public class StudyService {

	private final StudyDAO studyDAO = new StudyDAO();
	private final StudyApplicationDAO applicationDAO = new StudyApplicationDAO();
	private final UserDAO userDAO = new UserDAO();
	private final TagDAO tagDAO = new TagDAO();
	private final StudyTagDAO studyTagDAO = new StudyTagDAO();

	public int createStudy(int userId, String name, String description, int locationType, Double latitude,
			Double longitude, List<String> tags) {
		StudyDTO study = StudyDTO.builder().name(name).description(description).locationType(locationType)
				.latitude(latitude).longitude(longitude).isActive(1).createdBy(userId).build();

		int studyId = studyDAO.insertStudy(study);
		saveTags(studyId, tags);

		return studyId;
	}

	private void saveTags(int studyId, List<String> tags) {
		tags.stream().map(tag -> tag.trim().toLowerCase()).filter(clean -> !clean.isEmpty()).forEach(clean -> {
			try {
				Integer tagId = tagDAO.findTagIdByName(clean);
				if (tagId == null) {
					tagId = tagDAO.insertTag(clean);
				}
				if (!studyTagDAO.existsStudyTag(studyId, tagId)) {
					studyTagDAO.insertStudyTag(studyId, tagId);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	public List<StudyDTO> listMyStudies(int userId) {
		return studyDAO.findStudiesByUserId(userId);
	}

	public List<StudyDTO> listMyJoinedStudies(int userId) {
		List<StudyApplicationDTO> applications = applicationDAO.findAcceptedApplicationsByUserId(userId);
		List<StudyDTO> studies = new ArrayList<>();
		for (StudyApplicationDTO app : applications) {
			StudyDTO study = studyDAO.findById(app.getStudyId());
			if (study != null)
				studies.add(study);
		}
		return studies;
	}

	public List<StudyDTO> listAllStudiesByDistance(int userId) {
		Double[] location = userDAO.findUserLocationById(userId);
		if (location == null || location[0] == null || location[1] == null) {
			System.out.println("[오류] 사용자 위치 정보가 없습니다. 거리순 정렬을 할 수 없습니다.");
			return Collections.emptyList();
		}

		double userLat = location[0];
		double userLon = location[1];

		List<StudyDTO> allStudies = studyDAO.findAllStudiesOrderByDistance(userId);
		List<Integer> userTags = userDAO.findTagIdsByUserId(userId);

		// ✅ 거리 계산 & 캐싱 (모든 스터디 대상)
		for (StudyDTO study : allStudies) {
			Double lat = study.getLatitude();
			Double lon = study.getLongitude();

			double distance = (lat != null && lon != null) ? DistanceUtil.calculateDistance(userLat, userLon, lat, lon)
					: Double.MAX_VALUE;

			study.setDistanceFromUser(distance); // ⚠️ 정렬 전에 꼭 캐싱해야 함
		}

		// ✅ 정렬: 온라인 우선 → 거리 → 태그 매칭 수
		List<StudyDTO> result = allStudies.stream().sorted((s1, s2) -> {
			// 1. 온라인 우선
			if (s1.getLocationType() != s2.getLocationType()) {
				return Integer.compare(s1.getLocationType(), s2.getLocationType());
			}

			// 2. 거리 비교 (null-safe)
			Double d1 = s1.getDistanceFromUser();
			Double d2 = s2.getDistanceFromUser();
			double dist1 = (d1 != null) ? d1 : Double.MAX_VALUE;
			double dist2 = (d2 != null) ? d2 : Double.MAX_VALUE;

			if (Double.compare(dist1, dist2) != 0) {
				return Double.compare(dist1, dist2); // 가까운 순
			}

			// 3. 태그 매칭 수 (많은 순)
			List<Integer> tags1 = studyDAO.findTagIdsByStudyId(s1.getId());
			List<Integer> tags2 = studyDAO.findTagIdsByStudyId(s2.getId());

			int match1 = (int) tags1.stream().filter(userTags::contains).count();
			int match2 = (int) tags2.stream().filter(userTags::contains).count();

			return Integer.compare(match2, match1);
		}).toList();

		return result;
	}

	public List<StudyDTO> listAllStudiesByRecommend(int userId) {
		List<StudyDTO> studies = studyDAO.findAllStudiesOrderByRecommend(userId); // 모든 스터디
		List<Integer> userTags = userDAO.findTagIdsByUserId(userId);
		Double userLat = userDAO.findById(userId).getLatitude();
		Double userLon = userDAO.findById(userId).getLongitude();

		// 스터디 ID별: 태그 겹침 수 + 거리 저장
		Map<Integer, Integer> tagScoreMap = new HashMap<>();
		Map<Integer, Double> distanceMap = new HashMap<>();

		for (StudyDTO study : studies) {
			List<Integer> studyTags = studyDAO.findTagIdsByStudyId(study.getId());
			int score = (int) studyTags.stream().filter(userTags::contains).count();
			tagScoreMap.put(study.getId(), score);

			// 거리 계산 (오프라인만), 온라인은 거리 0
			if (study.getLocationType() == 1 && userLat != null && userLon != null) {
				double dist = DistanceUtil.calculateDistance(userLat, userLon, study.getLatitude(),
						study.getLongitude());
				distanceMap.put(study.getId(), dist);
			} else {
				distanceMap.put(study.getId(), 0.0); // 온라인 스터디는 거리 0
			}
		}

		studies.sort((s1, s2) -> {
			int score1 = tagScoreMap.getOrDefault(s1.getId(), 0);
			int score2 = tagScoreMap.getOrDefault(s2.getId(), 0);

			if (score1 != score2) {
				return Integer.compare(score2, score1); // 태그 점수 높은 순
			}

			if (s1.getLocationType() != s2.getLocationType()) {
				return Integer.compare(s1.getLocationType(), s2.getLocationType()); // 온라인(0) 먼저
			}

			return Double.compare(distanceMap.get(s1.getId()), distanceMap.get(s2.getId())); // 거리 짧은 순
		});

		return studies;
	}

	public StudyDTO viewStudyDetail(int studyId) {
		return studyDAO.findById(studyId);
	}

	public String getStudyNameById(int studyId) {
		return Optional.ofNullable(studyDAO.findById(studyId)).map(StudyDTO::getName).orElse("(알 수 없음)");
	}

	public boolean updateStudy(int studyId, int userId, String name, String desc, Integer isActive,
			Integer locationType, Double latitude, Double longitude) {

		StudyDTO study = studyDAO.findById(studyId);
		if (study == null || study.getCreatedBy() != userId)
			return false;

		String updatedName = (name == null || name.isBlank()) ? study.getName() : name;
		String updatedDesc = (desc == null || desc.isBlank()) ? study.getDescription() : desc;
		Integer updatedActive = isActive != null ? isActive : study.getIsActive();
		int updatedType = locationType != null ? locationType : study.getLocationType();

		Double updatedLat = updatedType == 1 ? (latitude != null ? latitude : study.getLatitude()) : null;

		Double updatedLon = updatedType == 1 ? (longitude != null ? longitude : study.getLongitude()) : null;

		studyDAO.updateStudy(studyId, updatedName, updatedDesc, updatedActive, updatedType, updatedLat, updatedLon);
		return true;
	}

	public boolean deleteStudy(int studyId, int userId) {
		StudyDTO study = studyDAO.findById(studyId);
		if (study == null || study.getCreatedBy() != userId)
			return false;

		studyDAO.deleteStudy(studyId);
		return true;
	}

	public boolean updateStudyTags(int studyId, int userId, List<String> tags) {
		StudyDTO study = studyDAO.findById(studyId);
		if (study == null || study.getCreatedBy() != userId)
			return false;

		studyTagDAO.deleteStudyTags(studyId);
		saveTags(studyId, tags);
		return true;
	}

	public StudyDTO validateOwnership(int studyId, int userId) {
		StudyDTO study = studyDAO.findById(studyId);
		if (study == null) {
			System.out.println("[오류] 스터디를 찾을 수 없습니다.");
			return null;
		}
		if (study.getCreatedBy() != userId) {
			System.out.println("[오류] 권한이 없습니다.");
			return null;
		}
		return study;
	}

	public List<String> getTagNamesByStudyId(int studyId) {
		List<Integer> tagIds = studyTagDAO.findTagIdsByStudyId(studyId);
		List<String> tagNames = new ArrayList<>();

		for (Integer id : tagIds) {
			try {
				String name = tagDAO.findTagNameById(id);
				if (name != null)
					tagNames.add(name);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return tagNames;
	}
}
