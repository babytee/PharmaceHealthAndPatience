package com.pharmacy.intelrx.pharmacy.services;

import com.pharmacy.intelrx.admin.models.Admin;
import com.pharmacy.intelrx.auxilliary.dto.RegisterRequest;
import com.pharmacy.intelrx.auxilliary.services.S3Service;
import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.PharmacyRequest;
import com.pharmacy.intelrx.pharmacy.dto.SupportTicketFilterRequest;
import com.pharmacy.intelrx.pharmacy.dto.SupportTicketRequest;
import com.pharmacy.intelrx.pharmacy.dto.SupportTicketResponse;
import com.pharmacy.intelrx.pharmacy.models.support.SupportTicket;
import com.pharmacy.intelrx.pharmacy.models.support.SupportType;
import com.pharmacy.intelrx.pharmacy.repositories.SupportTicketRepository;
import com.pharmacy.intelrx.pharmacy.repositories.SupportTypeRepository;
import com.pharmacy.intelrx.pharmacy.utility.PharmacyMapping;
import com.pharmacy.intelrx.utility.EmailService;
import com.pharmacy.intelrx.utility.UserDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service("PharmacySupportTicketService")
public class SupportTicketService {
    private final SupportTypeRepository supportTypeRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final Utility utility;
    private final EmailService emailService;
    private final UserDetailsService userDetailsService;
    private final S3Service s3Service;
    private final PharmacyMapping pharmacyMapping;

    public ResponseEntity<?> submitTicket(SupportTicketRequest request) throws MessagingException, IOException {
        var user = userDetailsService.getAuthenticatedUser();
        //var branch = userDetailsService.getBranch();
        var intelRxId = userDetailsService.getIntelRxId();

        if (utility.isNullOrEmpty(request.getEmailAddress())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("emailAddress is required"));
        } else if (utility.isNullOrEmpty(request.getSubject())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("subject is required"));
        } else if (utility.isNullOrEmpty(request.getDescription())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("description is required"));
        }
//        else if (utility.isNullOrEmpty(request.getAttachedFile())) {
//            return ResponseEntity.badRequest().body(StandardResponse.error("attachedFile is required"));
//        }
        else if (request.getSupportTypeId() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("supportTypeId is required"));
        }

        Optional<SupportType> optional = supportTypeRepository.findById(request.getSupportTypeId());
        if(!optional.isPresent()){
            return ResponseEntity.badRequest().body(StandardResponse.error("supportTypeId does not exist"));
        }

        SupportType supportType = optional.get();

        String getAttachedFile = null;
        if (!utility.isNullOrEmpty(request.getAttachedFile())) {
            getAttachedFile = s3Service.uploadFileDoc(request.getAttachedFile(), "pharmacy");
        } else {
            getAttachedFile = user.getProfilePic();
        }

        SupportTicket supportTicket = SupportTicket.builder()
                .intelRxId(intelRxId)
                .supportType(supportType)
                .subject(request.getSubject())
                .emailAddress(request.getEmailAddress())
                .description(request.getDescription())
                .ticketStatus("Pending")
                .attachedFile(getAttachedFile)
                .build();

        supportTicketRepository.save(supportTicket);

        emailService.supportTicketRequestEmail(user,supportTicket);

        return ResponseEntity.ok(StandardResponse.success("Ticket Submitted"));

    }

    public ResponseEntity<?> ticketsOverview() {
        var intelRxId = userDetailsService.getIntelRxId();
        Long totalTickets = supportTicketRepository.countTotalTickets(intelRxId);

        Long pendingTickets = supportTicketRepository.countPendingTickets(intelRxId);

        Long resolvedTickets = supportTicketRepository.countResolvedTickets(intelRxId);

        Map<String, Long> ticketsCount = new HashMap<>();
        ticketsCount.put("totalTickets", totalTickets == null ? 0 : totalTickets);
        ticketsCount.put("pendingTickets", pendingTickets == null ? 0 : pendingTickets);
        ticketsCount.put("resolvedTickets", resolvedTickets == null ? 0 : resolvedTickets);

        return ResponseEntity.ok(StandardResponse.success(ticketsCount));

    }

    public ResponseEntity<?> filtersSupportTicket(SupportTicketFilterRequest request, Pageable pageable) {

        var intelRxId = userDetailsService.getIntelRxId();

        SupportType supportType = null;
        if (request.getSupportTypeId() != null) {
            Optional<SupportType> optionalSupportType = supportTypeRepository.findById(request.getSupportTypeId());
            if (optionalSupportType.isPresent()) {
                supportType = optionalSupportType.get();
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        StandardResponse.error("Invalid Support Type ID")
                );
            }
        }

        // Check for null or empty Page object
        Page<SupportTicket> supportTickets = supportTicketRepository.findByFilters(
                supportType,
                intelRxId,
                request.getState(),  // Correct order
                request.getKeyword(), // Correct order
                pageable);

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

        var intelRxId = userDetailsService.getIntelRxId();
        Optional<SupportTicket> optional = supportTicketRepository.findByIdAndIntelRxId(
                request.getId(), intelRxId
        );

        if (!optional.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.success("Support Ticket Not Found"));
        }

        SupportTicket supportTicket = optional.get();
        SupportTicketResponse ticketResponse = mapToSupportTicketResponse(supportTicket);

        return ResponseEntity.ok(StandardResponse.success(ticketResponse));

    }

    private SupportTicketResponse mapToSupportTicketResponse(SupportTicket supportTicket) throws IOException {
        var pharmacy = userDetailsService.getPharmacyInfo();
        SupportType supportType = null;
        if (supportTicket.getSupportType() != null) {
            Optional<SupportType> optionalSupportType = supportTypeRepository.findById(supportTicket.getSupportType().getId());
            supportType = optionalSupportType.get();
        }

        PharmacyRequest pharmacyRequest = null;
        if (supportTicket.getIntelRxId() != null) {
            pharmacyRequest = pharmacyMapping.mapToPharmacy(pharmacy);
        }

        RegisterRequest registerRequest = new RegisterRequest();

        if (supportTicket.getResolvedBy() != null) {
            Admin admin  = supportTicket.getResolvedBy();
            registerRequest.setId(admin.getId());
            registerRequest.setFirstname(admin.getFirstname());
            registerRequest.setLastname(admin.getLastname());
            registerRequest.setEmail(admin.getEmail());
        }


        String attachedFile = null;
        if (supportTicket.getAttachedFile() != null) {
            S3Service.FetchedImage fetchedImage = s3Service.fetchImage(supportTicket.getAttachedFile()); // Replace "your_image_name.jpg" with the actual image name
            attachedFile = fetchedImage.getImageUrl();
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

    private Map yearlyInsight(int year, int month) {
        var intelRxId = userDetailsService.getIntelRxId();
        // Use the provided month if it is greater than 0, otherwise use the current month
        int targetMonth = (month > 0) ? month : YearMonth.now().getMonthValue();

        Map<String, Long> supportAnalytics = new HashMap<>();
        long totalTickets = 0;

        List<SupportType> supportTypes = supportTypeRepository.findAll();
        for (SupportType supportType : supportTypes) {
            totalTickets = supportTicketRepository.countBySupportTypeAndIntelRxIdAndCreatedAtYearAndCreatedAtMonth
                    (supportType, intelRxId, year, targetMonth);
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

        var intelRxId = userDetailsService.getIntelRxId();
        Map<String, Long> supportAnalytics = new HashMap<>();
        long totalTickets = 0;
        List<SupportType> supportTypes = supportTypeRepository.findAll();
        for (SupportType supportType : supportTypes) {
            totalTickets = supportTicketRepository.countBySupportTypeAndIntelRxIdAndCreatedAtBetween(
                    supportType, intelRxId, startDate.atStartOfDay(), endDate.atStartOfDay());

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
        var intelRxId = userDetailsService.getIntelRxId();
        LinkedHashMap<String, Object> insight = new LinkedHashMap<>();
        //insight.put("WeeklyAnalytics", start_date + " - " + end_date);
        insight.put("Categories", "No of complaints");

        long totalTickets = 0;

        List<SupportType> supportTypes = supportTypeRepository.findAll();
        for (SupportType supportType : supportTypes) {
            totalTickets = supportTicketRepository.countBySupportTypeAndIntelRxIdLast30Days(
                    supportType, intelRxId);
            insight.put(supportType.getName(), totalTickets == 0 ? 0 : totalTickets);
        }

        return ResponseEntity.ok(StandardResponse.success(insight));

    }

}
