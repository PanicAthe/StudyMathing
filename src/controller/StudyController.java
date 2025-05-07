package controller;

import dto.StudyApplicationDTO;
import dto.StudyDTO;
import service.StudyApplicationService;
import service.StudyService;
import util.InputUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import common.Session;

public class StudyController {

	private final StudyService studyService = new StudyService();
	private final StudyApplicationService studyApplicationService = new StudyApplicationService();
	private final Session session;

	public StudyController(Session session) {
		this.session = session;
	}

	// 기본 스터디 메뉴 화면
	public void studyMenu(Scanner scanner) {
		while (true) {
			System.out.println("\n\n===================================");
			System.out.println("           스터디 메뉴");
			System.out.println("===================================");
			System.out.println("  [1]   내 스터디 보기 (수정/삭제/태그수정)");
			System.out.println("  [2]   내 신청 목록 보기");
			System.out.println("  [3]   거리순 스터디 보기");
			System.out.println("  [4]   추천순 스터디 보기");
			System.out.println("  [5]   스터디 생성하기");
			System.out.println("  [0]   뒤로가기");
			int choice = InputUtil.inputInt(scanner, "선택 >>> ");

			switch (choice) {
			case 1 -> listAndSelectStudy(session.getUserId(), scanner, "MY");
			case 2 -> listMyApplicationsFlow(session.getUserId(), scanner);
			case 3 -> listAndSelectStudy(session.getUserId(), scanner, "DISTANCE");
			case 4 -> listAndSelectStudy(session.getUserId(), scanner, "RECOMMEND");
			case 5 -> createStudyFlow(session.getUserId(), scanner);
			case 0 -> {
				return;
			}
			default -> System.out.println("[에러] 잘못된 입력입니다.");
			}
		}
	}

	// [정렬 기준에 따라 스터디 목록 출력] -> 스터디의 상세조회 흐름으로 연결됨
	private void listAndSelectStudy(int userId, Scanner scanner, String mode) {
		List<StudyDTO> studies;
		switch (mode) {
		case "MY" -> studies = studyService.listMyStudies(userId);
		case "DISTANCE" -> studies = studyService.listAllStudiesByDistance(userId);
		case "RECOMMEND" -> studies = studyService.listAllStudiesByRecommend(userId);
		default -> throw new IllegalArgumentException("Invalid mode: " + mode);
		}

		if (studies.isEmpty()) {
			System.out.println("[알림] 스터디가 없습니다.");
			return;
		}

		System.out.println("\n============================================================================================================");
		System.out.printf("  %-3s | %-24s | %-30s | %-20s\n", "ID", "이름", "태그", "방식");
		System.out.println("============================================================================================================");

		for (StudyDTO study : studies) {
			List<String> tags = studyService.getTagNamesByStudyId(study.getId());
			String tagStr = tags.isEmpty() ? "(없음)"
					: tags.stream().map(t -> "#" + t).reduce((a, b) -> a + ", " + b).orElse("");

			if (tagStr.length() > 33)
				tagStr = tagStr.substring(0, 30) + "...";

			String modeStr = (study.getLocationType() == 0) ? "온라인" : "오프라인";
			String distanceStr = "";
			if (study.getLocationType() == 1 && study.getDistanceFromUser() != null
					&& study.getDistanceFromUser() != Double.MAX_VALUE) {
				distanceStr = String.format(" (%.2fkm)", study.getDistanceFromUser());
			}

			// 출력 순서: ID | 이름 | 태그 | 방식(거리)
			System.out.printf("  %-3d  %-24s  %-30s  %-20s\n", study.getId(), study.getName(), tagStr,
					modeStr + distanceStr);
		}

		System.out.println("============================================================================================================");

		int studyId = InputUtil.inputInt(scanner, "\n  선택할 스터디 ID 입력 (0: 뒤로가기): ");
		if (studyId == 0)
			return;

		StudyDTO study = studyService.viewStudyDetail(studyId);
		if (study == null) {
			System.out.println("[오류] 해당 스터디를 찾을 수 없습니다.");
			return;
		}

		viewStudyOptions(userId, study, scanner, mode.equals("MY"));
	}

	// 상세 조회와 동시에 스터디장인지 판단 -> 메뉴를 신분에 맞춰 출력
	private void viewStudyOptions(int userId, StudyDTO study, Scanner scanner, boolean isMyStudy) {
		while (true) {
			showStudyDetail(study);

			if (study.getCreatedBy() == userId) {
				System.out.println("\n[스터디장 메뉴]");
				System.out.println("1. 스터디 수정");
				System.out.println("2. 스터디 삭제");
				System.out.println("3. 태그 수정");
				System.out.println("4. 가입 신청 목록 보기 및 처리");
				System.out.println("0. 뒤로가기");

				int action = InputUtil.inputInt(scanner, "선택 >>> ");
				switch (action) {
				case 1 -> {
					updateStudyFlow(userId, study.getId(), scanner);
					study = studyService.viewStudyDetail(study.getId()); // 최신 정보 갱신
				}
				case 2 -> {
					deleteStudyFlow(userId, study.getId(), scanner);
					return;
				}
				case 3 -> {
					updateStudyTagsFlow(userId, study.getId(), scanner);
					study = studyService.viewStudyDetail(study.getId()); // 최신 정보 갱신
				}
				case 4 -> manageApplicationsFlow(study.getId(), scanner);
				case 0 -> {
					return;
				}
				default -> System.out.println("[에러] 잘못된 입력입니다.");
				}

			} else if (studyApplicationService.isUserJoinedStudy(userId, study.getId())) {
				System.out.println("\n[가입자 메뉴]");
				System.out.println("1. 스터디 탈퇴");
				System.out.println("0. 뒤로가기");

				int action = InputUtil.inputInt(scanner, "선택 >>> ");
				if (action == 1) {
					studyApplicationService.leaveStudy(userId, study.getId());
					return;
				} else if (action == 0) {
					return;
				}

			} else {
				if (study.getIsActive() == 0) {
					System.out.println("\n[알림] 이 스터디는 현재 비활성화 상태입니다. 신청할 수 없습니다.");
					return;
				}

				System.out.println("\n[비회원 메뉴]");
				System.out.println("1. 가입 신청");
				System.out.println("0. 뒤로가기");

				int action = InputUtil.inputInt(scanner, "선택 >>> ");
				if (action == 1) {
					String selfIntro = InputUtil.inputString(scanner, "자기소개 입력: ");
					studyApplicationService.applyToStudy(study.getId(), userId, selfIntro);
					System.out.println("\n[알림] 가입신청되었습니다!");
					return;
				} else if (action == 0) {
					return;
				}
			}
		}
	}

	// 스터디 상세 정보 출력
	private void showStudyDetail(StudyDTO study) {
		System.out.println("\n====================================");
		System.out.println("         스터디 상세 정보");
		System.out.println("====================================");
		System.out.println("ID        : " + study.getId());
		System.out.println("이름       : " + study.getName());
		System.out.println("설명       : " + (study.getDescription() != null ? study.getDescription() : "(설명 없음)"));

		String locationStr = switch (study.getLocationType()) {
		case 0 -> "온라인";
		case 1 -> String.format("오프라인\n   └ 위도: %.6f\n   └ 경도: %.6f", study.getLatitude(), study.getLongitude());
		default -> "알 수 없음";
		};

		System.out.println("모임 방식  : " + locationStr);
		System.out.println("상태       : " + ((study.getIsActive() == 1) ? "활성화" : "비활성화"));

		// 태그 출력
		List<String> tagNames = studyService.getTagNamesByStudyId(study.getId());
		if (!tagNames.isEmpty()) {
			System.out.print("태그       : ");
			for (String tag : tagNames) {
				System.out.print("#" + tag + " ");
			}
			System.out.println();
		}

		System.out.println("====================================");
	}

	// 스터디장이라면 가입 신청 관리를 할 수 있게
	private void manageApplicationsFlow(int studyId, Scanner scanner) {
		List<StudyApplicationDTO> applications = studyApplicationService.listApplicationsForStudy(studyId);

		if (applications.isEmpty()) {
			System.out.println("\n가입 신청이 없습니다.");
			return;
		}

		System.out.println("\n[가입 신청 목록]");
		for (StudyApplicationDTO app : applications) {
			String status = switch (app.getStatus()) {
			case 0 -> "대기중";
			case 1 -> "수락됨";
			case 2 -> "거절됨";
			default -> "알 수 없음";
			};
			System.out.printf("ID: %d | 신청자ID: %d | 상태: %s | 소개: %s\n", app.getId(), app.getUserId(), status,
					(app.getSelfIntroduction() != null ? app.getSelfIntroduction() : "-"));
		}

		while (true) {
			int applicationId = InputUtil.inputInt(scanner, "\n처리할 신청 ID 입력 (0: 뒤로가기): ");
			if (applicationId == 0)
				return;

			int decision = InputUtil.inputIntInRange(scanner, "수락(1) / 거절(2): ", 1, 2);

			boolean result = studyApplicationService.handleApplication(applicationId, decision);
			if (result) {
				System.out.println("\n[완료] 처리가 완료되었습니다.");
			} else {
				System.out.println("\n[에러] 처리에 실패했습니다.");
			}
		}
	}

	// 스터디 생성
	private void createStudyFlow(int userId, Scanner scanner) {
		System.out.println("\n[스터디 생성]");

		String name = InputUtil.inputString(scanner, "스터디 이름 입력: ");
		String description = InputUtil.inputString(scanner, "스터디 설명 입력: ");
		int locationType = InputUtil.inputIntInRange(scanner, "모임 방식 선택 (0=온라인, 1=오프라인): ", 0, 1);

		Double latitude = null;
		Double longitude = null;
		if (locationType == 1) {
			latitude = InputUtil.inputDouble(scanner, "모임 장소 위도 입력: ");
			longitude = InputUtil.inputDouble(scanner, "모임 장소 경도 입력: ");
		}

		String tagInput = InputUtil.inputString(scanner, "태그 입력 (쉼표로 구분): ");
		List<String> tags = Arrays.stream(tagInput.split(",")).map(String::trim).map(String::toLowerCase)
				.filter(s -> !s.isBlank()).toList();

		studyService.createStudy(userId, name, description, locationType, latitude, longitude, tags);
	}

	// 스터디 수정
	public void updateStudyFlow(int userId, int studyId, Scanner scanner) {
		StudyDTO study = studyService.validateOwnership(studyId, userId);
		if (study == null)
			return;

		String newName = InputUtil.inputOptionalString(scanner, "새 이름 (Enter시 유지): ");
		String newDesc = InputUtil.inputOptionalString(scanner, "새 설명 (Enter시 유지): ");
		Integer activeInput = InputUtil.inputOptionalInt(scanner, "활성화 여부 (1=활성, 0=비활성, Enter=유지): ");

		Integer newLocationType = InputUtil.inputOptionalIntInRange(scanner, "모임 방식 (0=온라인, 1=오프라인, Enter=유지): ", 0, 1);

		Double newLat = null;
		Double newLon = null;
		if (newLocationType != null && newLocationType == 1) {
			newLat = InputUtil.inputOptionalDouble(scanner, "새 위도 입력 (Enter시 null): ");
			newLon = InputUtil.inputOptionalDouble(scanner, "새 경도 입력 (Enter시 null): ");
		}

		boolean result = studyService.updateStudy(studyId, userId, newName, newDesc, activeInput, newLocationType,
				newLat, newLon);

		if (result) {
			System.out.println("\n[완료] 스터디가 수정되었습니다.");
		} else {
			System.out.println("\n[실패] 스터디 수정에 실패했습니다.");
		}
	}

	// 스터디 태그 수정
	public void updateStudyTagsFlow(int userId, int studyId, Scanner scanner) {
		StudyDTO study = studyService.validateOwnership(studyId, userId);
		if (study == null)
			return;

		String tagInput = InputUtil.inputString(scanner, "새 태그 입력 (쉼표구분): ");
		List<String> tags = Arrays.stream(tagInput.split(",")).map(String::trim).map(String::toLowerCase)
				.filter(s -> !s.isBlank()).toList();

		studyService.updateStudyTags(studyId, userId, tags);
		System.out.println("[완료] 태그 수정 완료!");
	}

	// 내 스터디 신청 목록 관리
	private void listMyApplicationsFlow(int userId, Scanner scanner) {
		var applications = studyApplicationService.listMyApplications(userId);

		if (applications.isEmpty()) {
			System.out.println("\n[알림] 신청한 스터디가 없습니다.");
			return;
		}

		System.out.println("\n======= 내 스터디 신청 목록 ========");
		System.out.printf("%-5s | %-20s | %-10s\n", "ID", "스터디 이름", "상태");
		System.out.println("--------------------------------------------------");

		for (var app : applications) {
			String statusStr = switch (app.getStatus()) {
			case 0 -> "대기중";
			case 1 -> "수락됨";
			case 2 -> "거절됨";
			default -> "알 수 없음";
			};

			String studyName = studyService.getStudyNameById(app.getStudyId());
			System.out.printf("%-5d | %-20s | %-10s\n", app.getId(), studyName, statusStr);
		}
	}

	// 스터디 삭제
	public void deleteStudyFlow(int userId, int studyId, Scanner scanner) {
		StudyDTO study = studyService.validateOwnership(studyId, userId);
		if (study == null)
			return;

		if (InputUtil.confirm(scanner, "정말 삭제하시겠습니까?")) {
			studyService.deleteStudy(studyId, userId);
			System.out.println("[완료] 스터디가 삭제되었습니다.");
		}

	}

}
