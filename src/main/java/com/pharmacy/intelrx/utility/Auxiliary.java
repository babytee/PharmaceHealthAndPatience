package com.pharmacy.intelrx.utility;

import com.pharmacy.intelrx.auxilliary.dto.CustomSingleResponse;
import com.pharmacy.intelrx.auxilliary.models.PaymentMethod;
import com.pharmacy.intelrx.auxilliary.repositories.InventoryDictionaryRepository;
import com.pharmacy.intelrx.auxilliary.repositories.KPITypeRepository;
import com.pharmacy.intelrx.brand.repositories.BrandClassRepository;
import com.pharmacy.intelrx.brand.repositories.BrandFormRepository;
import com.pharmacy.intelrx.brand.repositories.BrandRepository;
import com.pharmacy.intelrx.brand.repositories.SizeRepository;
import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.inventory.InventoryResponse;
import com.pharmacy.intelrx.pharmacy.models.Inventory;
import com.pharmacy.intelrx.pharmacy.models.auxilliary.*;
import com.pharmacy.intelrx.pharmacy.repositories.InventoryRepository;
import com.pharmacy.intelrx.pharmacy.repositories.MedTestTypeRepository;
import com.pharmacy.intelrx.pharmacy.repositories.NotificationTypeRepository;
import com.pharmacy.intelrx.pharmacy.repositories.SupportTypeRepository;
import com.pharmacy.intelrx.pharmacy.repositories.auxilliary.*;
import com.pharmacy.intelrx.auxilliary.repositories.PaymentMethodRepository;
import com.pharmacy.intelrx.auxilliary.repositories.PaymentStatusRepository;
import com.pharmacy.intelrx.pharmacy.utility.FilterInventory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class Auxiliary {

    private final DepartmentRepository departmentRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final SeniorityLevelRepository seniorityLevelRepository;
    private final JobTitleRepository jobTitleRepository;
    private final PaymentFrequencyRepository paymentFrequencyRepository;
    private final SalaryTypeRepository salaryTypeRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentStatusRepository paymentStatusRepository;
    private final BrandRepository brandRepository;
    private final SizeRepository sizeRepository;
    private final BrandFormRepository brandFormRepository;
    private final BrandClassRepository brandClassRepository;
    private final RefillDurationRepository refillDurationRepository;
    private final KPITypeRepository kpiTypeRepository;
    private final NotificationTypeRepository notificationTypeRepository;
    private final InventoryRepository inventoryRepository;
    private final FilterInventory filterInventory;
    private final SupportTypeRepository supportTypeRepository;
    private final SecurityQuestionRepository securityQuestionRepository;
    private final InventoryDictionaryRepository dictionaryRepository;
    private final MedTestTypeRepository medTestTypeRepository;

    public Department getDepartment(Long id) {
        Optional<Department> optional = departmentRepository.findById(id);
        Department department = null;
        if (optional.isPresent()) {
            department = optional.get();
        }
        return department;

    }

    public ResponseEntity<?> allDepartments() {
        //return departmentRepository.findAll();
        CustomSingleResponse<Objects> response = new CustomSingleResponse<>();

        response.setStatus("success");
        response.setData(departmentRepository.findAll());
        return ResponseEntity.ok(response);
    }

    public WorkSchedule getWorkSchedule(Long id) {
        Optional<WorkSchedule> optional = workScheduleRepository.findById(id);
        WorkSchedule workSchedule = null;
        if (optional.isPresent()) {
            workSchedule = optional.get();
        }
        return workSchedule;
    }

    public ResponseEntity<?> allWorkSchedule() {
        //return departmentRepository.findAll();
        CustomSingleResponse<Objects> response = new CustomSingleResponse<>();

        response.setStatus("success");
        response.setData(workScheduleRepository.findAll());
        return ResponseEntity.ok(response);
    }

    public SeniorityLevel getSeniorityLevel(Long id) {
        Optional<SeniorityLevel> optional = seniorityLevelRepository.findById(id);
        SeniorityLevel seniorityLevel = null;
        if (optional.isPresent()) {
            seniorityLevel = optional.get();
        }
        return seniorityLevel;
    }

    public ResponseEntity<?> allSeniorityLevel() {
        //return departmentRepository.findAll();
        CustomSingleResponse<Objects> response = new CustomSingleResponse<>();

        response.setStatus("success");
        response.setData(seniorityLevelRepository.findAll());
        return ResponseEntity.ok(response);
    }

    public JobTitle getJobTitle(Long id) {
        Optional<JobTitle> optional = jobTitleRepository.findById(id);
        JobTitle jobTitle = null;
        if (optional.isPresent()) {
            jobTitle = optional.get();
        }
        return jobTitle;
    }

    public ResponseEntity<?> allJobTitle() {
        CustomSingleResponse<Objects> response = new CustomSingleResponse<>();

        response.setStatus("success");
        response.setData(jobTitleRepository.findAll());
        return ResponseEntity.ok(response);
    }

    public PaymentFrequency getPaymentFrequency(Long id) {
        Optional<PaymentFrequency> optional = paymentFrequencyRepository.findById(id);
        PaymentFrequency paymentFrequency = null;
        if (optional.isPresent()) {
            paymentFrequency = optional.get();
        }
        return paymentFrequency;
    }

    public ResponseEntity<?> allPaymentFrequency() {
        //return departmentRepository.findAll();
        CustomSingleResponse<Objects> response = new CustomSingleResponse<>();

        response.setStatus("success");
        response.setData(paymentFrequencyRepository.findAll());
        return ResponseEntity.ok(response);
    }

    public SalaryType getSalaryType(Long id) {
        Optional<SalaryType> optional = salaryTypeRepository.findById(id);
        SalaryType salaryType = null;
        if (optional.isPresent()) {
            salaryType = optional.get();
        }
        return salaryType;
    }

    public ResponseEntity<?> allSalaryType() {
        //return departmentRepository.findAll();
        CustomSingleResponse<Objects> response = new CustomSingleResponse<>();

        response.setStatus("success");
        response.setData(salaryTypeRepository.findAll());
        return ResponseEntity.ok(response);
    }

    public DocumentType getDocumentType(Long id) {
        Optional<DocumentType> optional = documentTypeRepository.findById(id);
        DocumentType documentType = null;
        if (optional.isPresent()) {
            documentType = optional.get();
        }
        return documentType;
    }

    public PaymentMethod getPaymentMethod(Long id) {
        Optional<PaymentMethod> optional = paymentMethodRepository.findById(id);
        PaymentMethod paymentMethod = null;
        if (optional.isPresent()) {
            paymentMethod = optional.get();
        }
        return paymentMethod;
    }

    public ResponseEntity<?> allDocumentType() {
        //return departmentRepository.findAll();
        CustomSingleResponse<Objects> response = new CustomSingleResponse<>();

        response.setStatus("success");
        response.setData(documentTypeRepository.findAll());
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> allPaymentStatus() {
        CustomSingleResponse<Objects> response = new CustomSingleResponse<>();

        response.setStatus("success");
        response.setData(paymentStatusRepository.findAll());
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> allPaymentMethod() {
        CustomSingleResponse<Objects> response = new CustomSingleResponse<>();

        response.setStatus("success");
        response.setData(paymentMethodRepository.findAll());
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> allBrands() {
        CustomSingleResponse<Objects> response = new CustomSingleResponse<>();

        response.setStatus("success");
        response.setData(brandRepository.findAll());
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> allBrandClass() {
        CustomSingleResponse<Objects> response = new CustomSingleResponse<>();
        response.setStatus("success");
        response.setData(brandClassRepository.findAll());
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> allBrandForm() {
        CustomSingleResponse<Objects> response = new CustomSingleResponse<>();

        response.setStatus("success");
        response.setData(brandFormRepository.findAll());
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> allSizes() {
        CustomSingleResponse<Objects> response = new CustomSingleResponse<>();

        response.setStatus("success");
        response.setData(sizeRepository.findAll());
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> allRefillDurations() {
        CustomSingleResponse<Objects> response = new CustomSingleResponse<>();

        response.setStatus("success");
        response.setData(refillDurationRepository.findAll());
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> allKPIs() {
        CustomSingleResponse<Objects> response = new CustomSingleResponse<>();

        response.setStatus("success");
        response.setData(kpiTypeRepository.findAll());
        return ResponseEntity.ok(response);
    }


    public ResponseEntity<?> notificationTypes() {
        CustomSingleResponse<Objects> response = new CustomSingleResponse<>();

        response.setStatus("success");
        response.setData(notificationTypeRepository.findAll());
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> supportTypes() {
        CustomSingleResponse<Objects> response = new CustomSingleResponse<>();

        response.setStatus("success");
        response.setData(supportTypeRepository.findAll());
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> medTestTypes() {
        CustomSingleResponse<Objects> response = new CustomSingleResponse<>();

        response.setStatus("success");
        response.setData(medTestTypeRepository.findAll());
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> securityQuestions() {
        CustomSingleResponse<Objects> response = new CustomSingleResponse<>();

        response.setStatus("success");
        response.setData(securityQuestionRepository.findAll());
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> fetchMedInventories() {
        CustomSingleResponse<Objects> response = new CustomSingleResponse<>();

        response.setStatus("success");
        response.setData(inventoryRepository.findAllUniqueInventoryNamesByInventoryType("MEDICATION"));
        return ResponseEntity.ok(response);
    }

//    public ResponseEntity<?> fetchInventories() {
//        CustomSingleResponse<Objects> response = new CustomSingleResponse<>();
//
//        response.setStatus("success");
//        response.setData(inventoryRepository.findAllUniqueInventoryNames());
//        return ResponseEntity.ok(response);
//    }


    public ResponseEntity<?> fetchInventories(String inventoryType) {
        CustomSingleResponse<Objects> response = new CustomSingleResponse<>();
        response.setStatus("success");

        if (inventoryType == null) {
            response.setData(dictionaryRepository.findAll());
        }else {
            response.setData(dictionaryRepository.findAllByInventoryType(inventoryType));
        }
        return ResponseEntity.ok(response);
    }


    public ResponseEntity<?> fetchSingleInventory(Long inventoryId) {

        if (inventoryId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("inventoryId is required"));
        }

        Optional<Inventory> optional = inventoryRepository.findById(inventoryId);
        Inventory inventory = optional.get();
        InventoryResponse responseList = filterInventory.mapToAddedInventoryResponse(inventory);

        return ResponseEntity.ok(StandardResponse.success(responseList));
    }


}
