package com.pharmacy.intelrx.utility;

import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.auxilliary.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@RequiredArgsConstructor
@Component
public class Utility {

    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder()
            .appendOptional(DateTimeFormatter.ofPattern("MMMM dd, uuuu", Locale.ENGLISH))
            .appendOptional(DateTimeFormatter.ofPattern("MMM d, uuuu", Locale.ENGLISH))
            .appendOptional(DateTimeFormatter.ofPattern("MMM, uuuu", Locale.ENGLISH))
            .appendOptional(DateTimeFormatter.ofPattern("M/d/uuuu"))
            .appendOptional(DateTimeFormatter.ofPattern("uuuu-MM-dd"))
            .appendOptional(DateTimeFormatter.ofPattern("d-M-yyyy"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-M-d"))
            .toFormatter();

    @Value("${spring.mail.username}")
    private String mailUsername;
    private Map<String, String> otpMap = new HashMap();

    public String obfuscateEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "";
        }

        // Split the email into local part and domain part
        String[] parts = email.split("@");
        if (parts.length == 2) {
            String localPart = parts[0];
            String domainPart = parts[1];

            // Obfuscate the local part, keeping the first and last characters
            String obfuscatedLocalPart = obfuscateString(localPart);

            // Concatenate the obfuscated local part with the domain part
            return obfuscatedLocalPart + "@" + domainPart;
        }

        return email; // Return original email if not in the expected format
    }

    public String obfuscateString(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        // Keep the first and last characters, replace the rest with asterisks
        char firstChar = input.charAt(0);
        char lastChar = input.charAt(input.length() - 2);
        int middleLength = input.length() - 2;
        String obfuscatedMiddle = "*".repeat(middleLength);

        return firstChar + obfuscatedMiddle + lastChar;
    }

    public boolean isInputValid(String input, String regex) {
        return Pattern.compile(regex).matcher(input).matches();
    }

    public boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }


    public String generateOTP(String email) {
        Random random = new Random();
        String otp = String.valueOf(100000 + random.nextInt(900000));

        Optional<User> userOpt = this.userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setOtp(this.passwordEncoder.encode(otp));
            this.userRepository.save(user);
        }


        return String.valueOf(otp);
    }

    public String generateOTPReset(String email) {
        Random random = new Random();
        String otp = String.valueOf(100000 + random.nextInt(900000));

        Optional<User> userOpt = this.userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = (User) userOpt.get();
            user.setOtp(this.passwordEncoder.encode(otp));
            this.userRepository.save(user);
        }

        return String.valueOf(otp);
    }

    public boolean validateOTP(String email, String otp) {

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            boolean storedOTP = this.passwordEncoder.matches(otp, user.getOtp());
            if (storedOTP) {
                return true;
            }
        }
        return false;
    }


    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailUsername);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        this.mailSender.send(message);
    }

    public void markEmailAsVerified(String email) {
        this.otpMap.remove(email);
    }

    public boolean isValidEmail(String email) {
        // Define the regular expression pattern for a basic email address
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

        // Create a Pattern object
        Pattern pattern = Pattern.compile(emailRegex);

        // Create a Matcher object
        Matcher matcher = pattern.matcher(email);

        // Return true if the email matches the pattern
        return matcher.matches();
    }

    public String generateEmployeeIntelRxId() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String timestamp = dateFormat.format(new Date());
        Random random = new Random();
        int randomNum = random.nextInt(1000);
        String employeeIntelRxId = timestamp + String.format("%03d", randomNum);
        return employeeIntelRxId;
    }

    public String generateUniqueInvoiceRefNumber() {
        // You can customize the format of the invoice reference number based on your requirements
        // For example, using the current timestamp and a random UUID
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8); // Use a portion of the UUID

        return "iRXiNV-" + timestamp + "-" + uuid;
    }

    public LocalDate convertStringToLocalDate(String dateString){
        return LocalDate.parse(dateString, DATE_FORMATTER);
    }

    public String addHoursAndFormat(String createdAt, String hoursToAdd) {
        // Parse the createdAt string to LocalDateTime
        LocalDateTime dateTime = LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_DATE_TIME);

        // Add hours to the createdAt LocalDateTime
        dateTime = dateTime.plusHours(Long.parseLong(hoursToAdd.split(" ")[0]));

        // Format the LocalDateTime to display in AM/PM format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a");
        String formattedDateTime = dateTime.format(formatter);

        return formattedDateTime;
    }

    public double roundAmount(double value) {
        // Handle the special case where the value is 0 or 0.0
        if (value == 0.0) {
            return 0.00;
        }

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        // Ensure the double value has two decimal places by formatting it as a string and parsing it back
        return Double.parseDouble(String.format("%.2f", bd.doubleValue()));
    }


    public boolean isNigerianPhoneNumber(String phoneNumber) {
        // Regular expression for Nigerian phone number
        String regex = "^(\\+234|0)([789][01]\\d{8})$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }
}
