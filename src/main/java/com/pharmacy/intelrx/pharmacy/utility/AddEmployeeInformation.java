package com.pharmacy.intelrx.pharmacy.utility;

import com.pharmacy.intelrx.auxilliary.models.Role;
import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.auxilliary.models.UserType;
import com.pharmacy.intelrx.auxilliary.repositories.UserRepository;
import com.pharmacy.intelrx.auxilliary.services.S3Service;
import com.pharmacy.intelrx.pharmacy.dto.ContactInfoReqRes;
import com.pharmacy.intelrx.pharmacy.dto.UserRequest;
import com.pharmacy.intelrx.pharmacy.dto.employee.*;
import com.pharmacy.intelrx.pharmacy.models.ContactInfo;
import com.pharmacy.intelrx.pharmacy.models.auxilliary.*;
import com.pharmacy.intelrx.pharmacy.models.employee.*;
import com.pharmacy.intelrx.pharmacy.repositories.ContactInfoRepository;
import com.pharmacy.intelrx.pharmacy.repositories.employee.*;
import com.pharmacy.intelrx.utility.Auxiliary;
import com.pharmacy.intelrx.utility.UserDetailsService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class AddEmployeeInformation {
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
    private final UserDetailsService userDetailsService;


    @Transactional
    public EmployeeResponse AddEmployeeInfo(EmployeeRequest request) {

        try {
            // User Request
            UserRequest userRequest = request.getUserRequest();
            User user = addToUserEntity(userRequest);

            // Employee Request Added
            Employee employee = AddToEmployeeEntity(request, user);

            // User Request
            ContactInfoReqRes contactInfoReqRes = request.getContactInfoReqRes();
            AddToContactEntity(contactInfoReqRes, employee);

            // JobInformation Request
            JobInformationRequest jobInformationRequest = request.getJobInformationRequest();
            AddToJobInformationEntity(jobInformationRequest, employee);

            // CompensationDetail Request
            CompensationDetailRequest compensationDetailRequest = request.getCompensationDetailRequest();
            AddToCompensationDetailEntity(compensationDetailRequest, employee);

            // Education Request
            EducationRequest educationRequest = request.getEducationRequest();
            AddToEducationEntity(educationRequest, employee);

            // Legal Request
            LegalRequest legalRequest = request.getLegalRequest();
            AddLegalEntity(legalRequest, employee);

            // Benefit Request
            BenefitRequest benefitRequest = request.getBenefitRequest();
            AddBenefitEntity(benefitRequest, employee);

            // ExtraInformation Request
            ExtraInformationRequest extraInformationRequest = request.getExtraInformationRequest();
            AddExtraInformationEntity(extraInformationRequest, employee);

            // EmployeeDocument Request
            List<EmployeeDocumentRequest> employeeDocumentRequest = request.getEmployeeDocumentRequest();
            AddEmployeeDocumentEntity(employeeDocumentRequest, employee);

            EmployeeResponse employeeResponse = new EmployeeResponse(); //filterEmployee.mapToEmployeeResponse(employee);
            employeeResponse.setId(employee.getId());
            employeeResponse.setEmployeeIntelRxId(employee.getEmployeeIntelRxId());


            return employeeResponse;
        } catch (IOException e) {
            e.printStackTrace(); // Log the exception or handle it appropriately
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception or handle it appropriately

        }
        return null;
    }

    public User addToUserEntity(UserRequest userRequest) {

        User user = new User();
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setPhoneNumber(userRequest.getPhoneNumber());
        user.setEmail(userRequest.getEmail());
        user.setBirthMonth(userRequest.getBirthMonth());
        user.setDayOfBirth(userRequest.getDayOfBirth());
        user.setYearOfBirth(userRequest.getYearOfBirth());
        user.setGender(userRequest.getGender());
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

    public Employee AddToEmployeeEntity(EmployeeRequest request, User user) {
        Employee employee = new Employee();
        String intelRxId = userDetailsService.getIntelRxId();

        // Set properties
        employee.setUser(user);
        employee.setEmployeeType(request.getEmployeeType());
        employee.setEmployeeIntelRxId(intelRxId);

        // Save Employee entity
        var employeeDetails = employeeRepository.save(employee);

        return employeeDetails;
    }

    public ContactInfo AddToContactEntity(ContactInfoReqRes contactInfoReqRes, Employee employee) {
        ContactInfo contactInfo = new ContactInfo();

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

    public JobInformation AddToJobInformationEntity(JobInformationRequest jobInformationRequest, Employee employee) {
        // Fetch related entities from auxiliary service
        JobTitle jobTitle = auxiliary.getJobTitle(jobInformationRequest.getJobTitleId());
        //SeniorityLevel seniorityLevel = auxiliary.getSeniorityLevel(jobInformationRequest.getSeniorityLevelId());
        //Department department = auxiliary.getDepartment(jobInformationRequest.getDepartmentId());
        WorkSchedule workSchedule = auxiliary.getWorkSchedule(jobInformationRequest.getWorkScheduleId());

        // Create JobInformation entity and set properties
        JobInformation jobInformation = new JobInformation();
        jobInformation.setEmployee(employee);
        jobInformation.setJobTitle(jobTitle);
        //jobInformation.setSeniorityLevel(seniorityLevel);
        //jobInformation.setDepartment(department);
        jobInformation.setWorkSchedule(workSchedule);
        jobInformation.setJobScope(jobInformationRequest.getJobScope());
        jobInformation.setStartDate(jobInformationRequest.getStartDate());
        jobInformation.setEndDate(jobInformationRequest.getEndDate() == null ? null : jobInformationRequest.getEndDate());

        // Save JobInformation entity
        var jobInfo = jobInformationRepository.save(jobInformation);

        return jobInfo;
    }

    public CompensationDetail AddToCompensationDetailEntity(CompensationDetailRequest compensationDetailRequest, Employee employee) {
        // Fetch related entities from auxiliary service
        PaymentFrequency paymentFrequency = auxiliary.getPaymentFrequency(compensationDetailRequest.getPaymentFrequencyId());
        SalaryType salaryType = auxiliary.getSalaryType(compensationDetailRequest.getSalaryTypeId());

        // Create CompensationDetail entity and set properties
        CompensationDetail compensationDetail = new CompensationDetail();
        compensationDetail.setEmployee(employee);
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

    public Education AddToEducationEntity(EducationRequest educationRequest, Employee employee) {

        // Create an Education instance and set its fields
        Education education = new Education();
        if (educationRequest == null) {
            education.setEmployee(employee);
        } else {
            education.setEmployee(employee);
            education.setLicense(educationRequest.getLicense());

            // Create EducationDegree instances and set their fields
            List<EducationDegreeRequest> degreeDTOs = educationRequest.getEducationDegreeRequests();

            if (degreeDTOs != null && !degreeDTOs.isEmpty()) {
                List<EducationDegree> educationDegrees = new ArrayList<>();
                for (EducationDegreeRequest degreeDTO : degreeDTOs) {
                    EducationDegree degree = new EducationDegree();
                    degree.setCertification(degreeDTO.getCertification());
                    degree.setInstitution(degreeDTO.getInstitution());
                    // Set other fields...
                    degree.setEducation(education); // Set the relationship

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
                    WorkHistory workHistory = new WorkHistory();
                    workHistory.setDuration(workHistoryRequest.getDuration());
                    workHistory.setJobTitle(workHistoryRequest.getJobTitle());
                    workHistory.setCompany(workHistoryRequest.getCompany());
                    // Set other fields...
                    workHistory.setEducation(education); // Set the relationship

                    workHistories.add(workHistory);
                }

                // Save all WorkHistory instances
                workHistoryRepository.saveAll(workHistories);

                // Set the list of WorkHistory to the Education instance
                education.setWorkHistory(workHistories);
            }
        }
        // Save the Education instance to persist both entities
        var eduInfo = educationRepository.save(education);

        return eduInfo;
    }

    public Legal AddLegalEntity(LegalRequest legalRequest, Employee employee) {
        Legal legal = new Legal();
        if (legalRequest == null) {
            legal.setEmployee(employee);
        } else {
            // Set properties
            legal.setEmployee(employee);
            legal.setNinSsn(legalRequest.getNinSsn());
            legal.setWorkAuthorization(legalRequest.getWorkAuthorization());
            legal.setStatus(legalRequest.getStatus());
        }

        // Save Legal entity
        var legalDetails = legalRepository.save(legal);

        return legalDetails;
    }

    public Benefit AddBenefitEntity(BenefitRequest benefitRequest, Employee employee) {
        Benefit benefit = new Benefit();
        if (benefitRequest == null) {
            benefit.setEmployee(employee);
        } else {
            // Set properties
            benefit.setEmployee(employee);
            benefit.setBeneficiary(benefitRequest.getBeneficiary());
            benefit.setBenefits(benefitRequest.getBenefits());
        }
        // Save Benefit entity
        var benefitInfo = benefitRepository.save(benefit);
        return benefitInfo;
    }

    public ExtraInformation AddExtraInformationEntity(ExtraInformationRequest extraInformationRequest, Employee employee) {

        ExtraInformation extraInformation = new ExtraInformation();

        // Set properties
        extraInformation.setEmployee(employee);
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

    public void AddEmployeeDocumentEntity(List<EmployeeDocumentRequest> employeeDocumentRequestList, Employee employee) throws IOException {
        List<EmployeeDocument> employeeDocumentList = new ArrayList<>();
        if (employeeDocumentRequestList != null && !employeeDocumentRequestList.isEmpty()) {

            for (EmployeeDocumentRequest documentRequest : employeeDocumentRequestList) {

                DocumentType documentType = auxiliary.getDocumentType(documentRequest.getDocumentTypeId());
                // Save the file to the upload directory
                String fileName = s3Service.uploadFileDoc(documentRequest.getFileDoc(), "employee");

                EmployeeDocument employeeDocument = new EmployeeDocument();
                employeeDocument.setEmployee(employee);
                employeeDocument.setDocumentType(documentType);
                employeeDocument.setFileDoc(fileName);

                employeeDocumentList.add(employeeDocument);
            }

            // Save all WorkHistory instances
            employeeDocumentRepository.saveAll(employeeDocumentList);
        } else {
            EmployeeDocument employeeDocument = new EmployeeDocument();
            employeeDocument.setEmployee(employee);
            employeeDocumentRepository.save(employeeDocument);
        }
    }
}
