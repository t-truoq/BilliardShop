package swd.billiardshop.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import swd.billiardshop.dto.request.MailBody;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // Send OTP for password reset or email verification
    @Async
    public void sendOtp(MailBody mailBody) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(mailBody.getTo());
            helper.setFrom(fromEmail);
            helper.setSubject(mailBody.getSubject());

            String otp = extractOtpFromText(mailBody.getText());
            String otpDisplay = (otp != null && !"N/A".equals(otp) && !otp.trim().isEmpty()) ? otp : "Không tìm thấy OTP";

            String htmlContent = buildOtpHtml(otpDisplay);

            helper.setText(htmlContent, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Gửi email thất bại: " + e.getMessage());
        }
    }

    // Generic notification email (e.g., password changed, welcome, verification successful)
    @Async
    public void sendNotification(String to, String subject, String body) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setFrom(fromEmail);
            helper.setSubject(subject);

            String html = buildNotificationHtml(body);
            helper.setText(html, true);

            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Gửi email thất bại: " + e.getMessage());
        }
    }

    private String buildOtpHtml(String otpDisplay) {
        return "<!DOCTYPE html>" +
                "<html lang=\"vi\">" +
                "<head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><title>BilliardShop - Xác thực</title>" +
                "<style>body{font-family:Arial,sans-serif;background:#f7f7f7;color:#222} .card{max-width:600px;margin:24px auto;background:#fff;padding:24px;border-radius:8px;box-shadow:0 6px 18px rgba(0,0,0,.08)} .brand{color:#0a58ca;font-weight:700;font-size:24px} .otp{background:#0a58ca;color:#fff;padding:14px;border-radius:6px;font-size:22px;letter-spacing:4px;text-align:center;margin:18px 0} .note{color:#666;font-size:13px}</style></head>" +
                "<body><div class=\"card\"><div class=\"brand\">BilliardShop</div><p class=\"note\">Bạn (hoặc ai đó) vừa yêu cầu đặt lại mật khẩu / xác thực email. Vui lòng sử dụng mã bên dưới để tiếp tục.</p>" +
                "<div class=\"otp\">" + otpDisplay + "</div>" +
                "<p class=\"note\">Mã có hiệu lực trong 10 phút. Nếu bạn không yêu cầu, hãy bỏ qua email này.</p>" +
                "<p class=\"note\">Trân trọng,<br/>Đội ngũ BilliardShop</p></div></body></html>";
    }

    private String buildNotificationHtml(String body) {
        return "<!DOCTYPE html>" +
                "<html lang=\"vi\">" +
                "<head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><title>BilliardShop - Thông báo</title>" +
                "<style>body{font-family:Arial,sans-serif;background:#f7f7f7;color:#222} .card{max-width:600px;margin:24px auto;background:#fff;padding:24px;border-radius:8px;box-shadow:0 6px 18px rgba(0,0,0,.08)} .brand{color:#0a58ca;font-weight:700;font-size:24px} .content{color:#333;font-size:14px;line-height:1.5}</style></head>" +
                "<body><div class=\"card\"><div class=\"brand\">BilliardShop</div><div class=\"content\"><p>" + escapeHtml(body) + "</p><p>Trân trọng,<br/>Đội ngũ BilliardShop</p></div></div></body></html>";
    }

    // Simple OTP extractor: looks for the last group of digits in the text
    private String extractOtpFromText(String text) {
        if (text == null) return "N/A";
        String trimmed = text.trim();
        // Try to find sequences of 4-8 digits
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d{4,8})").matcher(trimmed);
        String last = null;
        while (m.find()) last = m.group(1);
        return last != null ? last : "N/A";
    }

    // Minimal HTML-escape for notification body
    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}
