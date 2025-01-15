package com.pharmacy.intelrx.pharmacy.services;

import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.auxilliary.models.UserType;
import com.pharmacy.intelrx.auxilliary.repositories.UserRepository;
import com.pharmacy.intelrx.auxilliary.services.S3Service;
import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.ExpenditureRequest;
import com.pharmacy.intelrx.pharmacy.dto.employee.EmployeeFilterRequest;
import com.pharmacy.intelrx.pharmacy.dto.employee.EmployeeResponse;
import com.pharmacy.intelrx.pharmacy.dto.employee.SalaryPaymentRequest;
import com.pharmacy.intelrx.pharmacy.models.Pharmacy;
import com.pharmacy.intelrx.pharmacy.models.SalaryPayment;
import com.pharmacy.intelrx.pharmacy.models.employee.CompensationDetail;
import com.pharmacy.intelrx.pharmacy.models.employee.Employee;
import com.pharmacy.intelrx.pharmacy.repositories.PharmacyRepository;
import com.pharmacy.intelrx.pharmacy.repositories.employee.CompensationDetailRepository;
import com.pharmacy.intelrx.pharmacy.repositories.employee.EmployeeRepository;
import com.pharmacy.intelrx.pharmacy.repositories.employee.SalaryPaymentRepository;
import com.pharmacy.intelrx.pharmacy.utility.FilterEmployee;
import com.pharmacy.intelrx.utility.UserDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RequiredArgsConstructor
@Service("PharmacySalaryPaymentService")
public class SalaryPaymentService {
    private final SalaryPaymentRepository salaryPaymentRepository;
    private final UserDetailsService userDetailsService;
    private final Utility utility;
    private final EmployeeRepository employeeRepository;
    private final S3Service s3Service;
    private final UserRepository userRepository;
    private final PharmacyRepository pharmacyRepository;
    private final FilterEmployee filterEmployee;
    private final CompensationDetailRepository compensationDetailRepository;
    private final ExpenditureService expenditureService;

    public ResponseEntity<?> paySalary(SalaryPaymentRequest request) throws IOException {
        var user = userDetailsService.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.internalServerError().body(StandardResponse.error("User is Un-Authenticated"));
        }
        var intelRxId = userDetailsService.getIntelRxId();
        var branch = userDetailsService.getBranch();
        if (utility.isNullOrEmpty(request.getPayPeriod())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("payPeriod is required"));
        } else if (request.getEmployeeId() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("employeeId is required"));
        }

        Optional<Employee> optionalEmployee = employeeRepository.findByIdAndEmployeeIntelRxId(
                request.getEmployeeId(), intelRxId
        );
        if (!optionalEmployee.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("employee not found"));
        }
        Employee employee = optionalEmployee.get();

        String paymentReceipt = "";
        if (!request.getPaymentReceipt().isEmpty()) {
            // Save the file to the upload directory
            paymentReceipt = s3Service.uploadFileDoc(request.getPaymentReceipt(), "pharmacy");
        }

        var checkIfAlreadyPaid = salaryPaymentRepository.findByPharmacyBranchAndIntelRxIdAndEmployee
                (branch, intelRxId, employee).orElse(null);

        if (checkIfAlreadyPaid != null) {
            // Define a formatter for the "MMMM yyyy" pattern (e.g., "April 2024")
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

            // Parse request.getPayPeriod() into a YearMonth object
            YearMonth requestPayPeriod = YearMonth.parse(request.getPayPeriod(), formatter);

            // Get the current YearMonth
            YearMonth currentYearMonth = YearMonth.now();

            if (checkIfAlreadyPaid.getPayPeriod().equals(request.getPayPeriod())
                    || checkIfAlreadyPaid.getPayPeriod() == request.getPayPeriod()) {
                return ResponseEntity.ok(StandardResponse.error("Payment Is Not Due For This Month"));
            } else if (requestPayPeriod.isAfter(currentYearMonth)) {
                // Compare currentYearMonth with requestPayPeriod
                return ResponseEntity.ok(StandardResponse.error("payPeriod Is Higher Than Current Month"));
            }
        }


        SalaryPayment salaryPayment = SalaryPayment.builder()
                .paymentReceipt(paymentReceipt)
                .amount(request.getAmount())
                .payPeriod(request.getPayPeriod())
                .pharmacyBranch(branch)
                .intelRxId(intelRxId)
                .employee(employee)
                .status("Paid")
                .createdAt(LocalDateTime.now())
                .build();

        salaryPaymentRepository.save(salaryPayment);

        Optional<CompensationDetail> optionalCompensationDetail = compensationDetailRepository.findByEmployeeId(employee.getId());

        optionalCompensationDetail.ifPresent(compensationDetail -> {
            compensationDetail.setSalaryStatus("Paid");
            compensationDetailRepository.save(compensationDetail);
        });


        //submit to expenditure model
        User userEmployee = employee.getUser();
        String fullName = userEmployee.getLastName() + " " + userEmployee.getFirstName();

        ExpenditureRequest expenditureRequest = new ExpenditureRequest();
        expenditureRequest.setExpenseName("Salary Payment for " + fullName);
        expenditureRequest.setExpDay(LocalDate.now().getDayOfMonth());
        expenditureRequest.setExpMonth(LocalDate.now().getMonthValue());
        expenditureRequest.setExpYear(LocalDate.now().getYear());
        expenditureRequest.setAmountSpent(request.getAmount());
        expenditureRequest.setExpenditureType("Salaries");

        expenditureService.addExpense(expenditureRequest);

        return ResponseEntity.ok(StandardResponse.success("You have successfully paid this employees salary."));
    }

    public ResponseEntity<?> paymentHistory(Long employeeId, Pageable pageable) {
        var user = userDetailsService.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.internalServerError().body(StandardResponse.error("User is Un-Authenticated"));
        }
        var intelRxId = userDetailsService.getIntelRxId();
        var branch = userDetailsService.getBranch();

        Optional<Employee> optionalEmployee = employeeRepository.findByIdAndEmployeeIntelRxId(employeeId, intelRxId);

        if (!optionalEmployee.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId not found"));
        }

        Employee employee = optionalEmployee.get();

        Page<SalaryPayment> salaryPayments = null;

        salaryPayments = salaryPaymentRepository.findAllByIntelRxIdAndPharmacyBranchAndEmployee(
                intelRxId, branch, employee, pageable
        );

        if (salaryPayments.isEmpty()) {
            return ResponseEntity.ok(StandardResponse.success(salaryPayments));
        }

        Page<SalaryPaymentRequest> salaryPaymentRequests = salaryPayments.map(salaryPayment ->
                mapToSalaryPayment(salaryPayment));

        return ResponseEntity.ok(StandardResponse.success(salaryPaymentRequests));
    }

    private SalaryPaymentRequest mapToSalaryPayment(SalaryPayment salaryPayment) {
        String paymentReceipt = null;
        if (!salaryPayment.getPaymentReceipt().isEmpty()) {
            S3Service.FetchedImage fetchedImage = s3Service.fetchImage(salaryPayment.getPaymentReceipt()); // Replace "your_image_name.jpg" with the actual image name
            paymentReceipt = fetchedImage.getImageUrl();
        }

        return SalaryPaymentRequest.builder()
                .id(salaryPayment.getId())
                .paymentReceipt(paymentReceipt)
                .amount(salaryPayment.getAmount())
                .payPeriod(salaryPayment.getPayPeriod())
                .build();
    }

    public ResponseEntity<?> paymentHistoryStat(Long employeeId) {
        var user = userDetailsService.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.internalServerError().body(StandardResponse.error("User is Un-Authenticated"));
        }
        var intelRxId = userDetailsService.getIntelRxId();
        var branch = userDetailsService.getBranch();

        Optional<Employee> optionalEmployee = employeeRepository.findByIdAndEmployeeIntelRxId(employeeId, intelRxId);

        if (!optionalEmployee.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId not found"));
        }

        Employee employee = optionalEmployee.get();

        List<SalaryPayment> salaryPayments = null;

        if (branch == null && user.getUserType() == UserType.OWNER) {
            salaryPayments = salaryPaymentRepository.findAllByIntelRxIdAndEmployee(
                    intelRxId, employee);
        }
        if (branch == null && user.getUserType() == UserType.EMPLOYEE) {
            salaryPayments = salaryPaymentRepository.findAllByIntelRxIdAndEmployee(
                    intelRxId, employee);
        }
        if (branch != null && user.getUserType() == UserType.EMPLOYEE) {
            salaryPayments = salaryPaymentRepository.findByIntelRxIdAndPharmacyBranchAndEmployee(
                    intelRxId, branch, employee);
        }

        Map<String, String> stringMap = new HashMap<>();
        double totSalaryAmount = 0.0;
        double salaryAmount = 0.0;

        String assignedBranch = branch == null ? "HeadQuarter" : branch.getName();
        String jobTitle = employee.getJobInformation().getJobTitle().getName();
        String salaryType = employee.getCompensationDetail().getSalaryType().getName();

        if (!salaryPayments.isEmpty()) {
            //total amount of salary paid up till date
            for (SalaryPayment salaryPayment : salaryPayments) {
                totSalaryAmount += salaryPayment.getAmount();
            }
            salaryAmount = Double.parseDouble(employee.getCompensationDetail().getSalary());

        }
        stringMap.put("AssignedBranch", assignedBranch);
        stringMap.put("JobTitle", jobTitle);
        stringMap.put("PaidTillDate", String.valueOf(totSalaryAmount));
        stringMap.put("SalaryAmount", String.valueOf(salaryAmount));
        stringMap.put("SalaryType", salaryType);

        return ResponseEntity.ok(StandardResponse.success(stringMap));
    }

    public ResponseEntity<?> salaryStat() {
        var user = userDetailsService.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.internalServerError().body(StandardResponse.error("User is Un-Authenticated"));
        }
        var intelRxId = userDetailsService.getIntelRxId();
        var branch = userDetailsService.getBranch();

        int salaryPaidCount = 0;
        int salaryDueCount = 0;

        double totalSalaryPaid = 0.0;
        double totalSalaryDue = 0.0;

        Map<String, String> stringMap = new HashMap<>();

        List<Employee> employeeList = employeeRepository.findAllByEmployeeIntelRxId(intelRxId);

        for (Employee employee : employeeList) {
            List<SalaryPayment> salaryPayments;
            List<CompensationDetail> compensationDetails;
            // Get the number of paid salaries
            if (branch == null && user.getUserType() == UserType.OWNER) {
                salaryPayments = salaryPaymentRepository.findAllByIntelRxIdAndEmployeeAndStatus(
                        intelRxId, employee, "Paid");
                compensationDetails = compensationDetailRepository.findAllByEmployee(employee);

            } else if (branch == null && user.getUserType() == UserType.EMPLOYEE) {
                salaryPayments = salaryPaymentRepository.findAllByIntelRxIdAndEmployeeAndStatus(
                        intelRxId, employee, "Paid");
                compensationDetails = compensationDetailRepository.findAllByEmployee(employee);

            } else if (branch != null && user.getUserType() == UserType.EMPLOYEE) {
                salaryPayments = salaryPaymentRepository.findByIntelRxIdAndPharmacyBranchAndEmployeeAndStatus(
                        intelRxId, branch, employee, "Paid");
                compensationDetails = compensationDetailRepository.findAllByEmployee(employee);

            } else {
                salaryPayments = new ArrayList<>();
                compensationDetails = new ArrayList<>();
            }
            salaryPaidCount += salaryPayments.size();

            for (SalaryPayment salaryPayment : salaryPayments) {
                totalSalaryPaid += salaryPayment.getAmount();
            }


            // Get the number of overdue salaries
            for (CompensationDetail compensationDetail : compensationDetails) {
                String salaryStatus = compensationDetail.getSalaryStatus();
                // Check if salaryStatus is not null and does not match any valid statuses
                if (salaryStatus != null &&
                        !salaryStatus.equals("Overdue By 0 Days") &&
                        !salaryStatus.equals("Paid") &&
                        !salaryStatus.equals("Not Overdue")) {
                    salaryDueCount++;
                    //totalSalaryDue
                    totalSalaryDue += Double.parseDouble(compensationDetail.getSalary());
                }
            }

        }

        stringMap.put("salariesPaidCount", String.valueOf(salaryPaidCount));
        stringMap.put("salariesDueCount", String.valueOf(salaryDueCount));
        stringMap.put("totalSalaryPaid", String.valueOf(totalSalaryPaid));
        stringMap.put("totalSalaryDue", String.valueOf(totalSalaryDue));


        return ResponseEntity.ok(StandardResponse.success(stringMap));
    }

    public ResponseEntity<?> filterEmployeeSalary(EmployeeFilterRequest request, Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("You are unauthorized"));
        }

        var userId = userRepository.findByEmail(email);
        //var intelRxId = userDetailsService.getIntelRxId();
        if (!userId.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Logged In USer Not Found"));
        }

        Optional<Pharmacy> optionalPharmacy = pharmacyRepository.findByIntelRxId(request.getIntelRxId());
        if (optionalPharmacy == null || optionalPharmacy.isEmpty()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("No Pharmacy with this intelRxId: " + request.getIntelRxId()));
        }

        Page<EmployeeResponse> employeeResponseList = filterEmployee.FilterEmployeeByShift(request, pageable);
        return ResponseEntity.ok(StandardResponse.success(employeeResponseList));
    }


    @Scheduled(cron = "0 0 0 ? * MON") // Runs every Monday at midnight
    public void manageSalariesForAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        String currentMonth = salaryPaymentRepository.getCurrentMonth();

        for (Employee employee : employees) {
            boolean isPaid = salaryPaymentRepository.isSalaryPaidForCurrentMonth(employee.getId(), currentMonth);
            int dueDays = salaryPaymentRepository.countOverduePayments(employee.getId(), currentMonth);
            Optional<CompensationDetail> optionalCompensationDetail = compensationDetailRepository.findByEmployeeId(employee.getId());

            optionalCompensationDetail.ifPresent(compensationDetail -> {
                if (isPaid) {
                    compensationDetail.setSalaryStatus("Paid");
                }
                if (dueDays < 1) {
                    compensationDetail.setSalaryStatus("Not Overdue");
                } else {
                    compensationDetail.setSalaryStatus("Overdue");
                    //compensationDetail.setSalaryStatus("Overdue In " + dueDays + " Days");
                }
                compensationDetailRepository.save(compensationDetail);
            });
        }
    }

}
