package com.pharmacy.intelrx.pharmacy.services;

import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.auxilliary.models.UserType;
import com.pharmacy.intelrx.auxilliary.repositories.UserRepository;
import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.*;
import com.pharmacy.intelrx.pharmacy.dto.employee.*;
import com.pharmacy.intelrx.pharmacy.models.BranchEmployee;
import com.pharmacy.intelrx.pharmacy.models.ConcludeEmployee;
import com.pharmacy.intelrx.pharmacy.models.Pharmacy;
import com.pharmacy.intelrx.pharmacy.models.TransferEmployee;
import com.pharmacy.intelrx.pharmacy.models.auxilliary.JobTitle;
import com.pharmacy.intelrx.pharmacy.models.employee.Employee;
import com.pharmacy.intelrx.pharmacy.models.employee.JobInformation;
import com.pharmacy.intelrx.pharmacy.repositories.*;
import com.pharmacy.intelrx.pharmacy.repositories.auxilliary.JobTitleRepository;
import com.pharmacy.intelrx.pharmacy.repositories.employee.EmployeeRepository;
import com.pharmacy.intelrx.pharmacy.repositories.employee.JobInformationRepository;
import com.pharmacy.intelrx.pharmacy.utility.*;
import com.pharmacy.intelrx.utility.EmailService;
import com.pharmacy.intelrx.utility.UserDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service("PharmacyEmployeeService")
public class EmployeeService {
    private final Utility utility;
    private final UserRepository userRepository;
    private final EmployeeValidation employeeValidation;
    private final EmployeeRepository employeeRepository;
    private final FilterEmployee filterEmployee;
    private final JobInformationRepository jobInformationRepository;
    private final JobTitleRepository jobTitleRepository;
    private final AddEmployeeInformation addEmployeeInformation;
    private final UpdateEmployeeInformation updateEmployeeInformation;
    private final PharmacyRepository pharmacyRepository;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final TransferEmployeeValidation transferEmployeeValidation;
    private final ConcludeEmployeeValidation concludeEmployeeValidation;
    private final TransferEmployeeRepository transferEmployeeRepository;
    private final ConcludeEmployeeRepository concludeEmployeeRepository;
    private final PharmacyBranchService pharmacyBranchService;
    private final BranchEmployeeRepository branchEmployeeRepository;
    private final EmailService emailService;
    private final NotificationServices notificationServices;
    private final NotificationTypeRepository notificationTypeRepository;


    @Value("${spring.intelrx.url}")
    private String baseUrl;

    @Transactional
    public ResponseEntity<?> addEmployee(EmployeeRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("You are unauthorized"));
        }

        if (utility.isNullOrEmpty(request.getEmployeeType())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("employeeType is required"));
        } else {
            try {

                //check for validation
                ResponseEntity<?> employeeRequest = employeeValidation.validateEmployeeRequest(request);
                if (employeeRequest.getStatusCode() != HttpStatus.OK) {
                    return employeeRequest;
                }

                //add new employee
                var employee = addEmployeeInformation.AddEmployeeInfo(request);

                return ResponseEntity.ok(StandardResponse.success("Preview Record", employee));

            } catch (Exception e) {
                // Handle exceptions, log errors, or throw a custom exception
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(StandardResponse.error("Error occurred while saving data" + e.getMessage()));
            }
        }
    }

    public ResponseEntity<?> updateUser(UserRequest request, Long employeeId) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            //String email = authentication.getName();

            if (!authentication.isAuthenticated()) {
                return ResponseEntity.internalServerError().body(StandardResponse.error("You are unauthorized"));
            }

            if (employeeId == null) {
                return ResponseEntity.badRequest().body(StandardResponse.error("employeeId is required"));
            }

            //check for validation
            ResponseEntity<?> employeeRequest = employeeValidation.userRequest(request);
            if (employeeRequest.getStatusCode() != HttpStatus.OK) {
                return employeeRequest;
            }

            Optional<Employee> optionalEmployee = employeeRepository.findByIdAndEmployeeIntelRxId(employeeId, request.getIntelRxId());
            Employee employee = null;
            if (optionalEmployee.isPresent()) {
                employee = optionalEmployee.get();
            } else {
                return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId is not found"));
            }

            //update user
            var user = updateEmployeeInformation.updateToUserEntity(request, employee);

            return ResponseEntity.ok(StandardResponse.success("Record Updated", user));

        } catch (Exception e) {
            // Handle exceptions, log errors, or throw a custom exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(StandardResponse.error("Error occurred while saving data" + e.getMessage()));
        }

    }

    public ResponseEntity<?> updateContact(ContactInfoReqRes request, Long employeeId) {

        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (!authentication.isAuthenticated()) {
                return ResponseEntity.internalServerError().body(StandardResponse.error("You are unauthorized"));
            }


            //check for validation
            ResponseEntity<?> employeeRequest = employeeValidation.ContactInfoReqRes(request);
            if (employeeRequest.getStatusCode() != HttpStatus.OK) {
                return employeeRequest;
            }

            if (employeeId == null) {
                return ResponseEntity.badRequest().body(StandardResponse.error("employeeId is required"));
            }

            Optional<Employee> optionalEmployee = employeeRepository.findByIdAndEmployeeIntelRxId(employeeId, request.getIntelRxId());
            Employee employee = null;
            if (optionalEmployee.isPresent()) {
                employee = optionalEmployee.get();
            } else {
                return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId is not found"));
            }

            //update user
            var user = updateEmployeeInformation.updateToContactEntity(request, employee);

            return ResponseEntity.ok(StandardResponse.success("Record Updated", user));

        } catch (Exception e) {
            // Handle exceptions, log errors, or throw a custom exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(StandardResponse.error("Error occurred while saving data" + e.getMessage()));
        }

    }

    public ResponseEntity<?> updateJobInformation(JobInformationRequest request, Long employeeId) {

        try {
            var user = userDetailsService.getAuthenticatedUser();

            if (user == null) {
                return ResponseEntity.badRequest().body(StandardResponse.error("You are unauthorized"));
            }

            //check for validation
            ResponseEntity<?> employeeRequest = employeeValidation.JobInformationRequest(request);
            if (employeeRequest.getStatusCode() != HttpStatus.OK) {
                return employeeRequest;
            }

            if (employeeId == null) {
                return ResponseEntity.badRequest().body(StandardResponse.error("employeeId is required"));
            }

            Optional<Employee> optionalEmployee = employeeRepository.findByIdAndEmployeeIntelRxId(employeeId, request.getIntelRxId());
            Employee employee = null;
            if (optionalEmployee.isPresent()) {
                employee = optionalEmployee.get();
            } else {
                return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId is not found"));
            }

            //update user
            var viewUser = updateEmployeeInformation.updateToJobInformationEntity(request, employee);

            return ResponseEntity.ok(StandardResponse.success("Record Updated"));

        } catch (Exception e) {
            // Handle exceptions, log errors, or throw a custom exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(StandardResponse.error("Error occurred while saving data" + e.getMessage()));
        }

    }

    public ResponseEntity<?> updateCompensationDetail(CompensationDetailRequest request, Long employeeId) {

        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            //String email = authentication.getName();

            if (!authentication.isAuthenticated()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("You are unauthorized"));
            }


            //check for validation
            ResponseEntity<?> employeeRequest = employeeValidation.CompensationDetailRequest(request);
            if (employeeRequest.getStatusCode() != HttpStatus.OK) {
                return employeeRequest;
            }

            if (employeeId == null) {
                return ResponseEntity.badRequest().body(StandardResponse.error("employeeId is required"));
            }

            Optional<Employee> optionalEmployee = employeeRepository.findByIdAndEmployeeIntelRxId(employeeId, request.getIntelRxId());
            Employee employee = null;
            if (optionalEmployee.isPresent()) {
                employee = optionalEmployee.get();
            } else {
                return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId is not found"));
            }

            //update user
            var user = updateEmployeeInformation.updateToCompensationDetailEntity(request, employee);

            return ResponseEntity.ok(StandardResponse.success("Record Updated", user));

        } catch (Exception e) {
            // Handle exceptions, log errors, or throw a custom exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(StandardResponse.error("Error occurred while saving data" + e.getMessage()));
        }

    }

    public ResponseEntity<?> updateEducation(EducationRequest request, Long employeeId) {

        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            //String email = authentication.getName();

            if (!authentication.isAuthenticated()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("You are unauthorized"));
            }


            //check for validation
            ResponseEntity<?> employeeRequest = employeeValidation.educationRequestValidation(request);
            if (employeeRequest.getStatusCode() != HttpStatus.OK) {
                return employeeRequest;
            }

            if (employeeId == null) {
                return ResponseEntity.badRequest().body(StandardResponse.error("employeeId is required"));
            }

            Optional<Employee> optionalEmployee = employeeRepository.findByIdAndEmployeeIntelRxId(employeeId, request.getIntelRxId());
            Employee employee = null;
            if (optionalEmployee.isPresent()) {
                employee = optionalEmployee.get();
            } else {
                return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId is not found"));
            }

            //update user
            var user = updateEmployeeInformation.updateToEducationEntity(request, employee);

            return ResponseEntity.ok(StandardResponse.success("Record Updated", user));

        } catch (Exception e) {
            // Handle exceptions, log errors, or throw a custom exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(StandardResponse.error("Error occurred while saving data" + e.getMessage()));
        }

    }

    public ResponseEntity<?> updateLegal(LegalRequest request, Long employeeId) {

        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            //String email = authentication.getName();

            if (!authentication.isAuthenticated()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("You are unauthorized"));
            }


            //check for validation
            ResponseEntity<?> employeeRequest = employeeValidation.legalRequestValidation(request);
            if (employeeRequest.getStatusCode() != HttpStatus.OK) {
                return employeeRequest;
            }

            if (employeeId == null) {
                return ResponseEntity.badRequest().body(StandardResponse.error("employeeId is required"));
            }

            Optional<Employee> optionalEmployee = employeeRepository.findByIdAndEmployeeIntelRxId(employeeId, request.getIntelRxId());
            Employee employee = null;
            if (optionalEmployee.isPresent()) {
                employee = optionalEmployee.get();
            } else {
                return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId is not found"));
            }

            //update user
            var user = updateEmployeeInformation.updateLegalEntity(request, employee);

            return ResponseEntity.ok(StandardResponse.success("Record Updated", user));

        } catch (Exception e) {
            // Handle exceptions, log errors, or throw a custom exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(StandardResponse.error("Error occurred while saving data" + e.getMessage()));
        }

    }

    public ResponseEntity<?> updateBenefit(BenefitRequest request, Long employeeId) {

        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            //String email = authentication.getName();

            if (!authentication.isAuthenticated()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("You are unauthorized"));
            }


            //check for validation
            ResponseEntity<?> employeeRequest = employeeValidation.benefitRequestValidation(request);
            if (employeeRequest.getStatusCode() != HttpStatus.OK) {
                return employeeRequest;
            }

            if (employeeId == null) {
                return ResponseEntity.badRequest().body(StandardResponse.error("employeeId is required"));
            }

            Optional<Employee> optionalEmployee = employeeRepository.findByIdAndEmployeeIntelRxId(employeeId, request.getIntelRxId());
            Employee employee = null;
            if (optionalEmployee.isPresent()) {
                employee = optionalEmployee.get();
            } else {
                return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId is not found"));
            }

            //update user
            var user = updateEmployeeInformation.updateBenefitEntity(request, employee);

            return ResponseEntity.ok(StandardResponse.success("Record Updated", user));

        } catch (Exception e) {
            // Handle exceptions, log errors, or throw a custom exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(StandardResponse.error("Error occurred while saving data" + e.getMessage()));
        }

    }

    public ResponseEntity<?> updateExtraInformation(ExtraInformationRequest request, Long employeeId) {

        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            //String email = authentication.getName();

            if (!authentication.isAuthenticated()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("You are unauthorized"));
            }


            //check for validation
//            ResponseEntity<?> employeeRequest = employeeValidation.extraInformationRequestValidation(request);
//            if (employeeRequest.getStatusCode() != HttpStatus.OK) {
//                return employeeRequest;
//            }

            if (employeeId == null) {
                return ResponseEntity.badRequest().body(StandardResponse.error("employeeId is required"));
            }

            Optional<Employee> optionalEmployee = employeeRepository.findByIdAndEmployeeIntelRxId(employeeId, request.getIntelRxId());
            Employee employee = null;
            if (optionalEmployee.isPresent()) {
                employee = optionalEmployee.get();
            } else {
                return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId is not found"));
            }

            //update user
            var user = updateEmployeeInformation.updateExtraInformationEntity(request, employee);

            return ResponseEntity.ok(StandardResponse.success("Record Updated", user));

        } catch (Exception e) {
            // Handle exceptions, log errors, or throw a custom exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(StandardResponse.error("Error occurred while saving data" + e.getMessage()));
        }

    }

    public ResponseEntity<?> updateEmployeeDocument(List<EmployeeDocumentRequest> requests, Long employeeId) {

        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            //String email = authentication.getName();

            if (!authentication.isAuthenticated()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("You are unauthorized"));
            }

            if (employeeId == null) {
                return ResponseEntity.badRequest().body(StandardResponse.error("employeeId is required"));
            }

            Employee employee = null;
            for (EmployeeDocumentRequest request : requests) {
                //check for validation
                ResponseEntity<?> employeeRequest = employeeValidation.employeeDocumentRequestValidation(request);
                if (employeeRequest.getStatusCode() != HttpStatus.OK) {
                    return employeeRequest;
                }

                Optional<Employee> optionalEmployee = employeeRepository.findByIdAndEmployeeIntelRxId(employeeId, request.getIntelRxId());

                if (optionalEmployee.isPresent()) {
                    employee = optionalEmployee.get();
                } else {
                    return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId is not found"));
                }
            }

            //update user
            var user = updateEmployeeInformation.updateEmployeeDocumentEntity(requests, employee);

            return ResponseEntity.ok(StandardResponse.success("Record Updated", user));

        } catch (Exception e) {
            // Handle exceptions, log errors, or throw a custom exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(StandardResponse.error("Error occurred while saving data" + e.getMessage()));
        }

    }

    @Transactional
    public ResponseEntity<?> appAccess(Long employeeId, Long jobTitleId) {
        Logger logger = LoggerFactory.getLogger(EmployeeService.class);
        try {
            var user = userDetailsService.getAuthenticatedUser();
            var intelRxId = userDetailsService.getIntelRxId();
            if (user == null) {
                return ResponseEntity.badRequest().body(StandardResponse.error("You are unauthorized"));
            }
            if (employeeId == null) {
                return ResponseEntity.badRequest().body(StandardResponse.error("employeeId is required"));
            }
            if (jobTitleId == null) {
                return ResponseEntity.badRequest().body(StandardResponse.error("jobTitleId is required"));
            }
            logger.info("Processing app access for employeeId: {}, jobTitleId: {}", employeeId, jobTitleId);

            Optional<Employee> optionalEmployee = employeeRepository.findByIdAndEmployeeIntelRxId(employeeId, intelRxId);
            if (!optionalEmployee.isPresent()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("employeeId not found"));
            }
            Employee employee = optionalEmployee.get();

            Optional<JobTitle> jobTitleOptional = jobTitleRepository.findById(jobTitleId);
            if (!jobTitleOptional.isPresent()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("jobTitleId not found"));
            }

            JobTitle jobTitle = jobTitleOptional.get();
            JobTitle employeejobTitle = employee.getJobInformation().getJobTitle();

            Optional<JobInformation> optional = jobInformationRepository.findByJobTitleAndEmployee(employeejobTitle, employee);
            if (!optional.isPresent()) {
                return ResponseEntity.badRequest().body(
                        StandardResponse.error("employee with this job information not found not found"));
            }

            JobInformation jobInformation = optional.get();
            jobInformation.setJobTitle(jobTitle);
            jobInformationRepository.save(jobInformation);

            String email = employee.getUser().getEmail();
            User empUser = employee.getUser();
            String fullName = empUser.getLastName() + " " + empUser.getFirstName();

            var pharmacy = pharmacyRepository.findByIntelRxId(intelRxId);
            if (!pharmacy.isPresent()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("Pharmacy not found"));
            }

            String subject = pharmacy.get().getPharmacyName() + " App Access Modified";
            String body = "Congratulations, your app access permission has been changed to " + jobTitle.getName();
            emailService.appAccessEmail(email, subject, body,fullName);

            String message = "App Access was changed for " + fullName + " to " + jobTitle.getName();
            NotificationEntityRequest currentBranchEntityRequest = new NotificationEntityRequest();
            currentBranchEntityRequest.setBranchId(null);
            currentBranchEntityRequest.setIntelRxId(intelRxId);
            currentBranchEntityRequest.setNotificationTypeId(1L);
            currentBranchEntityRequest.setEmployeeId(employeeId);
            currentBranchEntityRequest.setNotificationTitle(subject);
            currentBranchEntityRequest.setNotificationMsg(message);
            currentBranchEntityRequest.setNotificationStatus(false);

            notificationServices.submitNotification(currentBranchEntityRequest);

            return ResponseEntity.ok().body(StandardResponse.success("App access granted. Employee will receive notification via email"));
        } catch (Exception e) {
            logger.error("Error processing app access: ", e);
            return ResponseEntity.status(500).body(StandardResponse.error("Internal Server Error " + e.getMessage()));
        }
    }


    @Transactional
    public ResponseEntity<?> transferEmployee(TransferEmployeeRequest request) {
        var user = userDetailsService.getAuthenticatedUser();
        var intelRxId = userDetailsService.getIntelRxId();
        if (user == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("You are unauthorized"));
        }

        ResponseEntity<?> responseEntity = transferEmployeeValidation.validate(request);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return responseEntity;
        }

        var employee = transferEmployeeValidation.checkEmployee(request.getEmployeeId());
        var currentBranch = transferEmployeeValidation.checkBranch(request.getCurrentBranchId());
        var newBranch = transferEmployeeValidation.checkBranch(request.getNewBranchId());

        TransferEmployee transferEmployee = TransferEmployee.builder()
                .employeeIntelRxId(intelRxId)
                .employee(employee)
                .user(user)
                .currentBranch(currentBranch)
                .newBranch(newBranch)
                .dateTransferred(LocalDateTime.now())
                .build();

        transferEmployeeRepository.save(transferEmployee);

        // Construct message based on whether currentBranch is null
        String message;
        if (currentBranch == null) {
            String name = userDetailsService.getPharmacyInfo().getPharmacyName();
            String fullName = user.getLastName() + " " + user.getFirstName();
            message = name + " just transferred an employee by name " + fullName + " to " + newBranch.getName();
        } else {
            Employee CBM = currentBranch.getEmployee(); // Ensure CBM is not null
            String fullName = CBM != null ? CBM.getUser().getLastName() + " " + CBM.getUser().getFirstName() : "unknown employee";
            message = currentBranch.getName() + " just transferred an employee by name " + fullName + " to " + newBranch.getName();
        }

        // Adding member to the new branch
        BranchEmployee newBranchEmployee = pharmacyBranchService.AddMemberToBranch(user, newBranch, employee);

        // Ensure currentBranch and its employee are not null before proceeding with notifications
        if (currentBranch != null && currentBranch.getEmployee() != null) {
            NotificationEntityRequest currentBranchEntityRequest = new NotificationEntityRequest();
            currentBranchEntityRequest.setBranchId(currentBranch.getId());
            currentBranchEntityRequest.setIntelRxId(intelRxId);
            currentBranchEntityRequest.setNotificationTypeId(2L);
            currentBranchEntityRequest.setEmployeeId(currentBranch.getEmployee().getId());
            currentBranchEntityRequest.setNotificationTitle("New Request Transfer");
            currentBranchEntityRequest.setNotificationMsg(message);
            currentBranchEntityRequest.setNotificationStatus(false);
            notificationServices.submitNotification(currentBranchEntityRequest);
        }

        // Process new branch notifications only if newBranch and newBranchEmployee are valid
        if (newBranch != null && newBranchEmployee != null && newBranchEmployee.getEmployee() != null) {
            Employee NBM = newBranchEmployee.getEmployee();
            String newFullName = NBM.getUser().getLastName() + " " + NBM.getUser().getFirstName();
            String newMessage = newBranch.getName() + " just transferred an employee by name " + newFullName + " to " + newBranch.getName();

            NotificationEntityRequest newBranchEntityRequest = new NotificationEntityRequest();
            newBranchEntityRequest.setBranchId(newBranch.getId());
            newBranchEntityRequest.setIntelRxId(intelRxId);
            newBranchEntityRequest.setNotificationTypeId(2L);
            newBranchEntityRequest.setEmployeeId(newBranchEmployee.getEmployee().getId());
            newBranchEntityRequest.setNotificationTitle("New Request Transfer");
            newBranchEntityRequest.setNotificationMsg(newMessage);
            newBranchEntityRequest.setNotificationStatus(false);
            notificationServices.submitNotification(newBranchEntityRequest);
        }

        return ResponseEntity.ok(StandardResponse.success("Employee Transfer Successfully"));
    }

    @Transactional
    public ResponseEntity<?> concludeEmployee(ConcludeEmployeeRequest request) {
        var user = userDetailsService.getAuthenticatedUser();
        var intelRxId = userDetailsService.getIntelRxId();
        if (user == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("You are unauthorized"));
        }

        ResponseEntity responseEntity = concludeEmployeeValidation.validate(request);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return responseEntity;
        }

        var employee = concludeEmployeeValidation.checkEmployee(request.getEmployeeId());

        var optional = concludeEmployeeRepository.findByEmployeeIdAndEmployeeIntelRxId(request.getEmployeeId(), intelRxId);
        if (optional.isPresent()) {
            ConcludeEmployee concludeEmployee = ConcludeEmployee.builder()
                    .employee(employee)
                    .user(user)
                    .employeeIntelRxId(intelRxId)
                    .concludeType(request.getConcludeType())
                    .reasons(request.getReasons())
                    .dateTerminated(LocalDateTime.now())
                    .build();
            concludeEmployeeRepository.save(concludeEmployee);
        } else {
            ConcludeEmployee concludeEmployee = optional.get();
            concludeEmployee.setConcludeType(request.getConcludeType());
            concludeEmployee.setReasons(request.getReasons());
            concludeEmployeeRepository.save(concludeEmployee);
        }


//
        userDetailsService.updateUserStatus(employee.getUser(), request.getConcludeType());

        return ResponseEntity.ok(StandardResponse.success("Employee " + request.getConcludeType() + " Successfully"));
    }

    public ResponseEntity<?> getConcludeEmployee(Long employeeId) {

        var user = userDetailsService.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.ok().body(StandardResponse.error("You are unauthorized"));
        }

        var employee = concludeEmployeeValidation.checkEmployee(employeeId);
        if (employee == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("No employee record found"));
        }
        ConcludeEmployeeRequest concludeEmployeeRequest = filterEmployee.mapToConcludeEmployee(employee);

        return ResponseEntity.ok(StandardResponse.success(concludeEmployeeRequest));
    }

    @Transactional
    public ResponseEntity<?> deleteEmployee(Long employeeId, String password) {
        var user = userDetailsService.getAuthenticatedUser();
        var intelRxId = userDetailsService.getIntelRxId();
        if (user == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("You are unauthorized"));
        }
        if (employeeId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("employeeId is required"));
        }
        boolean check_password = this.passwordEncoder.matches(password, user.getPassword());

        if (!check_password) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Password not match"));
        }
        try {
            Optional<Employee> optional = employeeRepository.findById(employeeId);
            if (!optional.isPresent()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("employeeId not found"));
            }

            Employee employee = optional.get();

            userDetailsService.updateUserStatus(employee.getUser(), "DELETED");

            var email = employee.getUser().getEmail();
            //an email method that will send email verification message to the pharmacist

            var pharmacy = pharmacyRepository.findByIntelRxId(intelRxId);
            String subject = pharmacy.get().getPharmacyName();
            String body = "We are sorry to inform you that your profile information/account has been deleted";

            //this.utility.sendEmail(email, subject, body);
            emailService.accountDeletionEmail(email, subject, body);

            return ResponseEntity.ok().body(StandardResponse.success("Profile information deleted"));
        } catch (Exception e) {
            // Handle exceptions, log errors, or throw a custom exception
            return ResponseEntity.status(500).body(StandardResponse.error("Internal Server Error " + e.getMessage()));
        }

    }

    public ResponseEntity<?> approvedEmployee(Long employeeId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //String email = authentication.getName();

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("You are unauthorized"));
        }
        if (employeeId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("employeeId is required"));
        }
        try {
            Optional<Employee> optional = employeeRepository.findById(employeeId);
            if (!optional.isPresent()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("employeeId not found"));
            }
            Employee employee = optional.get();
            employee.setStatus(true);
            employeeRepository.save(employee);

            User user = employee.getUser();
            user.setUserStatus("Offline");
            userRepository.save(user);


            //an email method that will send email verification message to the pharmacist

            Optional<Pharmacy> optionalPharmacy = pharmacyRepository.findByIntelRxId(employee.getEmployeeIntelRxId());
            Pharmacy pharmacy = optionalPharmacy.get();

            String subject = pharmacy.getPharmacyName() + " Invite As An Employee";
            String body = "This is link to accept, confirm your email and set your password in case the button is not working" +
                    ": " + baseUrl + "accept-invite/" + employee.getEmployeeIntelRxId();
            String link = baseUrl + "accept-invite/" + employee.getEmployeeIntelRxId();
            //this.utility.sendEmail(employee.getUser().getEmail(), subject, body);
            emailService.employeeInviteEmail(employee.getUser(), link, subject, body);

            return ResponseEntity.ok().body(StandardResponse.success("Member Added Successfully"));
        } catch (Exception e) {
            // Handle exceptions, log errors, or throw a custom exception
            return ResponseEntity.status(500).body(StandardResponse.error("Internal Server Error " + e.getMessage()));
        }

    }

    public ResponseEntity<?> reviewEmployeeDetails(Long employeeId) {
        try {
            var user = userDetailsService.getAuthenticatedUser();
            if (user == null) {
                return ResponseEntity.ok().body(StandardResponse.error("You are unauthorized"));
            }
            if (employeeId == null) {
                return ResponseEntity.badRequest().body(StandardResponse.error("employeeId is required"));
            }
            var intelRxId = userDetailsService.getIntelRxId();

            Optional<Employee> optionalEmployee = employeeRepository.findByIdAndEmployeeIntelRxId(employeeId, intelRxId);
            Employee employee = null;
            if (!optionalEmployee.isPresent()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("employee not found or information not available"));
            }

            employee = optionalEmployee.get();

            if (!employee.getEmployeeIntelRxId().equals(intelRxId)) {
                return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId not found"));
            }

            EmployeeResponse employeeResponse = filterEmployee.mapToEmployeeResponse(employee);

            return ResponseEntity.ok(StandardResponse.success(employeeResponse));
        } catch (Exception e) {
            // Handle the exception according to your requirements
            e.printStackTrace(); // Log the exception or handle it in a more appropriate way
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(StandardResponse.error("An error occurred: " + e.getMessage()));
        }
    }

    public ResponseEntity<?> employeeDetails() {
        try {
            var user = userDetailsService.getAuthenticatedUser();
            if (user == null) {
                return ResponseEntity.ok().body(StandardResponse.error("You are unauthorized"));
            }
            var intelRxId = userDetailsService.getIntelRxId();

            Optional<Employee> optionalEmployee = employeeRepository.findByUserId(user.getId());
            Employee employee = null;
            if (!optionalEmployee.isPresent()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("employee not found or information not available"));
            }

            employee = optionalEmployee.get();

            if (!employee.getEmployeeIntelRxId().equals(intelRxId)) {
                return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId not found"));
            }

            EmployeeResponse employeeResponse = filterEmployee.mapToEmployeeResponse(employee);

            return ResponseEntity.ok(StandardResponse.success(employeeResponse));
        } catch (Exception e) {
            // Handle the exception according to your requirements
            e.printStackTrace(); // Log the exception or handle it in a more appropriate way
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(StandardResponse.error("An error occurred: " + e.getMessage()));
        }
    }

    public ResponseEntity<?> employeeOverview() {
        var user = userDetailsService.getAuthenticatedUser();
        String employeeIntelRxId = userDetailsService.getIntelRxId();
        var branch = userDetailsService.getBranch();
        //String email = authentication.getName();

        if (user == null) {
            return ResponseEntity.ok().body(StandardResponse.error("You are unauthorized"));
        }

        if (utility.isNullOrEmpty(employeeIntelRxId)) {
            return ResponseEntity.badRequest().body(StandardResponse.error("employeeIntelRxId is required"));
        }

        List<BranchEmployee> branchEmployeeList = new ArrayList<>();
        List<Employee> employeeList = new ArrayList<>();
        if (branch != null && user.getUserType() == UserType.EMPLOYEE) {
            branchEmployeeList = branchEmployeeRepository.findAllByPharmacyBranchId(branch.getId());
            employeeList = branchEmployeeList.stream()
                    .filter(branchEmployee -> branchEmployee.getEmployee().isStatus())
                    .map(BranchEmployee::getEmployee)
                    .collect(Collectors.toList());
        }

        if (branch == null && user.getUserType() == UserType.EMPLOYEE) {
            employeeList = employeeRepository.findAllByEmployeeIntelRxIdAndStatus(employeeIntelRxId, true);
        }
        if (branch == null && user.getUserType() == UserType.OWNER) {
            employeeList = employeeRepository.findAllByEmployeeIntelRxIdAndStatus(employeeIntelRxId, true);
        }

        /// Initialize count variables outside the loop
        int cleaner = 0;
        int pharmacists = 0;
        int cashiers = 0;
        int salesPersons = 0;
        int managers = 0;
        int admins = 0;
        int hrs = 0;

// Loop through each employee to calculate job title counts
        for (Employee employee : employeeList) {
            // Get job information for the current employee
            List<JobInformation> jobInformationList = jobInformationRepository.findAllByEmployeeId(employee.getId());

            for (JobInformation jobInformation : jobInformationList) {
                // Increment the count for each job title based on its ID
                // Handle any other job titles if needed
                if (jobInformation.getJobTitle().getId() == 1) {
                    pharmacists++;
                } else if (jobInformation.getJobTitle().getId() == 2) {
                    cashiers++;
                } else if (jobInformation.getJobTitle().getId() == 3) {
                    salesPersons++;
                } else if (jobInformation.getJobTitle().getId() == 4) {
                    cleaner++;
                } else if (jobInformation.getJobTitle().getId() == 5) {
                    managers++;
                } else if (jobInformation.getJobTitle().getId() == 6) {
                    hrs++;
                } else if (jobInformation.getJobTitle().getId() == 7) {
                    admins++;
                }
            }
        }

        OverviewResponse overviewResponse = new OverviewResponse();

        overviewResponse.setTotalEmployees(employeeList.size());
        overviewResponse.setPharmacists(pharmacists);
        overviewResponse.setCashiers(cashiers);
        overviewResponse.setSalesPersons(salesPersons);
        overviewResponse.setManagers(managers);
        overviewResponse.setCleaner(cleaner);
        overviewResponse.setAdministrator(admins);
        overviewResponse.setHr(hrs);

        return ResponseEntity.ok(StandardResponse.success(overviewResponse));

    }

    public ResponseEntity<?> getManagers() {
        var user = userDetailsService.getAuthenticatedUser();
        String employeeIntelRxId = userDetailsService.getIntelRxId();

        if (user == null) {
            return ResponseEntity.ok().body(StandardResponse.error("You are unauthorized"));
        }

        if (utility.isNullOrEmpty(employeeIntelRxId)) {
            return ResponseEntity.badRequest().body(StandardResponse.error("employeeIntelRxId is required"));
        }

        List<Employee> employeeList = employeeRepository.findByIntelRxIdAndJobTitleId(employeeIntelRxId, 5L);

        if (employeeList.isEmpty()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("No employee record found"));
        }
        List<EmployeeResponse> employeeResponseList = employeeList.stream().map((address) -> filterEmployee.mapToEmployeeResponse(address)).collect(Collectors.toList());

        return ResponseEntity.ok(StandardResponse.success(employeeResponseList));

    }

    public ResponseEntity<?> allEmployee() {

        var user = userDetailsService.getAuthenticatedUser();
        String employeeIntelRxId = userDetailsService.getIntelRxId();
        if (user == null) {
            return ResponseEntity.ok().body(StandardResponse.error("You are unauthorized"));
        }

        List<Employee> employeeList = employeeRepository.findAllByEmployeeIntelRxId(employeeIntelRxId);

        if (employeeList.isEmpty()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("No employee record found"));
        }
        List<EmployeeResponse> employeeResponseList = employeeList.stream().map((address) -> filterEmployee.mapToEmployeeResponse(address)).collect(Collectors.toList());

        return ResponseEntity.ok(StandardResponse.success(employeeResponseList));
    }

    public ResponseEntity<?> filterEmployee(EmployeeFilterRequest request, Pageable pageable) {
        var user = userDetailsService.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("You are unauthorized"));
        }

//        String email = user.getEmail();

//        var userId = userRepository.findByEmail(email);
//        var intelRxId = userDetailsService.getIntelRxId();

        Optional<Pharmacy> optionalPharmacy = pharmacyRepository.findByIntelRxId(request.getIntelRxId());
        if (optionalPharmacy == null || optionalPharmacy.isEmpty()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("No Pharmacy with this intelRxId: " + request.getIntelRxId()));
        }
        Page<EmployeeResponse> employeeResponseList = null;

        employeeResponseList = filterEmployee.FilterEmployeeInfo(request, pageable);

        if (employeeResponseList == null) {
            return ResponseEntity.ok(StandardResponse.success("No record found"));
        } else {
            return ResponseEntity.ok(StandardResponse.success(employeeResponseList));
        }
    }

}

