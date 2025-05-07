package service;

import dao.NotificationDAO;
import dto.NotificationDTO;

import java.util.List;

public class NotificationService {

    private final NotificationDAO notificationDAO = new NotificationDAO();

    // 알림 목록 가져오기
    public List<NotificationDTO> getNotifications(int userId) {
        return notificationDAO.findNotificationsByUserId(userId);
    }

    // 알림 읽음 처리
    public void markNotificationsAsRead(int userId) {
        notificationDAO.markAllNotificationsAsRead(userId);
    }
}
