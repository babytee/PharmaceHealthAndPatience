package com.pharmacy.intelrx.pharmacy.utility;

import com.pharmacy.intelrx.pharmacy.dto.ContactInfoReqRes;
import com.pharmacy.intelrx.pharmacy.dto.UserRequest;
import com.pharmacy.intelrx.auxilliary.models.Role;
import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.auxilliary.models.UserType;
import com.pharmacy.intelrx.pharmacy.dto.employee.*;
import com.pharmacy.intelrx.pharmacy.models.ContactInfo;
import com.pharmacy.intelrx.pharmacy.models.auxilliary.*;
import com.pharmacy.intelrx.pharmacy.models.employee.*;
import com.pharmacy.intelrx.pharmacy.repositories.employee.*;
import com.pharmacy.intelrx.auxilliary.repositories.UserRepository;
import com.pharmacy.intelrx.pharmacy.repositories.ContactInfoRepository;
import com.pharmacy.intelrx.auxilliary.services.S3Service;
import com.pharmacy.intelrx.utility.Auxiliary;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UpdateEmployeeInformation {
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final ContactInfoRepository contactInfoRepository;
    private final JobInformationRepository jobInformationRepository;
    private final CompensationDetailRepository compensationDetailRepository;
    private final EducationRepository educationRepository;
    private final LegalRepository legalRepository;
    private final BenefitRepository benefitRepository;
    private final ExtraInformationRepository extraInformationRepository;
    private final EmployeeDocumentRepository employeeDocumentRepository;
    private final Auxiliary auxiliary;
    private final EducationDegreeRepository educationDegreeRepository;
    private final WorkHistoryRepository workHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;
    private final FilterEmployee filterEmployee;

    public User updateToUserEntity(UserRequest userRequest, Employee employee) {

        User user = null;
        if (employee.getUser() == null) {
            // Create User entity
            user = new User();
        } else {
            // Update User entity
            user = employee.getUser();
        }

        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setPhoneNumber(userRequest.getPhoneNumber());
        user.setEmail(userRequest.getEmail());
        user.setBirthMonth(userRequest.getBirthMonth());
        user.setDayOfBirth(userRequest.getDayOfBirth());
        user.setYearOfBirth(userRequest.getYearOfBirth());
        user.setPassword(this.passwordEncoder.encode("12345678"));
        user.setUserType(UserType.EMPLOYEE);
        user.setRole(Role.USER);
        user.setStatus(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Save User entity
        userRepository.save(user);

        return user;
    }

    public Employee updateToEmployeeEntity(EmployeeRequest request, String employeeIntelRxId) {

        Optional<Employee> optionalEmployee = employeeRepository.findByEmployeeIntelRxId(employeeIntelRxId);
        Employee employee = null;
        if (optionalEmployee.isPresent()) {
            employee = optionalEmployee.get();
        } else {
            employee = new Employee();
        }
        // Set properties
        employee.setEmployeeType(request.getEmployeeType());

        // Save Employee entity
        var employeeDetails = employeeRepository.save(employee);

        return employeeDetails;
    }


    public ContactInfo updateToContactEntity(ContactInfoReqRes contactInfoReqRes, Employee employee) {
        ContactInfo contactInfo = null;
        if (employee.getContactInfo() == null) {
            // Create contactInfo entity
            contactInfo = new ContactInfo();
        } else {
            // Update contactInfo entity
            contactInfo = employee.getContactInfo();
        }

        // Set properties
        contactInfo.setEmployee(employee);
        contactInfo.setCountry(contactInfoReqRes.getCountry());
        contactInfo.setState(contactInfoReqRes.getState());
        contactInfo.setCity(contactInfoReqRes.getCity());
        contactInfo.setLga(contactInfoReqRes.getLga());
        contactInfo.setStreetAddress(contactInfoReqRes.getStreetAddress());

        // Save ContactInfo entity
        var contact = contactInfoRepository.save(contactInfo);

        return contact;
    }

    public JobInformation updateToJobInformationEntity(JobInformationRequest jobInformationRequest, Employee employee) {
        // Fetch related entities from auxiliary service
        JobTitle jobTitle = auxiliary.getJobTitle(jobInformationRequest.getJobTitleId());
        SeniorityLevel seniorityLevel = auxiliary.getSeniorityLevel(jobInformationRequest.getSeniorityLevelId());
        Department department = auxiliary.getDepartment(jobInformationRequest.getDepartmentId());
        WorkSchedule workSchedule = auxiliary.getWorkSchedule(jobInformationRequest.getWorkScheduleId());

        JobInformation jobInformation = null;
        if (employee.getJobInformation() == null) {
            // Create contactInfo entity
            jobInformation = new JobInformation();
            jobInformation.setEmployee(employee);
        } else {
            // Update contactInfo entity
            jobInformation = employee.getJobInformation();
        }

        // Create JobInformation entity and set properties

        jobInformation.setJobTitle(jobTitle);
        jobInformation.setSeniorityLevel(seniorityLevel);
        jobInformation.setDepartment(department);
        jobInformation.setWorkSchedule(workSchedule);
        jobInformation.setJobScope(jobInformationRequest.getJobScope());
        jobInformation.setStartDate(jobInformationRequest.getStartDate());
        jobInformation.setEndDate(jobInformationRequest.getEndDate());

        // Save JobInformation entity
        var jobInfo = jobInformationRepository.save(jobInformation);

        return jobInfo;
    }

    public CompensationDetail updateToCompensationDetailEntity(CompensationDetailRequest compensationDetailRequest, Employee employee) {
        // Fetch related entities from auxiliary service
        PaymentFrequency paymentFrequency = auxiliary.getPaymentFrequency(compensationDetailRequest.getPaymentFrequencyId());
        SalaryType salaryType = auxiliary.getSalaryType(compensationDetailRequest.getSalaryTypeId());

        CompensationDetail compensationDetail = null;
        if (employee.getCompensationDetail() == null) {
            // Create contactInfo entity
            compensationDetail = new CompensationDetail();
            compensationDetail.setEmployee(employee);
        } else {
            // Update contactInfo entity
            compensationDetail = employee.getCompensationDetail();
        }

        // Create CompensationDetail entity and set properties

        compensationDetail.setAccountNumber(compensationDetailRequest.getAccountNumber());
        compensationDetail.setAccountName(compensationDetailRequest.getAccountName());
        compensationDetail.setBankName(compensationDetailRequest.getBankName());
        compensationDetail.setSalary(compensationDetailRequest.getSalary());
        compensationDetail.setPaymentFrequency(paymentFrequency);
        compensationDetail.setSalaryType(salaryType);
        compensationDetail.setCreatedAt(LocalDateTime.now());
        compensationDetail.setUpdatedAt(LocalDateTime.now());

        // Save CompensationDetail entity
        var compesate = compensationDetailRepository.save(compensationDetail);

        return compesate;
    }

    public Education updateToEducationEntity(EducationRequest educationRequest, Employee employee) {

        // Create an Education instance and set its fields
        Education education = null;

        if (employee.getEducation() == null) {
            // Create contactInfo entity
            education = new Education();
            education.setEmployee(employee);
        } else {
            // Update contactInfo entity
            education = employee.getEducation();
        }

        education.setLicense(educationRequest.getLicense());

        // Create EducationDegree instances and set their fields
        List<EducationDegreeRequest> degreeDTOs = educationRequest.getEducationDegreeRequests();

        if (degreeDTOs != null && !degreeDTOs.isEmpty()) {
            List<EducationDegree> educationDegrees = new ArrayList<>();
            for (EducationDegreeRequest degreeDTO : degreeDTOs) {
                Optional<EducationDegree> degreeOptional = educationDegreeRepository.findByEducationId(education.getId());

                EducationDegree degree = null;

                if (degreeOptional.isEmpty() || degreeOptional == null) {
                    // Create contactInfo entity
                    degree = new EducationDegree();
                    degree.setEducation(education);
                } else {
                    // Update contactInfo entity
                    degree.setCertification(degreeDTO.getCertification());
                    degree.setInstitution(degreeDTO.getInstitution());
                }


                educationDegrees.add(degree);
            }

            // Save all EducationDegree instances
            educationDegreeRepository.saveAll(educationDegrees);

            // Set the list of EducationDegrees to the Education instance
            education.setEducationDegrees(educationDegrees);
        }


        // Create WorkHistory instances and set their fields
        List<WorkHistoryRequest> workHistoryRequestList = educationRequest.getWorkHistoryRequests();

        if (workHistoryRequestList != null && !workHistoryRequestList.isEmpty()) {
            List<WorkHistory> workHistories = new ArrayList<>();
            for (WorkHistoryRequest workHistoryRequest : workHistoryRequestList) {
                Optional<WorkHistory> optionalWorkHistory = workHistoryRepository.findByEducationId(education.getId());

                WorkHistory workHistory = null;

                if (optionalWorkHistory.isEmpty() || optionalWorkHistory == null) {
                    workHistory = new WorkHistory();
                    workHistory.setEducation(education); // Set the relationship
                } else {
                    workHistory.setDuration(workHistoryRequest.getDuration());
                    workHistory.setJobTitle(workHistoryRequest.getJobTitle());
                    workHistory.setCompany(workHistoryRequest.getCompany());
                }

                workHistories.add(workHistory);
            }

            // Save all WorkHistory instances
            workHistoryRepository.saveAll(workHistories);

            // Set the list of WorkHistory to the Education instance
            education.setWorkHistory(workHistories);
        }

        // Save the Education instance to persist both entities
        var eduInfo = educationRepository.save(education);

        return eduInfo;
    }

    public Legal updateLegalEntity(LegalRequest legalRequest, Employee employee) {
        Legal legal = null;
        if (employee.getLegal() == null) {
            // Create contactInfo entity
            legal = new Legal();
            legal.setEmployee(employee);
        } else {
            // Update contactInfo entity
            legal = employee.getLegal();
        }

        // Set properties
        legal.setNinSsn(legalRequest.getNinSsn());
        legal.setWorkAuthorization(legalRequest.getWorkAuthorization());
        legal.setStatus(legalRequest.getStatus());

        // Save Legal entity
        var legalDetails = legalRepository.save(legal);

        return legalDetails;
    }

    public Benefit updateBenefitEntity(BenefitRequest benefitRequest, Employee employee) {
        Benefit benefit = null;
        if (employee.getBenefit() == null) {
            // Create contactInfo entity
            benefit = new Benefit();
            benefit.setEmployee(employee);
        } else {
            // Update contactInfo entity
            benefit = employee.getBenefit();
        }
        // Set properties
        benefit.setBeneficiary(benefitRequest.getBeneficiary());
        benefit.setBenefits(benefitRequest.getBenefits());

        // Save Benefit entity
        var benefitInfo = benefitRepository.save(benefit);

        return benefitInfo;
    }

    public ExtraInformation updateExtraInformationEntity(ExtraInformationRequest extraInformationRequest, Employee employee) {

        ExtraInformation extraInformation = null;
        if (employee.getExtraInformation() == null) {
            // Create contactInfo entity
            extraInformation = new ExtraInformation();
            extraInformation.setEmployee(employee);
        } else {
            // Update contactInfo entity
            extraInformation = employee.getExtraInformation();
        }

        // Set properties
        extraInformation.setDisabilityStatus(extraInformationRequest.getDisabilityStatus());
        extraInformation.setEmergencyContactName(extraInformationRequest.getEmergencyContactName());
        extraInformation.setEmergencyContactNumber(extraInformationRequest.getEmergencyContactNumber());
        extraInformation.setLanguage(extraInformationRequest.getLanguage());
        extraInformation.setRefereeName(extraInformationRequest.getRefereeName());
        extraInformation.setRefereeNumber(extraInformationRequest.getRefereeNumber());
        extraInformation.setRelationshipWithReferee(extraInformationRequest.getRelationshipWithReferee());
        extraInformation.setPreferredNickname(extraInformationRequest.getPreferredNickname());
        extraInformation.setRelationshipWithEmergency(extraInformationRequest.getRelationshipWithEmergency());

        // Save ExtraInformation entity
        var extraInfo = extraInformationRepository.save(extraInformation);

        return extraInfo;
    }

    public EmployeeDocument updateEmployeeDocumentEntity(List<EmployeeDocumentRequest> employeeDocumentRequestList, Employee employee) throws IOException {
        List<EmployeeDocument> employeeDocumentList = new ArrayList<>();
        if (employeeDocumentRequestList != null && !employeeDocumentRequestList.isEmpty()) {

            for (EmployeeDocumentRequest documentRequest : employeeDocumentRequestList) {

                DocumentType documentType = auxiliary.getDocumentType(documentRequest.getDocumentTypeId());
                // Save the file to the upload directory
                String fileName = s3Service.uploadFileDoc(documentRequest.getFileDoc(),"employee");

                EmployeeDocument employeeDocument = null;
                if (employee.getEmployeeDocument() == null) {
                    // Create contactInfo entity
                    employeeDocument = new EmployeeDocument();
                    employeeDocument.setEmployee(employee);
                } else {
                    // Update contactInfo entity
                    employeeDocument = (EmployeeDocument) employee.getEmployeeDocument();
                }

                employeeDocument.setEmployee(employee);
                employeeDocument.setDocumentType(documentType);
                employeeDocument.setFileDoc(fileName);

                employeeDocumentList.add(employeeDocument);
            }

            // Save all WorkHistory instances
            employeeDocumentRepository.saveAll(employeeDocumentList);
        }
        return (EmployeeDocument) employeeDocumentList;
    }
}
