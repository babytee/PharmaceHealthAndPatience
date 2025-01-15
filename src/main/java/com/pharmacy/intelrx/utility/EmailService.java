package com.pharmacy.intelrx.utility;


import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.auxilliary.services.S3Service;
import com.pharmacy.intelrx.pharmacy.dto.UserRequest;
import com.pharmacy.intelrx.pharmacy.models.support.SupportTicket;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;


@RequiredArgsConstructor
@Service
public class EmailService {

    private final Utility utility;
    private final JavaMailSender mailSender;
    private final S3Service s3Service;

    private final TemplateEngine templateEngine;
    @Value("${spring.mail.username}")
    private String mailUsername;

    public void regVerificationEmail(User userRequest, String subject, String link,String message) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        Context context = new Context();
        if(userRequest.getFirstName() != null) {
            context.setVariable("name", userRequest.getFirstName());
        }
        context.setVariable("link", link);
        context.setVariable("subject", subject);
        context.setVariable("message", message);

        //String htmlContent = templateEngine.process("email-template", context);
        String htmlContent = templateEngine.process("emails/registration-verification", context);

        String sanitizedSubject = subject.replaceAll("\\s+", ""); // Remove whitespace
        helper.setFrom(mailUsername,"IntelRx Accounts");

        helper.setTo(userRequest.getEmail());
        helper.setSubject("Please activate your account");
        helper.setText(htmlContent, true);


        mailSender.send(mimeMessage);
    }

    public void resetPasswordEmail(User userRequest, String subject, String link,String message) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        Context context = new Context();
        if(!userRequest.getFirstName().isEmpty() || userRequest.getFirstName() != null) {
            context.setVariable("name", userRequest.getFirstName());
        }
        context.setVariable("link", link);
        context.setVariable("subject", subject);
        context.setVariable("message", message);

        //String htmlContent = templateEngine.process("email-template", context);
        String htmlContent = templateEngine.process("emails/reset-password", context);

        String sanitizedSubject = subject.replaceAll("\\s+", ""); // Remove whitespace
        helper.setFrom(mailUsername,"IntelRx Accounts");

        helper.setTo(userRequest.getEmail());
        helper.setSubject("Reset Forgot Password");
        helper.setText(htmlContent, true);


        mailSender.send(mimeMessage);
    }


    public void verificationWelcomeEmail(User userRequest) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        Context context = new Context();
        if(!userRequest.getFirstName().isEmpty() || userRequest.getFirstName() != null) {
            context.setVariable("name", userRequest.getFirstName());
        }
        String obfuscateEmail = utility.obfuscateEmail(userRequest.getEmail());
        context.setVariable("obfuscateEmail", obfuscateEmail);

        String htmlContent = templateEngine.process("emails/verification-welcome", context);

        helper.setFrom(mailUsername,"IntelRx Accounts");

        helper.setTo(userRequest.getEmail());
        helper.setSubject("Please "+userRequest.getFirstName());
        helper.setText(htmlContent, true);


        mailSender.send(mimeMessage);
    }

    public void employeeInviteEmail(User userRequest, String subject, String link,String message) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        Context context = new Context();
        if(!userRequest.getFirstName().isEmpty() || userRequest.getFirstName() != null) {
            context.setVariable("name", userRequest.getFirstName());
        }
        context.setVariable("link", link);
        context.setVariable("subject", subject);
        context.setVariable("message", message);

        String htmlContent = templateEngine.process("emails/employee-invite", context);

        //String sanitizedSubject = subject.replaceAll("\\s+", ""); // Remove whitespace
        helper.setFrom(mailUsername,"IntelRx Accounts");

        helper.setTo(userRequest.getEmail());
        helper.setSubject("Accept invite & activate your account");
        helper.setText(htmlContent, true);

        mailSender.send(mimeMessage);
    }

    public void appAccessEmail(String to,String subject, String message,String fullName) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        Context context = new Context();
        context.setVariable("subject", subject);
        context.setVariable("message", message);
        context.setVariable("fullName", fullName);

        String htmlContent = templateEngine.process("emails/app-access", context);

        //String sanitizedSubject = subject.replaceAll("\\s+", ""); // Remove whitespace
        helper.setFrom(mailUsername,"App Access");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(mimeMessage);
    }

    public void accountDeletionEmail(String to,String subject, String message) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        Context context = new Context();
        context.setVariable("subject", subject);
        context.setVariable("message", message);

        String htmlContent = templateEngine.process("emails/account-deletion", context);

        //String sanitizedSubject = subject.replaceAll("\\s+", ""); // Remove whitespace
        helper.setFrom(mailUsername,"Account Deletion");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);


        mailSender.send(mimeMessage);
    }

    public void otpAuthEmail(String to,String subject, String message) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        Context context = new Context();
        context.setVariable("subject", subject);
        context.setVariable("message", message);

        //String htmlContent = templateEngine.process("email-template", context);
        String htmlContent = templateEngine.process("emails/otp-auth", context);

        String sanitizedSubject = subject.replaceAll("\\s+", ""); // Remove whitespace
        helper.setFrom(mailUsername,"IntelRx OTP Authentication");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);


        mailSender.send(mimeMessage);
    }

    public void supportTicketRequestEmail(User userRequest, SupportTicket supportTicket) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        Tika tika = new Tika();
        String attachedFile = null;
        String fileType = null;

        if(supportTicket.getAttachedFile()!=null){
            S3Service.FetchedImage fetchedImage = s3Service.fetchImage(supportTicket.getAttachedFile()); // Replace "your_image_name.jpg" with the actual image name
            attachedFile = fetchedImage.getImageUrl();

            String mimeType = tika.detect(attachedFile);

            // Check if the MIME type is an image
            if (mimeType != null && mimeType.startsWith("image/")) {
                fileType = "image";
            } else {
                fileType = "file";
            }

        }


        Context context = new Context();
        if(!userRequest.getFirstName().isEmpty() || userRequest.getFirstName() != null) {
            context.setVariable("name", userRequest.getFirstName());
        }


        context.setVariable("attachedFile", attachedFile);
        context.setVariable("fileType", fileType);

        context.setVariable("supportType", supportTicket.getSupportType().getName());
        context.setVariable("supportTicketId", supportTicket.getTicketNumber());
        context.setVariable("subject", supportTicket.getSubject());
        context.setVariable("description", supportTicket.getDescription());

        String htmlContent = templateEngine.process("emails/support-ticket", context);

        helper.setFrom(mailUsername,"Support Ticket");

        helper.setTo(supportTicket.getEmailAddress());
        helper.setSubject(supportTicket.getSubject());
        helper.setText(htmlContent, true);

        mailSender.send(mimeMessage);
    }


    public void supportTicketResponseEmail(User userRequest, SupportTicket supportTicket) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);


        Context context = new Context();
        if(!userRequest.getFirstName().isEmpty() || userRequest.getFirstName() != null) {
            context.setVariable("name", userRequest.getFirstName());
        }
        context.setVariable("status", supportTicket.getTicketStatus());
        context.setVariable("supportType", supportTicket.getSupportType().getName());
        context.setVariable("supportTicketId", supportTicket.getTicketNumber());
        context.setVariable("subject", supportTicket.getSubject());
        context.setVariable("respondMsg", supportTicket.getRespondMsg());

        String htmlContent = templateEngine.process("emails/support-ticket-response", context);

        helper.setFrom(mailUsername,"IntelRx Support Ticket");

        helper.setTo(supportTicket.getEmailAddress());
        helper.setSubject(supportTicket.getSubject());
        helper.setText(htmlContent, true);

        mailSender.send(mimeMessage);
    }

    public void sendEmail(String to,String name, String subject, String message) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);


        Context context = new Context();
            context.setVariable("name", name);
        context.setVariable("link", "https://inventory.intelrx.io/accept-invite/intelRx_20240603125650532234");
        context.setVariable("message", message);

        //String htmlContent = templateEngine.process("email-template", context);
        String htmlContent = templateEngine.process("emails/forgot-password", context);

        String sanitizedSubject = subject.replaceAll("\\s+", ""); // Remove whitespace
        helper.setFrom(mailUsername,"intelRx-" + sanitizedSubject);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);


        mailSender.send(mimeMessage);
    }

}
