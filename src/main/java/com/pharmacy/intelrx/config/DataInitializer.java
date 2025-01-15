package com.pharmacy.intelrx.config;

import com.pharmacy.intelrx.admin.models.Admin;
import com.pharmacy.intelrx.admin.repositories.AdminRepository;
import com.pharmacy.intelrx.auxilliary.models.PaymentMethod;
import com.pharmacy.intelrx.auxilliary.models.PaymentStatus;
import com.pharmacy.intelrx.auxilliary.models.Role;
import com.pharmacy.intelrx.auxilliary.repositories.KPITypeRepository;
import com.pharmacy.intelrx.auxilliary.repositories.PaymentMethodRepository;
import com.pharmacy.intelrx.auxilliary.repositories.PaymentStatusRepository;
import com.pharmacy.intelrx.pharmacy.models.auxilliary.*;
import com.pharmacy.intelrx.pharmacy.models.notification.NotificationType;
import com.pharmacy.intelrx.pharmacy.models.support.SupportType;
import com.pharmacy.intelrx.pharmacy.repositories.MedTestTypeRepository;
import com.pharmacy.intelrx.pharmacy.repositories.NotificationTypeRepository;
import com.pharmacy.intelrx.pharmacy.repositories.SupportTypeRepository;
import com.pharmacy.intelrx.pharmacy.repositories.auxilliary.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class DataInitializer implements ApplicationRunner {
    private final DepartmentRepository departmentRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final SeniorityLevelRepository seniorityLevelRepository;
    private final JobTitleRepository jobTitleRepository;
    private final PaymentFrequencyRepository paymentFrequencyRepository;
    private final SalaryTypeRepository salaryTypeRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final PaymentStatusRepository paymentStatusRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final RefillDurationRepository refillDurationRepository;
    private final KPITypeRepository kpiTypeRepository;
    private final NotificationTypeRepository notificationTypeRepository;
    private final SupportTypeRepository supportTypeRepository;
    private final SecurityQuestionRepository securityQuestionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminRepository adminRepository;
    private final MedTestTypeRepository medTestTypeRepository;


    @Value("${spring.intelrx.admin.email}")
    private String adminEmail;

    @Value("${spring.intelrx.admin.password}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        initializeTables();
    }

    private void initializeTables() {

        // Add departments as needed
        addDepartmentIfNotExists("HR");
        addDepartmentIfNotExists("Sales");
        addDepartmentIfNotExists("Management");
        addDepartmentIfNotExists("Medical");

        // Add workSchedule as needed
        addWorkScheduleIfNotExists("7 days a week");
        addWorkScheduleIfNotExists("6 days a week");
        addWorkScheduleIfNotExists("5 days a week");
        addWorkScheduleIfNotExists("3 days a week");
        addWorkScheduleIfNotExists("2 days a week");
        addWorkScheduleIfNotExists("Once a week");

        // Add SeniorityLevel as needed
        addSeniorityLevelIfNotExists("Not Applicable");
        addSeniorityLevelIfNotExists("Junior");
        addSeniorityLevelIfNotExists("Mid");
        addSeniorityLevelIfNotExists("Senior");
        addSeniorityLevelIfNotExists("Lead");
        addSeniorityLevelIfNotExists("Director");

        // Add JobTitle as needed
        addJobTitleIfNotExists(
                "Pharmacist",
                "View and manage patient information. Has all sales personnel and cashier functions");

        addJobTitleIfNotExists("Cashier",
                "View customer/patient invoice. Receive payments from customers/patients");

        addJobTitleIfNotExists("Sales Person",
                "Handles sales to customers/patients. Create customer profile, " +
                        "Add inventory to customer account. Disburse sold inventory to customers/patients, " +
                        "send inventory alerts for low stock, send payment invoice to cashier. " +
                        "Can carry out cashier function");

        addJobTitleIfNotExists("Cleaner",
                "");

        addJobTitleIfNotExists("Manager",
                "Manages affairs of the assigned branch. Can view the data of the employees of that branch, " +
                        "pay,suspend and terminate employees in said branch. " +
                        "Send inventory request to headquarters");

        addJobTitleIfNotExists("Administrator",
                "Has access to the entire app. Can make changes to company profile, " +
                        "can edit employee app access,suspend and terminate employees, " +
                        "Edit inventory, view sales activities, Patients data etc.");

        addJobTitleIfNotExists("Hr Manager",
                "Manages employees affairs. View salaries, send salary delay alerts and add new employees");


        // Add PaymentFrequency as needed
        addPaymentFrequencyIfNotExists("Daily");
        addPaymentFrequencyIfNotExists("Weekly");
        addPaymentFrequencyIfNotExists("Bi-Weekly");
        addPaymentFrequencyIfNotExists("Monthly");
        addPaymentFrequencyIfNotExists("End of contract");

        // Add SalaryType as needed
        addSalaryTypeIfNotExists("Monthly");
        addSalaryTypeIfNotExists("Hourly");

        // Add DocumentType as needed
        addDocumentTypeIfNotExists("National ID");
        addDocumentTypeIfNotExists("Drivers License");
        addDocumentTypeIfNotExists("Voters Card");
        addDocumentTypeIfNotExists("NIN");

        // Add PaymentStatus as needed
        addPaymentStatusIfNotExists("Fully Paid");
        addPaymentStatusIfNotExists("Part Payment");
        addPaymentStatusIfNotExists("Purchased On Credit");

        // Add PaymentMethod as needed
        addPaymentMethodIfNotExists("Cash");
        addPaymentMethodIfNotExists("Bank Transfer");
        addPaymentMethodIfNotExists("Purchased On Credit");

        //Add RefillDuration as needed
        //minutes
        addRefillDurationIfNotExists("2 Minutes");
//        addRefillDurationIfNotExists("5 Minutes");
//        addRefillDurationIfNotExists("10 Minutes");
        //days
        addRefillDurationIfNotExists("5 Days");
        addRefillDurationIfNotExists("6 Days");
        //weeks
        addRefillDurationIfNotExists("1 Week");
        addRefillDurationIfNotExists("2 Weeks");
        addRefillDurationIfNotExists("3 Weeks");
        addRefillDurationIfNotExists("4 Weeks");
        addRefillDurationIfNotExists("5 Weeks");
        //months
        addRefillDurationIfNotExists("1 Month");
        addRefillDurationIfNotExists("2 Months");
        addRefillDurationIfNotExists("3 Months");
        addRefillDurationIfNotExists("4 Months");
        addRefillDurationIfNotExists("5 Months");
        addRefillDurationIfNotExists("6 Months");
        addRefillDurationIfNotExists("7 Months");
        addRefillDurationIfNotExists("8 Months");
        addRefillDurationIfNotExists("9 Months");
        addRefillDurationIfNotExists("10 Months");
        addRefillDurationIfNotExists("11 Months");
        addRefillDurationIfNotExists("12 Months");


        addKPITypeIfNotExists("Sales Data Evaluation");
        addKPITypeIfNotExists("Customer Evaluation");
        addKPITypeIfNotExists("Merchandising");
        addKPITypeIfNotExists("Retail financial KPI");


        //Notification Type
        addNotificationTypeIfNotExists("Activities");
        addNotificationTypeIfNotExists("TransferRequest");
        addNotificationTypeIfNotExists("IntelRxNews");
        addNotificationTypeIfNotExists("SystemManagers");

        //Support Type
        addSupportTypeIfNotExists("Sales");
        addSupportTypeIfNotExists("Cashier");
        addSupportTypeIfNotExists("Inventory");
        addSupportTypeIfNotExists("Organization");
        addSupportTypeIfNotExists("Patients");
        addSupportTypeIfNotExists("Suggestion");
        addSupportTypeIfNotExists("Others");

        //Security Questions
        addSecurityQuestionIfNotExists("What is your mother's maiden name?");
        addSecurityQuestionIfNotExists("What was the name of your first pet?");
        addSecurityQuestionIfNotExists("What was the name of your elementary school?");
        addSecurityQuestionIfNotExists("In what city were you born?");
        addSecurityQuestionIfNotExists("What is your favorite food?");
        addSecurityQuestionIfNotExists("What is the name of your best friend from childhood?");
        addSecurityQuestionIfNotExists("What was your first car's make and model?");
        addSecurityQuestionIfNotExists("What is the name of the street you grew up on?");
        addSecurityQuestionIfNotExists("What is your favorite book?");
        addSecurityQuestionIfNotExists("What is your father's middle name?");

        addMedTestTypeIfNotExists("Blood Pressure Monitoring","mmHg");
        addMedTestTypeIfNotExists("Malaria Test","cells/µL");
        addMedTestTypeIfNotExists("Typhoid Test","IU/mL");
        addMedTestTypeIfNotExists("Blood Glucose Testing","mg/dL");
        addMedTestTypeIfNotExists("Hemoglobin A1C (HbA1c) Testing","%");
        addMedTestTypeIfNotExists("Cholesterol and Lipid Profile","mg/dL");


        //Adding of new admin
        addAdmin();

    }

    private void addDepartmentIfNotExists(String departmentName) {
        Optional<Department> existingDepartment = departmentRepository.findByName(departmentName);

        if (existingDepartment.isEmpty()) {
            Department department = new Department();
            department.setName(departmentName);
            departmentRepository.save(department);
        }
    }

    private void addWorkScheduleIfNotExists(String workScheduleName) {
        Optional<WorkSchedule> existingWorkSchedule = workScheduleRepository.findByName(workScheduleName);

        if (existingWorkSchedule.isEmpty()) {
            WorkSchedule workSchedule = new WorkSchedule();
            workSchedule.setName(workScheduleName);
            workScheduleRepository.save(workSchedule);
        }
    }

    private void addSeniorityLevelIfNotExists(String SeniorityLevelName) {
        Optional<SeniorityLevel> existingWorkSchedule = seniorityLevelRepository.findByName(SeniorityLevelName);

        if (existingWorkSchedule.isEmpty()) {
            SeniorityLevel seniorityLevel = new SeniorityLevel();
            seniorityLevel.setName(SeniorityLevelName);
            seniorityLevelRepository.save(seniorityLevel);
        }
    }

    private void addJobTitleIfNotExists(String JobTitleName, String desc) {
        Optional<JobTitle> existingJobTitle = jobTitleRepository.findByName(JobTitleName);

        if (existingJobTitle.isEmpty()) {
            JobTitle jobTitle = new JobTitle();
            jobTitle.setName(JobTitleName);
            jobTitle.setDescription(desc);
            jobTitleRepository.save(jobTitle);
        }


    }

    private void addPaymentFrequencyIfNotExists(String PaymentFrequencyName) {
        Optional<PaymentFrequency> existingPaymentFrequency = paymentFrequencyRepository.findByName(PaymentFrequencyName);

        if (existingPaymentFrequency.isEmpty()) {
            PaymentFrequency paymentFrequency = new PaymentFrequency();
            paymentFrequency.setName(PaymentFrequencyName);
            paymentFrequencyRepository.save(paymentFrequency);
        }
    }

    private void addSalaryTypeIfNotExists(String SalaryTypeName) {
        Optional<SalaryType> existingSalaryType = salaryTypeRepository.findByName(SalaryTypeName);

        if (existingSalaryType.isEmpty()) {
            SalaryType salaryType = new SalaryType();
            salaryType.setName(SalaryTypeName);
            salaryTypeRepository.save(salaryType);
        }
    }

    private void addDocumentTypeIfNotExists(String DocumentTypeName) {
        Optional<DocumentType> existingSalaryType = documentTypeRepository.findByName(DocumentTypeName);

        if (existingSalaryType.isEmpty()) {
            DocumentType documentType = new DocumentType();
            documentType.setName(DocumentTypeName);
            documentTypeRepository.save(documentType);
        }
    }

    private void addPaymentStatusIfNotExists(String paymentStatusName) {
        Optional<PaymentStatus> optional = paymentStatusRepository.findByName(paymentStatusName);

        if (optional.isEmpty()) {
            PaymentStatus paymentStatus = new PaymentStatus();
            paymentStatus.setName(paymentStatusName);
            paymentStatusRepository.save(paymentStatus);
        }
    }

    private void addPaymentMethodIfNotExists(String labelName) {
        Optional<PaymentMethod> optional = paymentMethodRepository.findByName(labelName);

        if (optional.isEmpty()) {
            PaymentMethod paymentMethod = new PaymentMethod();
            paymentMethod.setName(labelName);
            paymentMethodRepository.save(paymentMethod);
        }
    }

    private void addRefillDurationIfNotExists(String labelName) {
        Optional<RefillDuration> optional = refillDurationRepository.findByName(labelName);

        if (optional.isEmpty()) {
            RefillDuration refillDuration = new RefillDuration();
            refillDuration.setName(labelName);
            refillDurationRepository.save(refillDuration);
        }
    }

    private void addKPITypeIfNotExists(String labelName) {
        Optional<KPIType> optional = kpiTypeRepository.findByRecommendationName(labelName);

        if (optional.isEmpty()) {
            KPIType kpiType = new KPIType();
            kpiType.setRecommendationName(labelName);
            kpiTypeRepository.save(kpiType);
        }
    }


    private void addNotificationTypeIfNotExists(String labelName) {
        Optional<NotificationType> optional = notificationTypeRepository.findByName(labelName);

        if (optional.isEmpty()) {
            NotificationType notificationType = new NotificationType();
            notificationType.setName(labelName);
            notificationTypeRepository.save(notificationType);
        }
    }

    private void addSupportTypeIfNotExists(String labelName) {
        Optional<SupportType> optional = supportTypeRepository.findByName(labelName);

        if (optional.isEmpty()) {
            SupportType supportType = new SupportType();
            supportType.setName(labelName);
            supportTypeRepository.save(supportType);
        }
    }

    private void addMedTestTypeIfNotExists(String labelName,String unit) {
        Optional<MedTestType> optional = medTestTypeRepository.findByTestTypeName(labelName);

        if (optional.isEmpty()) {
            MedTestType medTestType = new MedTestType();
            medTestType.setTestTypeName(labelName);
            medTestType.setTestUnit(unit);
            medTestTypeRepository.save(medTestType);
        }
    }

    private void addSecurityQuestionIfNotExists(String labelQuestion) {
        Optional<SecurityQuestion> optional = securityQuestionRepository.findByQuestion(labelQuestion);
        if (optional.isEmpty()) {
            SecurityQuestion securityQuestion = new SecurityQuestion();
            securityQuestion.setQuestion(labelQuestion);
            securityQuestionRepository.save(securityQuestion);
        }
    }

    private void addAdmin() {
        Optional<Admin> optional = adminRepository.findByEmail(adminEmail);
        if (optional.isEmpty()) {
            Admin admin = Admin.builder()
                    .firstname("Sola")
                    .lastname("Awolusi")
                    .email(adminEmail)
                    .password(this.passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now()).build();
            adminRepository.save(admin);
        }
    }

}
