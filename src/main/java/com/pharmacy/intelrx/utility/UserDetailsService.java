package com.pharmacy.intelrx.utility;

import com.pharmacy.intelrx.PCNAPICrawling.PremisesDataDTO;
import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.auxilliary.models.UserType;
import com.pharmacy.intelrx.auxilliary.repositories.UserRepository;
import com.pharmacy.intelrx.auxilliary.services.S3Service;
import com.pharmacy.intelrx.pharmacy.dto.UserRequest;
import com.pharmacy.intelrx.pharmacy.dto.employee.JobInformationRequest;
import com.pharmacy.intelrx.pharmacy.models.BranchEmployee;
import com.pharmacy.intelrx.pharmacy.models.Pharmacy;
import com.pharmacy.intelrx.pharmacy.models.PharmacyBranch;
import com.pharmacy.intelrx.pharmacy.models.auxilliary.JobTitle;
import com.pharmacy.intelrx.pharmacy.models.employee.Employee;
import com.pharmacy.intelrx.pharmacy.models.employee.JobInformation;
import com.pharmacy.intelrx.pharmacy.repositories.BranchEmployeeRepository;
import com.pharmacy.intelrx.pharmacy.repositories.PharmacyBranchRepository;
import com.pharmacy.intelrx.pharmacy.repositories.PharmacyRepository;
import com.pharmacy.intelrx.pharmacy.repositories.employee.EmployeeRepository;
import com.pharmacy.intelrx.pharmacy.utility.PharmacyWalletConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RequiredArgsConstructor
@Service("UserDetailsService")
public class UserDetailsService {
    private final EmployeeRepository employeeRepository;
    private final PharmacyRepository pharmacyRepository;
    private final BranchEmployeeRepository branchEmployeeRepository;
    private final S3Service s3Service;
    private final UserRepository userRepository;
    private final PharmacyWalletConfig pharmacyWalletConfig;

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized user");
        }
        return (User) authentication.getPrincipal();
    }

    public UserRequest getAuthProfile() {
        User user = getAuthenticatedUser();
        return mapToUserResponse(user);
    }

    public Pharmacy getPharmacyInfo() {
        Pharmacy pharmacy = null;
        User user = getAuthenticatedUser();

        PharmacyBranch branch = getBranch();
        if(branch==null){
            Optional<Pharmacy> optionalPharmacy = pharmacyRepository.findByUserId(user.getId());
            if (optionalPharmacy.isPresent()) {
                pharmacy = optionalPharmacy.get();
            }
        }else{
            pharmacy = branch.getPharmacy();
        }



        return pharmacy;
    }

    public boolean verifyIfPharmacyInfoExist(PremisesDataDTO premisesData, UserRequest userRequest) {
        Optional<Pharmacy> optionalPharmacy = pharmacyRepository.findByPremisesId(premisesData.getPremisesId());
        Optional<User> optionalUser = userRepository.findByEmail(userRequest.getEmail());
        // If either the user or the pharmacy is not present, return false
        if (!optionalUser.isPresent() || !optionalPharmacy.isPresent()) {
            return false;
        }
        // Both user and pharmacy exist
        return true;
    }




    public String getIntelRxId() {
        Pharmacy pharmacy = null;
        Employee employee = null;

        User user = getAuthenticatedUser();
        String intelRxId = "";
        //if (user.getUserType().equals("OWNER")) {
        Optional<Pharmacy> optionalPharmacy = pharmacyRepository.findByUserId(user.getId());
        if (optionalPharmacy.isPresent()) {
            pharmacy = optionalPharmacy.get();
            intelRxId = pharmacy.getIntelRxId();
        }
        //} else {
        Optional<Employee> optionalEmployee = employeeRepository.findByUserId(user.getId());
        if (optionalEmployee.isPresent()) {
            employee = optionalEmployee.get();
            intelRxId = employee.getEmployeeIntelRxId();
        }
        //}
        //creating new pharmacy wallet
        pharmacyWalletConfig.createWallet(intelRxId);
        return intelRxId;
    }

    public PharmacyBranch getBranch() {
        PharmacyBranch branch = null;
        User user = getAuthenticatedUser();
        String intelRxId = getIntelRxId();
        Optional<Employee> optionalEmployee = employeeRepository.findByUserIdAndEmployeeIntelRxId(user.getId(), intelRxId);
        if (optionalEmployee.isPresent()) {
            Employee employee = optionalEmployee.get();
            Optional<BranchEmployee> branchOptional = branchEmployeeRepository.findByEmployee(employee);
            if (branchOptional.isPresent()) {
                BranchEmployee branchEmployee = branchOptional.get();
                branch = branchEmployee.getPharmacyBranch();
            }
        }
        return branch;
    }

    public UserRequest mapToUserResponse(User user) {
        UserRequest userResponse = new UserRequest();
        Pharmacy pharmacy;
        Employee employee;

        userResponse.setId(user.getId());
        if (user.getUserType().equals("OWNER")) {
            Optional<Pharmacy> optionalPharmacy = pharmacyRepository.findByUserId(user.getId());
            if (optionalPharmacy.isPresent()) {
                pharmacy = optionalPharmacy.get();
                userResponse.setIntelRxId(pharmacy.getIntelRxId());
            }
        } else {
            Optional<Employee> optionalEmployee = employeeRepository.findByUserId(user.getId());
            if (optionalEmployee.isPresent()) {
                employee = optionalEmployee.get();
                userResponse.setIntelRxId(employee.getEmployeeIntelRxId());

                //get pharmacy the user belongs to

                JobInformationRequest jobInformationRequest = new JobInformationRequest();

                //check the user type if THE OWNER OR EMPLOYEE
                if (user.getUserType().equals("EMPLOYEE")) {
                    jobInformationRequest.setId(employee.getJobInformation().getJobTitle().getId());
                    jobInformationRequest.setJobTitle(employee.getJobInformation().getJobTitle().getName());
                }
            }
        }

        userResponse.setEmail(user.getEmail());
        userResponse.setUserType(user.getUserType());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setTwoFactorAuth(user.isTwoFactorAuth());
        String logo = null;
        if (user.getProfilePic() != null) {
            S3Service.FetchedImage fetchedImage = s3Service.fetchImage(user.getProfilePic()); // Replace "your_image_name.jpg" with the actual image name
            logo = fetchedImage.getImageUrl();
        }

        userResponse.setProfilePic(logo);

        return userResponse;

    }

    public JobInformationRequest getJobInfo() {
        User user = getAuthenticatedUser();
        Optional<Employee> optionalEmployee = employeeRepository.findByUserId(user.getId());

        if (optionalEmployee.isPresent()) {
            Employee employee = optionalEmployee.get();

            // Check if user type is EMPLOYEE
            if (user.getUserType() == UserType.EMPLOYEE) {
                JobInformation jobInformation = employee.getJobInformation();
                if (jobInformation != null) {
                    JobTitle jobTitle = jobInformation.getJobTitle();
                    if (jobTitle != null) {
                        JobInformationRequest jobInformationRequest = new JobInformationRequest();
                        jobInformationRequest.setId(jobTitle.getId());
                        jobInformationRequest.setJobTitle(jobTitle.getName());
                        return jobInformationRequest;
                    }
                }
            }
        }
        return null;
    }


    public UserRequest mapToUserInfo(User user) {
        UserRequest request = new UserRequest();
        // String dob = user.getDayOfBirth()+"-"+ user.getBirthMonth()+"-"+ user.getYearOfBirth();
        request.setId(user.getId());
        request.setFirstName(user.getFirstName());
        request.setLastName(user.getLastName());
        request.setGender(user.getGender());
        request.setEmail(user.getEmail());
        request.setPhoneNumber(user.getPhoneNumber());
        request.setDayOfBirth(user.getDayOfBirth());
        request.setBirthMonth(user.getBirthMonth());
        request.setYearOfBirth(user.getYearOfBirth());
        request.setUserStatus(user.getUserStatus());
        request.setTwoFactorAuth(user.isTwoFactorAuth());
        String logo = null;

        if (user.getProfilePic() != null && !user.getProfilePic().isEmpty()) {
            S3Service.FetchedImage fetchedImage = s3Service.fetchImage(user.getProfilePic());
            if (fetchedImage != null) {
                logo = fetchedImage.getImageUrl();
            }
        }


        request.setProfilePic(logo);

        return request;

    }


    public void updateUserStatus(User user, String userStatus) {
        if (user.getUserStatus() != null &&
                (!user.getUserStatus().equals("TERMINATED") &&
                        !user.getUserStatus().equals("DELETED") &&
                        !user.getUserStatus().equals("SUSPENDED") &&
                        !user.getUserStatus().equals("END CONTRACT"))) {
            System.out.println("Updating user status to: " + userStatus);
            user.setUserStatus(userStatus);
            userRepository.save(user);
            System.out.println("User status updated successfully.");
        } else {
            user.setUserStatus(userStatus);
            userRepository.save(user);
            System.out.println("User status not updated. Current status: " + user.getUserStatus());
        }
    }


}
