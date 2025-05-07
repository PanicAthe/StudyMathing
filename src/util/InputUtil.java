package util;

import java.util.Scanner;

public class InputUtil {

	public static int inputInt(Scanner scanner, String message) {
		while (true) {
			try {
				System.out.print(message);
				return Integer.parseInt(scanner.nextLine().trim());
			} catch (NumberFormatException e) {
				System.out.println("[에러] 숫자를 입력해주세요.");
			}
		}
	}

	public static Integer inputOptionalInt(Scanner scanner, String message) {
		System.out.print(message);
		String input = scanner.nextLine().trim();
		if (input.isBlank())
			return null;

		try {
			return Integer.parseInt(input);
		} catch (NumberFormatException e) {
			System.out.println("[에러] 숫자 형식이 아닙니다.");
			return inputOptionalInt(scanner, message); // 재귀 재시도
		}
	}

	public static String inputString(Scanner scanner, String message) {
		System.out.print(message);
		return scanner.nextLine().trim();
	}

	public static String inputOptionalString(Scanner scanner, String message) {
		System.out.print(message);
		String input = scanner.nextLine().trim();
		return input.isEmpty() ? null : input;
	}

	public static Double inputDouble(Scanner scanner, String message) {
		while (true) {
			try {
				System.out.print(message);
				return Double.parseDouble(scanner.nextLine().trim());
			} catch (NumberFormatException e) {
				System.out.println("[에러] 숫자 형식이 아닙니다. 예: 37.123");
			}
		}
	}

	public static Double inputOptionalDouble(Scanner scanner, String message) {
		System.out.print(message);
		String input = scanner.nextLine().trim();
		if (input.isBlank())
			return null;

		try {
			return Double.parseDouble(input);
		} catch (NumberFormatException e) {
			System.out.println("[에러] 숫자 형식이 아닙니다.");
			return inputOptionalDouble(scanner, message);
		}
	}

	public static boolean confirm(Scanner scanner, String message) {
		String input = inputString(scanner, message + " (y/n): ");
		return input.equalsIgnoreCase("y");
	}

	public static int inputIntInRange(Scanner scanner, String message, int min, int max) {
		while (true) {
			int value = inputInt(scanner, message);
			if (value >= min && value <= max)
				return value;
			System.out.println("[에러] " + min + "부터 " + max + " 사이 숫자를 입력하세요.");
		}
	}

	public static Integer inputOptionalIntInRange(Scanner scanner, String message, int min, int max) {
		String input = inputOptionalString(scanner, message);
		if (input == null)
			return null;

		try {
			int value = Integer.parseInt(input);
			if (value < min || value > max) {
				System.out.println("[에러] " + min + "부터 " + max + " 사이 숫자만 입력하세요.");
				return inputOptionalIntInRange(scanner, message, min, max);
			}
			return value;
		} catch (NumberFormatException e) {
			System.out.println("[에러] 숫자 형식이 아닙니다.");
			return inputOptionalIntInRange(scanner, message, min, max);
		}
	}

}
