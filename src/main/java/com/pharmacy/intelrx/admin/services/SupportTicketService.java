package com.pharmacy.intelrx.admin.services;

import com.pharmacy.intelrx.admin.models.Admin;
import com.pharmacy.intelrx.admin.repositories.AdminRepository;
import com.pharmacy.intelrx.auxilliary.dto.RegisterRequest;
import com.pharmacy.intelrx.auxilliary.dto.Response;
import com.pharmacy.intelrx.auxilliary.services.S3Service;
import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.PharmacyRequest;
import com.pharmacy.intelrx.pharmacy.dto.SupportTicketFilterRequest;
import com.pharmacy.intelrx.pharmacy.dto.SupportTicketRequest;
import com.pharmacy.intelrx.pharmacy.dto.SupportTicketResponse;
import com.pharmacy.intelrx.pharmacy.models.CartItem;
import com.pharmacy.intelrx.pharmacy.models.Pharmacy;
import com.pharmacy.intelrx.pharmacy.models.PharmacyBranch;
import com.pharmacy.intelrx.pharmacy.models.support.SupportTicket;
import com.pharmacy.intelrx.pharmacy.models.support.SupportType;
import com.pharmacy.intelrx.pharmacy.repositories.PharmacyRepository;
import com.pharmacy.intelrx.pharmacy.repositories.SupportTicketRepository;
import com.pharmacy.intelrx.pharmacy.repositories.SupportTypeRepository;
import com.pharmacy.intelrx.pharmacy.utility.PharmacyMapping;
import com.pharmacy.intelrx.utility.EmailService;
import com.pharmacy.intelrx.utility.Utility;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service("AdminSupportTicketService")
public class SupportTicketService {
    private final SupportTypeRepository supportTypeRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final PharmacyRepository pharmacyRepository;
    private final PharmacyMapping pharmacyMapping;
    private final AdminRepository adminRepository;
    private final S3Service s3Service;
    private final Utility utility;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public ResponseEntity<?> ticketsOverview() {
        Long totalTickets = supportTicketRepository.countTotalTickets(null);

        Long pendingTickets = supportTicketRepository.countPendingTickets(null);

        Long resolvedTickets = supportTicketRepository.countResolvedTickets(null);

        Map<String, Long> ticketsCount = new HashMap<>();
        ticketsCount.put("totalTickets", totalTickets == null ? 0 : totalTickets);
        ticketsCount.put("pendingTickets", pendingTickets == null ? 0 : pendingTickets);
        ticketsCount.put("resolvedTickets", resolvedTickets == null ? 0 : resolvedTickets);

        return ResponseEntity.ok(StandardResponse.success(ticketsCount));

    }

    public ResponseEntity<?> filtersSupportTicket(SupportTicketFilterRequest request, Pageable pageable) {

        SupportType supportType = null;
        if (request.getSupportTypeId() != null) {
            Optional<SupportType> optionalSupportType = supportTypeRepository.findById(request.getSupportTypeId());
            if (optionalSupportType.isPresent()) {
                supportType = optionalSupportType.get();
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Support Type ID");
            }
        }

        // Check for null or empty Page object
        Page<SupportTicket> supportTickets = supportTicketRepository.findByFilters(
                supportType,
                request.getIntelRxId(),
                request.getState(),  // Correct order
                request.getKeyword(), // Correct order
                pageable);

//        if (supportTickets == null || supportTickets.isEmpty()) {
//            // Return success response with empty supportTickets
//            return ResponseEntity.ok(StandardResponse.success(supportTickets));
//        }

        Page<SupportTicketResponse> ticketResponses = new PageImpl<>(
                supportTickets.stream().map(supportTicket -> {
                    try {
                        return mapToSupportTicketResponse(supportTicket);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList()),
                pageable,
                supportTickets.getTotalElements());

        return ResponseEntity.ok(StandardResponse.success(ticketResponses));
    }

    public ResponseEntity<?> fetchTicket(SupportTicketFilterRequest request) throws IOException {

        Optional<SupportTicket> optional = supportTicketRepository.findByIdAndIntelRxId(
                request.getId(), request.getIntelRxId()
        );

        if (!optional.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.success("Support Ticket Not Found"));
        }

        SupportTicket supportTicket = optional.get();
        SupportTicketResponse ticketResponse = mapToSupportTicketResponse(supportTicket);

        return ResponseEntity.ok(StandardResponse.success(ticketResponse));

    }

    private SupportTicketResponse mapToSupportTicketResponse(SupportTicket supportTicket) throws IOException {
        SupportType supportType = null;
        if (supportTicket.getSupportType() != null) {
            Optional<SupportType> optionalSupportType = supportTypeRepository.findById(supportTicket.getSupportType().getId());
            supportType = optionalSupportType.get();
        }

        PharmacyRequest pharmacyRequest = null;
        if (supportTicket.getIntelRxId() != null) {
            Optional<Pharmacy> optionalPharmacy = pharmacyRepository.findByIntelRxId(supportTicket.getIntelRxId());
            Pharmacy pharmacy = optionalPharmacy.get();
            pharmacyRequest = pharmacyMapping.mapToPharmacy(pharmacy);
        }

        RegisterRequest registerRequest = new RegisterRequest();
        Admin admin = null;
        if (supportTicket.getResolvedBy() != null) {
            Optional<Admin> optionalAdmin = adminRepository.findById(supportTicket.getResolvedBy().getId());
             admin = optionalAdmin.get();
            registerRequest.setId(admin.getId());
            registerRequest.setFirstname(admin.getFirstname());
            registerRequest.setLastname(admin.getLastname());
            registerRequest.setEmail(admin.getEmail());
        }


        String attachedFile = null;
        if (!utility.isNullOrEmpty(supportTicket.getAttachedFile())) {
            attachedFile = s3Service.uploadFileDoc(supportTicket.getAttachedFile(), "pharmacy");
        }


        return SupportTicketResponse.builder()
                .id(supportTicket.getId())
                .ticketStatus(supportTicket.getTicketStatus())
                .respondMsg(supportTicket.getRespondMsg())
                .ticketNumber(supportTicket.getTicketNumber())
                .description(supportTicket.getDescription())
                .supportType(supportType)
                .attachedFile(attachedFile)
                .emailAddress(supportTicket.getEmailAddress())
                .pharmacyInfo(pharmacyRequest)
                .subject(supportTicket.getSubject())
                .resolvedBy(registerRequest)
                .createdAt(supportTicket.getCreatedAt())
                .build();

    }

    public ResponseEntity<?> changeStatus(SupportTicketFilterRequest request, Long id) throws MessagingException, UnsupportedEncodingException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.ok().body(new Response("failed", "You are unauthorized"));
        } else if (utility.isNullOrEmpty(request.getTicketStatus())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("ticketStatus is required"));
        } else if (utility.isNullOrEmpty(request.getIntelRxId())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId is required"));
        } else if (id == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("id is required"));
        } else if (!request.getTicketStatus().equals("Pending") && !request.getTicketStatus().equals("Resolved")) {
            return ResponseEntity.badRequest().body(StandardResponse.error("ticketStatus must be Pending or Resolved"));
        }

        String userEmail = authentication.getName();
        Optional<Admin> adminOptional = adminRepository.findByEmail(userEmail);
        if (!adminOptional.isPresent()) {
            return ResponseEntity.ok().body(new Response("failed", "User Not Found"));
        }
        Admin admin = adminOptional.get();
        Optional<SupportTicket> optional = supportTicketRepository.findByIdAndIntelRxId(
                id, request.getIntelRxId()
        );

        if (!optional.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.success("Support Ticket Not Found"));
        }

        boolean check_password = this.passwordEncoder.matches(request.getPassword(), admin.getPassword());

        if (!check_password) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Password not match"));
        }

        SupportTicket supportTicket = optional.get();
        supportTicket.setTicketStatus(request.getTicketStatus());
        supportTicket.setResolvedBy(admin);
        supportTicket.setRespondMsg(request.getRespondMsg() == null ? null : request.getRespondMsg());

        supportTicketRepository.save(supportTicket);

        Optional<Pharmacy>optionalPharmacy = pharmacyRepository.findByIntelRxId(supportTicket.getIntelRxId());
        Pharmacy pharmacy = optionalPharmacy.get();
        emailService.supportTicketResponseEmail(pharmacy.getUser(),supportTicket);
        return ResponseEntity.ok(StandardResponse.success("Response Sent"));

    }

    private Map yearlyInsight(int year, int month) {
        // Use the provided month if it is greater than 0, otherwise use the current month
        int targetMonth = (month > 0) ? month : YearMonth.now().getMonthValue();

        Map<String, Long> supportAnalytics = new HashMap<>();
        long totalTickets = 0;

        List<SupportType> supportTypes = supportTypeRepository.findAll();
        for (SupportType supportType : supportTypes) {
            totalTickets = supportTicketRepository.countBySupportTypeAndIntelRxIdAndCreatedAtYearAndCreatedAtMonth
                    (supportType, null, year, targetMonth);
            supportAnalytics.put(supportType.getName(), totalTickets == 0 ? 0 : totalTickets);
        }

        return supportAnalytics;
    }

    public ResponseEntity<?> yearlyAnalytics(int year) {
        LinkedHashMap<String, Object> insight = new LinkedHashMap<>();
        insight.put("Year", year < 1 ? YearMonth.now().getYear() : year);
        insight.put("Jan", yearlyInsight(year, 1));
        insight.put("Feb", yearlyInsight(year, 2));
        insight.put("Mar", yearlyInsight(year, 3));
        insight.put("Apr", yearlyInsight(year, 4));
        insight.put("May", yearlyInsight(year, 5));
        insight.put("Jun", yearlyInsight(year, 6));
        insight.put("Jul", yearlyInsight(year, 7));
        insight.put("Aug", yearlyInsight(year, 8));
        insight.put("Sep", yearlyInsight(year, 9));
        insight.put("Oct", yearlyInsight(year, 10));
        insight.put("Nov", yearlyInsight(year, 11));
        insight.put("Dec", yearlyInsight(year, 12));

        return ResponseEntity.ok(StandardResponse.success(insight));
    }


    public Map WeeklyInsight(LocalDate startDate, LocalDate endDate) {

        Map<String, Long> supportAnalytics = new HashMap<>();
        long totalTickets = 0;
        List<SupportType> supportTypes = supportTypeRepository.findAll();
        for (SupportType supportType : supportTypes) {
            totalTickets = supportTicketRepository.countBySupportTypeAndIntelRxIdAndCreatedAtBetween(
                    supportType, null, startDate.atStartOfDay(), endDate.atStartOfDay());

            supportAnalytics.put(supportType.getName(), totalTickets == 0 ? 0 : totalTickets);
        }

        return supportAnalytics;
    }

    public ResponseEntity<?> weeklyAnalytics(String start_date, String end_date) {
        LinkedHashMap<String, Object> insight = new LinkedHashMap<>();
        insight.put("WeeklyAnalytics", start_date + " - " + end_date);

        LocalDate startDate = utility.convertStringToLocalDate(start_date);
        LocalDate endDate = utility.convertStringToLocalDate(end_date);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");

        List<Map<String, Object>> dailyInsights = new ArrayList<>();

        LocalDate currentStartDate = startDate;
        while (currentStartDate.isBefore(endDate) || currentStartDate.isEqual(endDate)) {
            LocalDate currentEndDate = currentStartDate.plusDays(1).minusDays(1); // end of the day
            if (currentEndDate.isAfter(endDate)) {
                currentEndDate = endDate; // do not go past the end date
            }

            Map<String, Double> dailySales = WeeklyInsight(currentStartDate, currentEndDate);

            Map<String, Object> dailyInsight = new LinkedHashMap<>();
            dailyInsight.put("DateRange", currentStartDate.format(formatter));
            dailyInsight.putAll(dailySales);
            dailyInsights.add(dailyInsight);

            currentStartDate = currentStartDate.plusDays(1); // move to the next day
        }

        insight.put("WeeklyAnalytics", dailyInsights);

        return ResponseEntity.ok(StandardResponse.success(insight));
    }

    public ResponseEntity<?> topComplaint() {

        LinkedHashMap<String, Object> insight = new LinkedHashMap<>();
        //insight.put("WeeklyAnalytics", start_date + " - " + end_date);
        insight.put("Categories", "No of complaints");

        long totalTickets = 0;

        List<SupportType> supportTypes = supportTypeRepository.findAll();
        for (SupportType supportType : supportTypes) {
            totalTickets = supportTicketRepository.countBySupportTypeAndIntelRxIdLast30Days(
                    supportType, null);
            insight.put(supportType.getName(), totalTickets == 0 ? 0 : totalTickets);
        }

        return ResponseEntity.ok(StandardResponse.success(insight));

    }

}
