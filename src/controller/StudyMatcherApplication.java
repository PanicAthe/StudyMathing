package controller;

import java.util.Scanner;

import common.Session;
import util.InputUtil;

public class StudyMatcherApplication {

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		Session session = new Session(); // 로그인 정보 저장
		UserController userController = new UserController(session);
		NotiController notiController = new NotiController(session);
		StudyController studyController = new StudyController(session);

		while (true) {
			defaultMenu();
			int choice = InputUtil.inputInt(scanner, "");

			switch (choice) {
			case 1 -> userController.signUp(scanner);
			case 2 -> {
				userController.login(scanner);
				if (session.isLoggedIn()) {
					notiController.getNotiByUserId();
				}
			}
			case 0 -> {
				System.out.println("프로그램 종료");
				return;
			}
			}

			while (session.isLoggedIn()) {
				loginMenu();
				choice = InputUtil.inputInt(scanner, "");
				switch (choice) {
				case 1 -> userController.myPageFlow(scanner);
				case 2 -> studyController.studyMenu(scanner);
				case 0 -> {
					System.out.println("로그아웃합니다.");
					session.logout();
				}
				}
			}
		}
	}

	static void defaultMenu() {
		System.out.println("\n\n===================================");
		System.out.println("           Study Matcher ");
		System.out.println("===================================");
		System.out.println("  [1] 회원가입");
		System.out.println("  [2] 로그인");
		System.out.println("  [0] 종료");
		System.out.print("\n선택 >>> ");
	}

	static void loginMenu() {
		System.out.println("\n\n===================================");
		System.out.println("           로그인 메뉴");
		System.out.println("===================================");
		System.out.println("  [1]  마이페이지");
		System.out.println("  [2]  스터디 보기");
		System.out.println("  [0]  로그아웃");
		System.out.print("\n선택 >>> ");
	}
}
