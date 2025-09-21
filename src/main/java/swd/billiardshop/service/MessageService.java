package swd.billiardshop.service;

import lombok.Builder;
import org.springframework.stereotype.Service;
import swd.billiardshop.entity.Message;
import java.util.List;
@Builder
@Service
public class MessageService {
    public List<Message> getAllMessages() {
        return null;
    }
}
