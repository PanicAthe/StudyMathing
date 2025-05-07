package controller;

import dto.NotificationDTO;
import service.NotificationService;

import java.util.List;

import common.Session;

public class NotiController {

    private final NotificationService notificationService = new NotificationService();
    private final Session session;
    
    public NotiController(Session session) {
    	this.session = session;
    }

    // 유저의 읽지 않은 알람 가져오고 읽음 처리.
	public void getNotiByUserId() {
        List<NotificationDTO> notifications = notificationService.getNotifications(session.getUserId());

        if (notifications.isEmpty()) {
            return;
        }

        System.out.println("\n\n===================================");
        System.out.println("          알림 목록");
        System.out.println("===================================");

        for (NotificationDTO noti : notifications) {
            if(!noti.isRead()) {
            	System.out.printf("[%s] %s %s\n",
                        noti.getId(),
                        (noti.getType() == 0 ? "가입신청" : "신청결과"),
                        noti.getMessage());
            }
        }

        System.out.println();
        notificationService.markNotificationsAsRead(session.getUserId());
    }
}
