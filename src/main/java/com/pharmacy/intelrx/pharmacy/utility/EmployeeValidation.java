package com.pharmacy.intelrx.pharmacy.utility;

import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.ContactInfoReqRes;
import com.pharmacy.intelrx.pharmacy.dto.UserRequest;
import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.pharmacy.dto.employee.*;
import com.pharmacy.intelrx.pharmacy.models.Pharmacy;
import com.pharmacy.intelrx.pharmacy.models.auxilliary.*;
import com.pharmacy.intelrx.pharmacy.repositories.auxilliary.DocumentTypeRepository;
import com.pharmacy.intelrx.pharmacy.repositories.auxilliary.JobTitleRepository;
import com.pharmacy.intelrx.pharmacy.repositories.auxilliary.PaymentFrequencyRepository;
import com.pharmacy.intelrx.pharmacy.repositories.auxilliary.SalaryTypeRepository;
import com.pharmacy.intelrx.pharmacy.repositories.employee.*;
import com.pharmacy.intelrx.auxilliary.repositories.UserRepository;
import com.pharmacy.intelrx.pharmacy.repositories.PharmacyRepository;
import com.pharmacy.intelrx.utility.Auxiliary;
import com.pharmacy.intelrx.utility.UserDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class EmployeeValidation {
    private final UserRepository userRepository;
    private final Utility utility;
    private final Auxiliary auxiliary;

    private final JobTitleRepository jobTitleRepository;
    private final PaymentFrequencyRepository paymentFrequencyRepository;
    private final SalaryTypeRepository salaryTypeRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final EmployeeRepository employeeRepository;
    private final PharmacyRepository pharmacyRepository;
    private final UserDetailsService userDetailsService;

    public ResponseEntity<?> validateEmployeeRequest(EmployeeRequest request) {
        if (utility.isNullOrEmpty(request.getEmployeeType())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("employeeType is required"));
        } else {
            String intelRxId = userDetailsService.getIntelRxId();
            Optional<Pharmacy> optionalPharmacy = pharmacyRepository.findByIntelRxId(intelRxId);

            if (optionalPharmacy == null || optionalPharmacy.isEmpty()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("employeeIntelRxId is required or not found with this pharmacy"));
            }

            // User Request
            ResponseEntity<?> userRequestEntity = userRequest(request.getUserRequest());
            if (userRequestEntity.getStatusCode() != HttpStatus.OK) {
                return userRequestEntity;
            }

            // Contact Request
            ResponseEntity<?> contactInfoReqResResEntity = ContactInfoReqRes(request.getContactInfoReqRes());
            if (contactInfoReqResResEntity.getStatusCode() != HttpStatus.OK) {
                return contactInfoReqResResEntity;
            }

            // JobInformation Request
            ResponseEntity<?> jobInformationRequestEntity = JobInformationRequest(request.getJobInformationRequest());
            if (jobInformationRequestEntity.getStatusCode() != HttpStatus.OK) {
                return jobInformationRequestEntity;
            }

            // CompensationDetail Request
            ResponseEntity<?> compensationDetailRequestEntity = CompensationDetailRequest(request.getCompensationDetailRequest());
            if (compensationDetailRequestEntity.getStatusCode() != HttpStatus.OK) {
                return compensationDetailRequestEntity;
            }

            // Education Request
            if (request.getEducationRequest() != null) {
                ResponseEntity<?> educationRequestEntity = educationRequestValidation(request.getEducationRequest());
                if (educationRequestEntity.getStatusCode() != HttpStatus.OK) {
                    return educationRequestEntity;
                }
            }

            // Legal Request
            if (request.getLegalRequest() != null) {
                ResponseEntity<?> legalRequestEntity = legalRequestValidation(request.getLegalRequest());
                if (legalRequestEntity.getStatusCode() != HttpStatus.OK) {
                    return legalRequestEntity;
                }
            }

            // Benefit Request
            if (request.getBenefitRequest() != null) {
                ResponseEntity<?> benefitRequestEntity = benefitRequestValidation(request.getBenefitRequest());
                if (benefitRequestEntity.getStatusCode() != HttpStatus.OK) {
                    return benefitRequestEntity;
                }
            }

            // ExtraInformation Request
//            ResponseEntity<?> extraInformationRequestEntity = extraInformationRequestValidation(request.getExtraInformationRequest());
//            if (extraInformationRequestEntity.getStatusCode() != HttpStatus.OK) {
//                return extraInformationRequestEntity;
//            }

            // EmployeeDocument Request
//            List<EmployeeDocumentRequest> employeeDocumentRequestList = request.getEmployeeDocumentRequest();
//            if (employeeDocumentRequestList != null && !employeeDocumentRequestList.isEmpty()) {
//                for (EmployeeDocumentRequest documentRequest : employeeDocumentRequestList) {
//                    ResponseEntity<?> employeeDocumentRequestEntity = employeeDocumentRequestValidation(documentRequest);
//                    if (employeeDocumentRequestEntity.getStatusCode() != HttpStatus.OK) {
//                        return employeeDocumentRequestEntity;
//                    }
//                }
//            }

            return ResponseEntity.ok(StandardResponse.success("validated successfully"));
        }
    }

    public ResponseEntity<?> userRequest(UserRequest request) {
        Optional<User> optionalUserEmail = userRepository.findByEmail(request.getEmail());
        Optional<User> optionalUserPhoneNumber = userRepository.findByPhoneNumber(request.getPhoneNumber());

        if (optionalUserEmail.isPresent() && utility.isNullOrEmpty(request.getIntelRxId())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Pharmacy with this email account already exists"));
        } else if (optionalUserPhoneNumber.isPresent() && utility.isNullOrEmpty(request.getIntelRxId())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Pharmacy with this phone number account already exists"));
        } else if (utility.isNullOrEmpty(request.getFirstName())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("firstName is required"));
        } else if (utility.isNullOrEmpty(request.getLastName())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("lastName is required"));
        } else if (utility.isNullOrEmpty(request.getGender())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("gender is required"));
        } else if (request.getDayOfBirth() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("dayOfBirth is required"));
        } else if (request.getBirthMonth() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("birthMonth is required"));
        } else if (request.getYearOfBirth() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("yearOfBirth is required"));
        } else if (utility.isNullOrEmpty(request.getPhoneNumber())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("phoneNumber is required"));
        } else if (utility.isNullOrEmpty(request.getEmail())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("email is required"));
        } else {
            return ResponseEntity.ok().body(StandardResponse.success("User request is valid"));
        }
    }

    public ResponseEntity<?> ContactInfoReqRes(ContactInfoReqRes request) {
        if (utility.isNullOrEmpty(request.getCountry())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("country is required"));
        } else if (utility.isNullOrEmpty(request.getState())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("state is required"));
        } else if (utility.isNullOrEmpty(request.getCity())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("city is required"));
        }
//        else if (utility.isNullOrEmpty(request.getLga())) {
//            return ResponseEntity.badRequest().body(StandardResponse.error("lga is required"));
//        }
        else {
            return ResponseEntity.ok().body(StandardResponse.success("Contact Info request is valid"));
        }
    }

    public ResponseEntity<?> JobInformationRequest(JobInformationRequest request) {
        Optional<JobTitle> optionalJobTitle = jobTitleRepository.findById(request.getJobTitleId());
        JobTitle jobTitle = optionalJobTitle.orElse(null);

//        SeniorityLevel seniorityLevel = auxiliary.getSeniorityLevel(request.getSeniorityLevelId());
//        Department department = auxiliary.getDepartment(request.getDepartmentId());

        WorkSchedule workSchedule = auxiliary.getWorkSchedule(request.getWorkScheduleId());

        if (request.getJobTitleId() == null || jobTitle == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("jobTitleId is required or not found"));
        } else if (request.getWorkScheduleId() == null || workSchedule == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("workScheduleId is required or not found"));
        } else if (request.getStartDate() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("startDate is required"));
        }
//        else if (request.getEndDate() == null) {
//            return ResponseEntity.badRequest().body(StandardResponse.error("endDate is required"));
//        }
        else if (utility.isNullOrEmpty(request.getJobScope())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("jobScope is required"));
        } else {
            return ResponseEntity.ok().body(StandardResponse.success("Job Information request is valid"));
        }
    }

    public ResponseEntity<?> CompensationDetailRequest(CompensationDetailRequest request) {

        Optional<PaymentFrequency> optionalPaymentFrequency = paymentFrequencyRepository.findById(request.getPaymentFrequencyId());
        PaymentFrequency paymentFrequency = optionalPaymentFrequency.orElse(null);

        Optional<SalaryType> optionalSalaryType = salaryTypeRepository.findById(request.getSalaryTypeId());
        SalaryType salaryType = optionalSalaryType.orElse(null);

        if (request.getSalaryTypeId() == null || salaryType == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("salaryTypeId is required or not found"));
        } else if (request.getPaymentFrequencyId() == null || paymentFrequency == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("paymentFrequencyId is required or not found"));
        } else if (utility.isNullOrEmpty(request.getSalary())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("salary is required"));
        } else if (utility.isNullOrEmpty(request.getBankName())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("bankName is required"));
        } else if (utility.isNullOrEmpty(request.getAccountNumber())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("accountNumber is required"));
        } else if (utility.isNullOrEmpty(request.getAccountName())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("accountName is required"));
        } else {
            return ResponseEntity.ok().body(StandardResponse.success("Compensation Detail request is valid"));
        }
    }

    public ResponseEntity<?> educationRequestValidation(EducationRequest request) {
        if (request.getLicense() == null || request.getLicense().length == 0) {
            return ResponseEntity.badRequest().body(StandardResponse.error("At least one license is required"));
        } else {
            return ResponseEntity.ok().body(StandardResponse.success("Education Request is valid"));
        }
    }

    public ResponseEntity<?> legalRequestValidation(LegalRequest request) {
        if (utility.isNullOrEmpty(request.getNinSsn())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("ninSsn is required"));
        } else if (utility.isNullOrEmpty(request.getStatus())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("status is required"));
        } else if (utility.isNullOrEmpty(request.getWorkAuthorization())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("workAuthorization is required"));
        } else {
            return ResponseEntity.ok().body(StandardResponse.success("Legal Request is valid"));
        }
    }

    public ResponseEntity<?> benefitRequestValidation(BenefitRequest request) {
        if (request.getBenefits() == null || request.getBenefits().length == 0) {
            return ResponseEntity.badRequest().body(StandardResponse.error("At least one benefit is required"));
        } else if (utility.isNullOrEmpty(request.getBeneficiary())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("beneficiary is required"));
        } else {
            return ResponseEntity.ok().body(StandardResponse.success("Benefit Request is valid"));
        }
    }

    public ResponseEntity<?> extraInformationRequestValidation(ExtraInformationRequest request) {
        if (utility.isNullOrEmpty(request.getPreferredNickname())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("preferredNickname is required"));
        } else if (utility.isNullOrEmpty(request.getEmergencyContactName())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("emergencyContactName is required"));
        } else if (utility.isNullOrEmpty(request.getEmergencyContactNumber())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("emergencyContactNumber is required"));
        } else if (utility.isNullOrEmpty(request.getRelationshipWithEmergency())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("relationshipWithEmergency is required"));
        } else if (utility.isNullOrEmpty(request.getRefereeName())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("refereeName is required"));
        } else if (utility.isNullOrEmpty(request.getRefereeNumber())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("refereeNumber is required"));
        } else if (utility.isNullOrEmpty(request.getRelationshipWithReferee())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("relationshipWithReferee is required"));
        }
//        else if (utility.isNullOrEmpty(request.getDisabilityStatus())) {
//            return ResponseEntity.badRequest().body(StandardResponse.error("disabilityStatus is required"));
//        }
        else if (request.getLanguage() == null || request.getLanguage().length == 0) {
            return ResponseEntity.badRequest().body(StandardResponse.error("At least one language is required"));
        } else {
            return ResponseEntity.ok().body(StandardResponse.success("Extra Information Request is valid"));
        }
    }

    public ResponseEntity<?> employeeDocumentRequestValidation(EmployeeDocumentRequest request) {
        Optional<DocumentType> optionalDocumentType = documentTypeRepository.findById(request.getDocumentTypeId());
        DocumentType documentType = optionalDocumentType.orElse(null);

        if (request.getDocumentTypeId() == null || documentType == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("documentTypeId is required or not found"));
        } else if (request.getFileDoc() == null || request.getFileDoc().isEmpty()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("fileDoc is required"));
        } else {
            return ResponseEntity.ok().body(StandardResponse.success("Employee Document Request is valid"));
        }
    }


}
