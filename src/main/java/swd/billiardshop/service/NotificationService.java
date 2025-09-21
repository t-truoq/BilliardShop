package swd.billiardshop.service;

import lombok.Builder;
import org.springframework.stereotype.Service;
import swd.billiardshop.entity.Notification;
import java.util.List;
@Builder
@Service
public class NotificationService {
    public List<Notification> getAllNotifications() {
        return null;
    }
}
