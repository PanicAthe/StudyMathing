package service;

import dao.TagDAO;
import dao.UserDAO;
import dao.UserTagDAO;
import dto.UserDTO;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class UserService {

	private final UserDAO userDAO = new UserDAO();

	public boolean signUp(String email, String password, String name) {
		UserDTO user = UserDTO.builder().email(email).password(password).name(name).build();
		userDAO.insertUser(user);
		return true;
	}

	public boolean isEmailExists(String email) {
		return userDAO.isEmailExists(email);
	}

	public boolean isValidEmail(String email) {
		return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
	}

	public boolean isValidPassword(String password) {
		return password.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$");
	}

	public int login(String email, String password) {
		UserDTO user = userDAO.findByEmail(email);
		return (user != null && user.getPassword().equals(password)) ? user.getId() : 0;
	}

	public UserDTO getUserProfile(int userId) {
		return userDAO.findById(userId);
	}

	public List<String> getInterestTagNames(int userId) {
		try {
			UserTagDAO userTagDAO = new UserTagDAO();
			TagDAO tagDAO = new TagDAO();

			List<Integer> tagIds = userTagDAO.findTagIdsByUserId(userId);

			return tagIds.stream().map(tagId -> {
				try {
					return tagDAO.findTagNameById(tagId);
				} catch (Exception e) {
					return null;
				}
			}).filter(Objects::nonNull).toList();
		} catch (Exception e) {
			System.out.println("[에러] 관심 태그 조회 실패");
			return List.of();
		}
	}

	public boolean updateProfile(int userId, String name, Integer age, String bio, Double lat, Double lon,
			Integer gender) {
		try {
			userDAO.updateProfile(userId, name, age, bio, lat, lon, gender);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public void updateInterestTags(int userId, List<String> tagNames) {
		UserTagDAO userTagDAO = new UserTagDAO();
		TagDAO tagDAO = new TagDAO();

		userTagDAO.deleteUserTags(userId);

		for (String tagName : tagNames) {
			try {
				Integer tagId = tagDAO.findTagIdByName(tagName);
				if (tagId == null) {
					tagId = tagDAO.insertTag(tagName);
				}
				userTagDAO.insertUserTag(userId, tagId);
			} catch (SQLException e) {
				System.out.println("[에러] 태그 처리 중 오류: " + tagName);
			}
		}
	}

	public boolean changePassword(int userId, String currentPassword, String newPassword) {
		UserDTO user = userDAO.findById(userId);
		if (user == null)
			return false;

		boolean currentMatches = user.getPassword().equals(currentPassword);
		boolean newValid = isValidPassword(newPassword);

		if (!currentMatches || !newValid)
			return false;

		userDAO.updatePassword(userId, newPassword);
		return true;
	}

	// 회원 탈퇴
	public boolean deleteAccount(int userId) {
		try {
			userDAO.deleteUser(userId);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}


}
