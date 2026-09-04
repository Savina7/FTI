package Savina.ftiApp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@fti.edu.al}")
    private String fromEmail;

    /**
     * Sends a 6-digit OTP verification code to the given email address via SMTP.
     * Logs the code in console for easy local development testing if SMTP fails.
     */
    public void sendVerificationCode(String toEmail, String recipientName, String code) {
        log.info("==================================================================");
        log.info(">>> KODI I VERIFIKIMIT PER [{}]: {} <<<", toEmail, code);
        log.info("==================================================================");

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("FTI App — Kodi juaj i Verifikimit");

            String htmlContent = """
                    <div style="font-family: 'Segoe UI', Arial, sans-serif; max-width: 480px; margin: 0 auto; padding: 24px;">
                        <div style="text-align: center; margin-bottom: 24px;">
                            <h2 style="color: #1a56db; margin: 0;">FTI App</h2>
                            <p style="color: #6b7280; margin: 4px 0 0;">Sistemi Akademik</p>
                        </div>
                        <div style="background: #f9fafb; border-radius: 8px; padding: 24px; text-align: center;">
                            <p style="color: #374151; margin: 0 0 16px;">Mire se vini, <strong>%s</strong>!</p>
                            <p style="color: #374151; margin: 0 0 20px;">
                                Kodi juaj i verifikimit eshte:
                            </p>
                            <div style="background: #1a56db; color: white; font-size: 32px; font-weight: bold;
                                        letter-spacing: 12px; padding: 16px 24px; border-radius: 6px; display: inline-block;">
                                %s
                            </div>
                            <p style="color: #6b7280; font-size: 13px; margin: 20px 0 0;">
                                Ky kod skadon pas <strong>15 minutash</strong>.<br>
                                Nese nuk jeni ju, injorojeni kete email.
                            </p>
                        </div>
                        <p style="color: #9ca3af; font-size: 12px; text-align: center; margin-top: 16px;">
                            © FTI App — Fakulteti i Teknologjise se Informacionit
                        </p>
                    </div>
                    """.formatted(recipientName, code);

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Email me kodin e verifikimit u dergua me sukses te: {}", toEmail);

        } catch (Exception e) {
            log.warn("Nuk u dergua dot email me SMTP (kontrolloni fjalekalimin te application.properties). Kodi per testim eshte i shfaqur me siper te konzola: {}", e.getMessage());
        }
    }

    /**
     * Sends a 6-digit OTP code to reset password via SMTP.
     */
    public void sendPasswordResetCode(String toEmail, String recipientName, String code) {
        log.info("==================================================================");
        log.info(">>> KODI I RIVENDOSJES SE FJALEKALIMIT PER [{}]: {} <<<", toEmail, code);
        log.info("==================================================================");

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("FTI App — Rivendosja e Fjalekalimit");

            String displayName = (recipientName != null && !recipientName.isBlank()) ? recipientName : "Perdorues";

            String htmlContent = """
                    <div style="font-family: 'Segoe UI', Arial, sans-serif; max-width: 480px; margin: 0 auto; padding: 24px;">
                        <div style="text-align: center; margin-bottom: 24px;">
                            <h2 style="color: #1a56db; margin: 0;">FTI App</h2>
                            <p style="color: #6b7280; margin: 4px 0 0;">Sistemi Akademik</p>
                        </div>
                        <div style="background: #f9fafb; border-radius: 8px; padding: 24px; text-align: center;">
                            <p style="color: #374151; margin: 0 0 16px;">Pershendetje, <strong>%s</strong>!</p>
                            <p style="color: #374151; margin: 0 0 20px;">
                                Keni kerkuar rivendosjen e fjalekalimit tuaj. Kodi juaj i sigurise eshte:
                            </p>
                            <div style="background: #1a56db; color: white; font-size: 32px; font-weight: bold;
                                        letter-spacing: 12px; padding: 16px 24px; border-radius: 6px; display: inline-block;">
                                %s
                            </div>
                            <p style="color: #6b7280; font-size: 13px; margin: 20px 0 0;">
                                Ky kod skadon pas <strong>15 minutash</strong>.<br>
                                Nese nuk e keni kerkuar ju kete veprim, ju lutemi injorojeni kete email.
                            </p>
                        </div>
                        <p style="color: #9ca3af; font-size: 12px; text-align: center; margin-top: 16px;">
                            © FTI App — Fakulteti i Teknologjise se Informacionit
                        </p>
                    </div>
                    """.formatted(displayName, code);

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Email me kodin e rivendosjes u dergua me sukses te: {}", toEmail);

        } catch (Exception e) {
            log.warn("Nuk u dergua dot email me SMTP (kontrolloni fjalekalimin te application.properties). Kodi per testim eshte i shfaqur me siper te konzola: {}", e.getMessage());
        }
    }
}
