package com.pharmacy.intelrx.pharmacy.repositories.employee;

import com.pharmacy.intelrx.pharmacy.models.auxilliary.JobTitle;
import com.pharmacy.intelrx.pharmacy.models.employee.Employee;
import com.pharmacy.intelrx.pharmacy.models.employee.JobInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobInformationRepository extends JpaRepository<JobInformation,Long> {
    Optional<JobInformation>findByEmployeeId(Long employeeId);

    List<JobInformation> findAllByEmployeeId(Long employeeId);

    Optional<JobInformation> findByJobTitleAndEmployee(JobTitle jobTitle, Employee employee);

    List<JobInformation> findByJobTitle(JobTitle jobTitle);

    List<JobInformation> findAllByJobTitleAndEmployee(JobTitle jobTitle, Employee employee);
}
