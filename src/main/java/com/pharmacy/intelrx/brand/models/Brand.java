package com.pharmacy.intelrx.brand.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "brands")
public class Brand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToMany(mappedBy = "brand",cascade = CascadeType.ALL)
    private List<BrandClass> brandClass;

    @OneToMany(mappedBy = "brand",cascade = CascadeType.ALL)
    private List<BrandForm> brandForm;
}
