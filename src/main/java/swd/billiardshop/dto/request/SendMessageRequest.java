package swd.billiardshop.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

class SendMessageRequest {
    private String content;
    private String messageType; // TEXT, IMAGE, FILE
}
