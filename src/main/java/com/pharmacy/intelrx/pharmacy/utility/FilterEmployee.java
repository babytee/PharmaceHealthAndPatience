package com.pharmacy.intelrx.pharmacy.utility;


import com.pharmacy.intelrx.pharmacy.dto.*;
import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.pharmacy.dto.employee.*;
import com.pharmacy.intelrx.pharmacy.models.BranchEmployee;
import com.pharmacy.intelrx.pharmacy.models.ConcludeEmployee;
import com.pharmacy.intelrx.pharmacy.models.ContactInfo;
import com.pharmacy.intelrx.pharmacy.models.PharmacyBranch;
import com.pharmacy.intelrx.pharmacy.models.employee.*;
import com.pharmacy.intelrx.pharmacy.repositories.*;
import com.pharmacy.intelrx.pharmacy.repositories.employee.*;
import com.pharmacy.intelrx.auxilliary.repositories.UserRepository;
import com.pharmacy.intelrx.auxilliary.services.S3Service;
import com.pharmacy.intelrx.utility.Auxiliary;
import com.pharmacy.intelrx.utility.UserDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Builder
@RequiredArgsConstructor
@Component
public class FilterEmployee {
    private final Utility utility;
    private final EmployeeRepository employeeRepository;
    private final JobInformationRepository jobInformationRepository;
    private final EmployeeValidation employeeValidation;
    private final UserRepository userRepository;
    private final ContactInfoRepository contactInfoRepository;
    private final CompensationDetailRepository compensationDetailRepository;
    private final EducationRepository educationRepository;
    private final LegalRepository legalRepository;
    private final BenefitRepository benefitRepository;
    private final ExtraInformationRepository extraInformationRepository;
    private final EmployeeDocumentRepository employeeDocumentRepository;
    private final Auxiliary auxiliary;
    private final EducationDegreeRepository educationDegreeRepository;
    private final WorkHistoryRepository workHistoryRepository;
    private final PharmacyRepository pharmacyRepository;
    private final PharmacyBranchRepository branchRepository;
    private final S3Service s3Service;
    private final ConcludeEmployeeRepository concludeEmployeeRepository;
    private final UserDetailsService userDetailsService;
    private final BranchEmployeeRepository branchEmployeeRepository;
    private final AssignShiftRepository assignShiftRepository;
    private final SalaryPaymentRepository salaryPaymentRepository;

    public Page<EmployeeResponse> FilterEmployeeInfo(EmployeeFilterRequest request, Pageable pageable) {

        Page<Employee> employeeList = filterEmployees(request, pageable);
        Page<EmployeeResponse> responseList = employeeList.map(employee -> mapToEmployeeResponse(employee));

        // Check if the employeeList is empty
        if (employeeList.isEmpty()) {
            return null;
        }

        return responseList;
    }

    public Page<EmployeeResponse> FilterEmployeeByShift(EmployeeFilterRequest request, Pageable pageable) {

        Page<Employee> employeeList = filterEmployees(request, pageable);
        Page<EmployeeResponse> responseList = employeeList.map(employee -> mapToEmployeeResponse(employee));

        // Check if the employeeList is empty
        if (employeeList.isEmpty()) {
            return null;
        }

        return responseList;
    }

    private Page<Employee> filterEmployees(EmployeeFilterRequest filterRequest, Pageable pageable) {
        return employeeRepository.findAllByFilterRequest(
                filterRequest.getUserId(),
                filterRequest.getWorkerStatus(),
                filterRequest.getEmployeeType(),
                filterRequest.getSalaryStatus(),
                filterRequest.getSalaryTypeId(),
                filterRequest.getJobTitleId(),
                filterRequest.getBranchId(),
                filterRequest.getBirthMonth(),
                filterRequest.getIntelRxId(),
                filterRequest.getSearchText(),
                pageable
        );
    }

    public EmployeeResponse mapToEmployeeResponse(Employee employee) {
        EmployeeResponse response = new EmployeeResponse();
        response.setId(employee.getId());
        response.setEmployeeIntelRxId(employee.getEmployeeIntelRxId());
        response.setEmployeeType(employee.getEmployeeType());
        response.setStatus(employee.isStatus());

        //PersonalInformation
        if (employee.getUser() == null) {
            response.setPersonalInformation(null);
        } else {
            UserRequest userRequest = mapToUserInfo(employee);
            response.setPersonalInformation(userRequest);
        }

        //Contact Information
        ContactInfoReqRes contactInfoReqRes = mapToContactInfo(employee);
        response.setContactInformation(contactInfoReqRes);

        //Job Information
        JobInformationRequest jobInformationRequest = mapToJobInfo(employee);
        response.setJobInformation(jobInformationRequest);

        //Compensation Details
        CompensationDetailRequest compensationDetailRequest = mapToCompensationDetail(employee);
        response.setCompensationDetails(compensationDetailRequest);

        //Compensation Details
        EducationRequest educationRequest = mapToEducation(employee);
        response.setEducationQualification(educationRequest);

        //Legal Details
        LegalRequest legalRequest = mapToLegal(employee);
        response.setLegal(legalRequest);

        //Benefit Details
        BenefitRequest benefitRequest = mapToBenefit(employee);
        response.setBenefit(benefitRequest);

        //ExtraInformation Details
        ExtraInformationRequest extraInformationRequest = mapToExtraInformation(employee);
        if (extraInformationRequest == null) {
            response.setExtraInformation(null);
        } else {
            response.setExtraInformation(extraInformationRequest);
        }

        //ConcludeEmployee Details
        ConcludeEmployeeRequest concludeEmployeeRequest = mapToConcludeEmployee(employee);
        response.setConcludedEmployeeStatus(concludeEmployeeRequest);


        //Employee AssignShift Details
        var intelRxId = employee.getEmployeeIntelRxId();
        PharmacyBranchResponse branch = mapToBranchResponse(employee);

        Optional<PharmacyBranch> branchOptional = branchRepository.findByIdAndIntelRxIdAndEmployeeId(
                branch.getId(), intelRxId, employee.getId());
        if (branchOptional.isPresent()) {
            PharmacyBranch pharmacyBranch = branchOptional.get();

            List<AssignShift> assignShiftList = assignShiftRepository.findAllByAssignedMemberAndIntelRxIdAndPharmacyBranch(
                    employee, intelRxId, pharmacyBranch == null ? null : pharmacyBranch
            );

            List<AssignShiftResponse> assignShiftResponses = assignShiftList.stream().map(assignShift ->
                    mapToAssignShift(assignShift)
            ).collect(Collectors.toList());
            response.setAssignShiftResponse(assignShiftResponses);
        }


        //EmployeeDocument Details
        List<EmployeeDocument> employeeDocumentList = employeeDocumentRepository.findAllByEmployeeId(employee.getId());

        List<EmployeeDocumentRequest> employeeDocumentRequestList = (List) employeeDocumentList.stream().map((address) -> {
            try {
                return this.mapToEmployeeDocument(address);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        response.setEmployeeDocuments(employeeDocumentRequestList);

        if (employee.getUser() == null) {
            response.setBranches(null);
        } else {
            //Branch Information
            var branchInfo = mapToBranchEmployeeResponse(employee);
            response.setBranches(branchInfo);
        }

        return response;
    }

    public AssignShiftResponse mapToAssignShift(AssignShift assignShift) {

        //AssignShift assignShift = optional.get();
        //String shiftDateTime = utility.addHoursAndFormat(String.valueOf(assignShift.getCreatedAt()),assignShift.getShiftTime());
        return AssignShiftResponse.builder()
                .id(assignShift.getId())
                .shiftHours(assignShift.getShiftTime())
                .startTime(assignShift.getStartTime())
                .endTime(assignShift.getEndTime())
                .createdAt(assignShift.getCreatedAt())
                .shiftDate(assignShift.getStartDate())
                .assignedColor(assignShift.getAssignedColor())
                .jobTitle(assignShift.getJobTitle())
                .build();
    }

    public ConcludeEmployeeRequest mapToConcludeEmployee(Employee employee) {
        Optional<ConcludeEmployee> optional = concludeEmployeeRepository.findByEmployeeIdAndEmployeeIntelRxId(
                employee.getId(), employee.getEmployeeIntelRxId());
        if (!optional.isPresent()) {
            return null;
        }
        ConcludeEmployee concludeEmployee = optional.get();

        return ConcludeEmployeeRequest.builder()
                .employeeId(employee.getId())
                .concludeType(concludeEmployee.getConcludeType())
                .reasons(concludeEmployee.getReasons())
                .build();
    }

    private PharmacyBranchResponse mapToBranchEmployeeResponse(Employee employee) {
        var branchEmployeeOptional = branchEmployeeRepository.findByEmployee(employee);
        if (branchEmployeeOptional.isPresent()) {
            BranchEmployee branchEmployee = branchEmployeeOptional.get();
            var optionalUser = userRepository.findById(branchEmployee.getUser().getId());
            //if(optionalUser.isPresent())
            User user = optionalUser.get();

            PharmacyBranchResponse branchResponse = new PharmacyBranchResponse();

            if (branchEmployee.getPharmacyBranch() != null) {
                PharmacyBranch pharmacyBranch = branchEmployee.getPharmacyBranch();
                branchResponse.setId(pharmacyBranch.getId());
                branchResponse.setName(pharmacyBranch.getName());

                PharmacyRequest pharmacyRequest = new PharmacyRequest();
                pharmacyRequest.setId(pharmacyBranch.getPharmacy().getId());
                pharmacyRequest.setIntelRxId(pharmacyBranch.getPharmacy().getIntelRxId());
                pharmacyRequest.setPharmacyName(pharmacyBranch.getPharmacy().getPharmacyName());

                ContactInfoReqRes contactInfoReqRes = new ContactInfoReqRes();
                if (pharmacyBranch.getContactInfo() != null) {
                    contactInfoReqRes.setId(pharmacyBranch.getId());
                    contactInfoReqRes.setCountry(pharmacyBranch.getContactInfo().getCountry());
                    contactInfoReqRes.setState(pharmacyBranch.getContactInfo().getState());
                    contactInfoReqRes.setCity(pharmacyBranch.getContactInfo().getCity());
                    contactInfoReqRes.setLga(pharmacyBranch.getContactInfo().getLga());
                    contactInfoReqRes.setStreetAddress(pharmacyBranch.getContactInfo().getStreetAddress());
                    contactInfoReqRes.setZipCode(pharmacyBranch.getContactInfo().getZipCode());
                }

                //Manager Information
                var userRequest = mapToUserInfo(employee);

                branchResponse.setPharmacyInfo(pharmacyRequest);
                //branchResponse.setContactInfo(contactInfoReqRes);
                branchResponse.setManagerInfo(userRequest);
            }

            return branchResponse;
        } else {
            return null;
        }


    }

    public PharmacyBranchResponse mapToBranchResponse(Employee employee) {

        var optionalUser = userRepository.findById(employee.getUser().getId());
        //if(optionalUser.isPresent())
        User user = optionalUser.get();

        PharmacyBranchResponse branchResponse = new PharmacyBranchResponse();
        var optionalBranch = branchRepository.findByUserId(user.getId());

        if (optionalBranch.isPresent()) {
            PharmacyBranch pharmacyBranch = optionalBranch.get();
            branchResponse.setId(pharmacyBranch.getId());
            branchResponse.setName(pharmacyBranch.getName());

            PharmacyRequest pharmacyRequest = new PharmacyRequest();
            pharmacyRequest.setId(pharmacyBranch.getPharmacy().getId());
            pharmacyRequest.setIntelRxId(pharmacyBranch.getPharmacy().getIntelRxId());
            pharmacyRequest.setPharmacyName(pharmacyBranch.getPharmacy().getPharmacyName());

            ContactInfoReqRes contactInfoReqRes = new ContactInfoReqRes();
            contactInfoReqRes.setId(pharmacyBranch.getId());
            contactInfoReqRes.setCountry(pharmacyBranch.getContactInfo().getCountry());
            contactInfoReqRes.setState(pharmacyBranch.getContactInfo().getState());
            contactInfoReqRes.setCity(pharmacyBranch.getContactInfo().getCity());
            contactInfoReqRes.setLga(pharmacyBranch.getContactInfo().getLga());
            contactInfoReqRes.setStreetAddress(pharmacyBranch.getContactInfo().getStreetAddress());
            contactInfoReqRes.setZipCode(pharmacyBranch.getContactInfo().getZipCode());


            //Manager Information
            var userRequest = mapToUserInfo(employee);

            branchResponse.setPharmacyInfo(pharmacyRequest);
            branchResponse.setContactInfo(contactInfoReqRes);
            branchResponse.setManagerInfo(userRequest);
        }

        return branchResponse;
    }

    public UserRequest mapToUserInfo(Employee employee) {
        Optional<User> optional = userRepository.findById(employee.getUser().getId());

        UserRequest request = new UserRequest();
        if (optional.isPresent()) {
            User user = optional.get();
            // String dob = user.getDayOfBirth()+"-"+ user.getBirthMonth()+"-"+ user.getYearOfBirth();
            request.setId(user.getId());
            request.setEmployeeId(employee.getId());
            request.setFirstName(user.getFirstName());
            request.setLastName(user.getLastName());
            request.setGender(user.getGender());
            request.setEmail(user.getEmail());
            request.setPhoneNumber(user.getPhoneNumber());
            request.setDayOfBirth(user.getDayOfBirth());
            request.setBirthMonth(user.getBirthMonth());
            request.setYearOfBirth(user.getYearOfBirth());
            request.setUserStatus(user.getUserStatus());
            request.setUserStatus(user.getUserStatus());
        }

        return request;

    }

    private ContactInfoReqRes mapToContactInfo(Employee employee) {
        Optional<ContactInfo> optional = contactInfoRepository.findByEmployeeId(employee.getId());

        ContactInfoReqRes request = new ContactInfoReqRes();
        if (optional.isPresent()) {
            ContactInfo contactInfo = optional.get();
            request.setId(contactInfo.getId());
            request.setCountry(contactInfo.getCountry());
            request.setState(contactInfo.getState());
            request.setCity(contactInfo.getCity());
            request.setLga(contactInfo.getLga());
            request.setStreetAddress(contactInfo.getStreetAddress());
            request.setZipCode(contactInfo.getZipCode());
        }

        return request;

    }

    //using employee model
    public JobInformationRequest mapToJobInfo(Employee employee) {
        Optional<JobInformation> optional = jobInformationRepository.findByEmployeeId(employee.getId());

        JobInformationRequest request = new JobInformationRequest();
        if (optional.isPresent()) {
            JobInformation jobInformation = optional.get();
            request.setId(jobInformation.getId());
            request.setJobTitle(jobInformation.getJobTitle().getName());
//            request.setDepartment(jobInformation.getDepartment().getName());
//            request.setSeniorityLevel(jobInformation.getSeniorityLevel().getName());
            request.setWorkSchedule(jobInformation.getWorkSchedule().getName());
            request.setJobScope(jobInformation.getJobScope());
            request.setStartDate(jobInformation.getStartDate());
            request.setEndDate(jobInformation.getEndDate());
        }

        return request;

    }

    private CompensationDetailRequest mapToCompensationDetail(Employee employee) {

        Optional<CompensationDetail> optional = compensationDetailRepository.findByEmployeeId(employee.getId());

        CompensationDetailRequest request = new CompensationDetailRequest();
        if (optional.isPresent()) {
            CompensationDetail compensationDetail = optional.get();

            request.setId(compensationDetail.getId());
            request.setSalaryType(compensationDetail.getSalaryType().getName());

            if (compensationDetail.getPaymentFrequency() != null) {
                request.setPaymentFrequency(compensationDetail.getPaymentFrequency().getName());
            }

            String currentYearMonth = salaryPaymentRepository.getCurrentYearMonth();


            boolean isPaid = salaryPaymentRepository.isSalaryPaidForCurrentMonth(
                    employee.getId(), currentYearMonth);

            int dueDays = salaryPaymentRepository.countOverduePayments(employee.getId(), currentYearMonth);

            if (isPaid) {
                request.setSalaryStatus("Paid");
            } else {
                if (dueDays < 0) {
                    request.setSalaryStatus("Paid");
                }else {
                    request.setSalaryStatus("Overdue By " + dueDays + " Days");
                }
            }


            request.setSalary(compensationDetail.getSalary());
            request.setBankName(compensationDetail.getBankName());
            request.setAccountNumber(compensationDetail.getAccountNumber());
            request.setAccountName(compensationDetail.getAccountName());
        }

        return request;

    }

    private EducationRequest mapToEducation(Employee employee) {

        Optional<Education> optional = educationRepository.findByEmployeeId(employee.getId());

        EducationRequest request = new EducationRequest();
        if (optional.isPresent()) {
            Education education = optional.get();
            //education degree
            List<EducationDegree> degreeList = educationDegreeRepository.findAllByEducationId(education.getId());

            List<EducationDegreeRequest> degreeRequestList = (List) degreeList.stream().map((address) -> {
                return this.mapToEducationDegree(address);
            }).collect(Collectors.toList());

            //work history
            List<WorkHistory> workHistoryList = workHistoryRepository.findAllByEducationId(education.getId());

            List<WorkHistoryRequest> workHistoryRequestList = (List) workHistoryList.stream().map((address) -> {
                return this.mapToWorkHistory(address);
            }).collect(Collectors.toList());

            request.setId(education.getId());
            request.setLicense(education.getLicense());
            request.setEducationDegree(degreeRequestList);
            request.setWorkHistory(workHistoryRequestList);

        }

        return request;

    }

    //subset of education
    private EducationDegreeRequest mapToEducationDegree(EducationDegree degree) {
        EducationDegreeRequest degreeRequest = new EducationDegreeRequest();
        degreeRequest.setCertification(degree.getCertification());
        degreeRequest.setInstitution(degree.getInstitution());

        return degreeRequest;
    }

    //subset of education
    private WorkHistoryRequest mapToWorkHistory(WorkHistory workHistory) {
        WorkHistoryRequest workHistoryRequest = new WorkHistoryRequest();
        workHistoryRequest.setCompany(workHistory.getCompany());
        workHistoryRequest.setJobTitle(workHistory.getJobTitle());
        workHistoryRequest.setDuration(workHistory.getDuration());

        return workHistoryRequest;
    }

    private LegalRequest mapToLegal(Employee employee) {

        Optional<Legal> optional = legalRepository.findByEmployeeId(employee.getId());

        LegalRequest request = new LegalRequest();
        if (optional.isPresent()) {
            Legal legal = optional.get();
            request.setId(legal.getId());
            request.setStatus(legal.getStatus());
            request.setNinSsn(legal.getNinSsn());
            request.setWorkAuthorization(legal.getWorkAuthorization());
        }

        return request;

    }

    private BenefitRequest mapToBenefit(Employee employee) {

        Optional<Benefit> optional = benefitRepository.findByEmployeeId(employee.getId());

        BenefitRequest request = new BenefitRequest();
        if (optional.isPresent()) {
            Benefit benefit = optional.get();
            request.setId(benefit.getId());
            request.setBenefits(benefit.getBenefits());
            request.setBeneficiary(benefit.getBeneficiary());
        }

        return request;

    }

    private ExtraInformationRequest mapToExtraInformation(Employee employee) {
        Optional<ExtraInformation> optional = extraInformationRepository.findByEmployeeId(employee.getId());

        ExtraInformationRequest request = new ExtraInformationRequest();
        if (optional.isPresent()) {
            ExtraInformation extraInformation = employee.getExtraInformation();
            request.setId(employee.getExtraInformation().getId());
            request.setLanguage(extraInformation.getLanguage());
            request.setDisabilityStatus(extraInformation.getDisabilityStatus());
            request.setRefereeName(extraInformation.getRefereeName());
            request.setRefereeNumber(extraInformation.getRefereeNumber());
            request.setEmergencyContactName(extraInformation.getEmergencyContactName());
            request.setEmergencyContactNumber(extraInformation.getEmergencyContactNumber());
            request.setPreferredNickname(extraInformation.getPreferredNickname());
            request.setRelationshipWithEmergency(extraInformation.getRelationshipWithEmergency());
        }
        return request;


    }

    private EmployeeDocumentRequest mapToEmployeeDocument(EmployeeDocument employeeDocument) throws IOException {

        EmployeeDocumentRequest request = new EmployeeDocumentRequest();

        request.setId(employeeDocument.getId());
        request.setDocumentType(employeeDocument.getDocumentType().getName());

        S3Service.FetchedImage fetchedImage = s3Service.fetchImage(employeeDocument.getFileDoc()); // Replace "your_image_name.jpg" with the actual image name
        String fileDoc = fetchedImage.getImageUrl();

        request.setFileDocLink(fileDoc);


        return request;

    }


}
