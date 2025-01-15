package com.pharmacy.intelrx.pharmacy.models.employee;

import com.pharmacy.intelrx.pharmacy.models.auxilliary.DocumentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "employee_documents")
public class EmployeeDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "employee_id", referencedColumnName = "id")
    private Employee employee;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "document_type_id", referencedColumnName = "id")
    private DocumentType documentType;

    @Column(name = "file_doc", columnDefinition = "TEXT")
    private String fileDoc;
}
