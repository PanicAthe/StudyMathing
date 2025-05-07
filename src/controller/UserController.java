package controller;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import common.Session;
import dto.UserDTO;
import service.UserService;
import util.InputUtil;

public class UserController {

	private final UserService userService = new UserService();
	private final Session session;

	public UserController(Session session) {
		this.session = session;
	}

	// 기본 마이페이지
	public void myPageFlow(Scanner scanner) {
		while (true) {
			System.out.println("\n\n===================================");
			System.out.println("           마이페이지");
			System.out.println("===================================");
			printUserProfile();
			System.out.println("\n[메뉴]");
			System.out.println("  [1]   내 정보 수정");
			System.out.println("  [2]   비밀번호 변경");
			System.out.println("  [3]   회원 탈퇴");
			System.out.println("  [0]   뒤로가기");

			int choice = InputUtil.inputInt(scanner, "\n선택 >>> ");

			switch (choice) {
			case 1 -> updateProfileFlow(scanner);
			case 2 -> changePasswordFlow(scanner);
			case 3 -> {deleteAccountFlow(scanner);
				return;}
			case 0 -> {
				return;
			}
			default -> System.out.println("[에러] 잘못된 입력입니다.");
			}
		}
	}

	// 회원가입
	public void signUp(Scanner scanner) {
		System.out.println("\n===== 회원가입 =====");

		String email;
		while (true) {
			email = InputUtil.inputString(scanner, "이메일 입력: ");
			if (!userService.isValidEmail(email)) {
				System.out.println("[에러] 이메일 형식이 올바르지 않습니다.");
				continue;
			}
			if (userService.isEmailExists(email)) {
				System.out.println("[에러] 이미 사용 중인 이메일입니다.");
				continue;
			}
			break;
		}

		String password;
		while (true) {
			password = InputUtil.inputString(scanner, "비밀번호 입력 (8자 이상, 영문+숫자+특수문자): ");
			if (!userService.isValidPassword(password)) {
				System.out.println("[에러] 비밀번호 형식이 올바르지 않습니다.");
				continue;
			}
			break;
		}

		String name = InputUtil.inputString(scanner, "이름 입력: ");

		boolean result = userService.signUp(email, password, name);
		if (result) {
			System.out.println("  회원가입이 완료되었습니다! 환영합니다!");
		} else {
			System.out.println("[에러] 회원가입 처리 중 오류가 발생했습니다.");
		}
	}

	// 로그인
	public void login(Scanner scanner) {
		System.out.println("\n  [ 로그인 ]");
		String email = InputUtil.inputString(scanner, "이메일: ");
		String password = InputUtil.inputString(scanner, "비밀번호: ");

		int userId = userService.login(email, password);
		if (userId > 0) {
			session.login(userId); // 세션에 로그인 정보
			System.out.println("  로그인 성공!");
		} else {
			System.out.println("[에러] 로그인 실패. 이메일 또는 비밀번호를 확인하세요.");
		}
	}

	// 내 정보 수정
	public void updateProfileFlow(Scanner scanner) {
		System.out.println("\n===== 내 정보 수정 =====");

		String newName = InputUtil.inputOptionalString(scanner, "새 이름 입력 (엔터시 유지): ");
		Integer newAge = InputUtil.inputOptionalInt(scanner, "새 나이 입력 (엔터시 유지): ");
		String newBio = InputUtil.inputOptionalString(scanner, "새 자기소개 입력 (엔터시 유지): ");
		Double newLat = InputUtil.inputOptionalDouble(scanner, "새 위도 입력 (엔터시 유지): ");
		Double newLon = InputUtil.inputOptionalDouble(scanner, "새 경도 입력 (엔터시 유지): ");

		Integer newGender = null;
		String genderInput = InputUtil.inputOptionalString(scanner, "새 성별 입력 (0: 남성, 1: 여성 / 엔터시 유지): ");
		if (genderInput != null) {
			if (genderInput.matches("[01]")) {
				newGender = Integer.parseInt(genderInput);
			} else {
				System.out.println("[에러] 성별은 0, 1 중 하나만 입력 가능합니다.");
				return;
			}
		}

		boolean updated = userService.updateProfile(session.getUserId(), newName, newAge, newBio, newLat, newLon,
				newGender);

		if (updated) {
			System.out.println("  내 정보가 수정되었습니다.");
		} else {
			System.out.println("[에러] 정보 수정에 실패했습니다.");
			return;
		}

		if (InputUtil.confirm(scanner, "\n관심 태그도 수정하시겠습니까?")) {
			String tagInput = InputUtil.inputString(scanner, "관심 태그 입력 (쉼표 구분): ");
			List<String> tagList = Arrays.stream(tagInput.split(",")).map(String::trim).map(String::toLowerCase)
					.filter(s -> !s.isBlank()).toList();
			userService.updateInterestTags(session.getUserId(), tagList);
			System.out.println("  관심 태그가 수정되었습니다.");
		}
	}

	// 비번 변경
	public void changePasswordFlow(Scanner scanner) {
		System.out.println("\n===== 비밀번호 변경 =====");

		String currentPassword = InputUtil.inputString(scanner, "현재 비밀번호 입력: ");
		String newPassword = InputUtil.inputString(scanner, "새 비밀번호 입력 (8자 이상, 영문+숫자+특수문자): ");

		boolean result = userService.changePassword(session.getUserId(), currentPassword, newPassword);

		if (result) {
			System.out.println(" 비밀번호가 성공적으로 변경되었습니다.");
		} else {
			System.out.println("[에러] 현재 비밀번호가 일치하지 않거나 새 비밀번호가 유효하지 않습니다.");
		}
	}

	// 계정 삭제
	public void deleteAccountFlow(Scanner scanner) {

		if (!InputUtil.confirm(scanner, "정말로 탈퇴하시겠습니까?")) {
			System.out.println("회원 탈퇴를 취소했습니다.");
			return;
		}

		boolean result = userService.deleteAccount(session.getUserId());
		if (result) {
			System.out.println("  회원 탈퇴가 완료되었습니다.");
			session.logout();
		} else {
			System.out.println("[에러] 탈퇴 처리 중 문제가 발생했습니다.");
		}
	}

	// 유저 정보 출력
	private void printUserProfile() {
		UserDTO user = userService.getUserProfile(session.getUserId());
		if (user == null) {
			System.out.println("[오류] 사용자를 찾을 수 없습니다.");
			return;
		}

		System.out.println("\n==========  내 정보 ==========");
		System.out.println("  이름     : " + user.getName());
		System.out.println("  이메일   : " + user.getEmail());
		System.out.println("  나이     : " + (user.getAge() != null ? user.getAge() : "입력 안 됨"));
		System.out.println("  성별     : " + (user.getGender() != null ? genderToString(user.getGender()) : "입력 안 됨"));
		System.out.println("  거주지   : 위도 %.6f, 경도 %.6f".formatted(user.getLatitude(), user.getLongitude()));
		System.out.println("  자기소개 : " + (user.getBio() != null ? user.getBio() : "입력 안 됨"));

		List<String> tagNames = userService.getInterestTagNames(session.getUserId());
		if (tagNames.isEmpty()) {
			System.out.println("  관심 태그 : (없음)");
		} else {
			System.out.print("  관심 태그 : ");
			tagNames.forEach(t -> System.out.print("#" + t + " "));
			System.out.println();
		}
	}

	// 성별(int -> string)
	private String genderToString(Integer gender) {
		return switch (gender) {
		case 0 -> "남성";
		case 1 -> "여성";
		case 2 -> "기타";
		default -> "입력 안 됨";
		};
	}

}