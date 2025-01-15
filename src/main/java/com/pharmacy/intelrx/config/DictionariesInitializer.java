package com.pharmacy.intelrx.config;

import com.pharmacy.intelrx.auxilliary.models.InventoryDictionary;
import com.pharmacy.intelrx.auxilliary.repositories.InventoryDictionaryRepository;
import com.pharmacy.intelrx.brand.models.Brand;
import com.pharmacy.intelrx.brand.models.BrandClass;
import com.pharmacy.intelrx.brand.models.BrandForm;
import com.pharmacy.intelrx.brand.models.Size;
import com.pharmacy.intelrx.brand.repositories.BrandClassRepository;
import com.pharmacy.intelrx.brand.repositories.BrandFormRepository;
import com.pharmacy.intelrx.brand.repositories.BrandRepository;
import com.pharmacy.intelrx.brand.repositories.SizeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class DictionariesInitializer implements ApplicationRunner {
    private final BrandRepository brandRepository;
    private final SizeRepository sizeRepository;
    private final BrandFormRepository brandFormRepository;
    private final BrandClassRepository brandClassRepository;
    private final InventoryDictionaryRepository inventoryDictionaryRepository;

    @Override
    public void run(ApplicationArguments args) {
        initializeTables();
    }

    private void initializeTables() {
        // Add brands as needed
        addBrandIfNotExists("Panadol");
        addBrandIfNotExists("Augmentin");
        addBrandIfNotExists("Lasix");
        addBrandIfNotExists("Amaryl");


        // Add sizes as needed
        addSizeIfNotExists("Unit");
        addSizeIfNotExists("Sachet");
        addSizeIfNotExists("Pack");

        //add form  as needed
        addFormIfNotExists("Tablet");
        addFormIfNotExists("Capsule");
        addFormIfNotExists("Caplet");
        addFormIfNotExists("Liquid (Suspension)");
        addFormIfNotExists("Syrup");
        addFormIfNotExists("Injection");
        addFormIfNotExists("Cream");
        addFormIfNotExists("Ointment");
        addFormIfNotExists("Gel");
        addFormIfNotExists("Powder");
        addFormIfNotExists("Suppository");
        addFormIfNotExists("Inhaler");
        addFormIfNotExists("Patch");
        addFormIfNotExists("Drops (Eye or Ear)");
        addFormIfNotExists("Spray");
        addFormIfNotExists("Lozenge (Troche)");
        addFormIfNotExists("Chewable Tablet");
        addFormIfNotExists("Sublingual Tablet");
        addFormIfNotExists("Nasal Spray");
        addFormIfNotExists("Solution");
        addFormIfNotExists("Lotion");
        addFormIfNotExists("Elixir");
        addFormIfNotExists("Patches (Transdermal)");
        addFormIfNotExists("Injectables");
        addFormIfNotExists("Powders");
        addFormIfNotExists("Granules");
        addFormIfNotExists("Aerosols");
        addFormIfNotExists("Mouthwash");
        addFormIfNotExists("Buccal Films/Tablets");
        addFormIfNotExists("Nebulizer");
        addFormIfNotExists("Implants");
        addFormIfNotExists("Douche");


        //add class  as needed
        addClassIfNotExists("Antibiotics");
        addClassIfNotExists("Antivirals");
        addClassIfNotExists("Antifungals");
        addClassIfNotExists("Antipyretics");
        addClassIfNotExists("Analgesics");
        addClassIfNotExists("Anti-inflammatories");
        addClassIfNotExists("Antihypertensives");
        addClassIfNotExists("Anticoagulants");
        addClassIfNotExists("Antiplatelet Agents");
        addClassIfNotExists("Antidepressants");
        addClassIfNotExists("Antipsychotics");
        addClassIfNotExists("Anxiolytics");
        addClassIfNotExists("Anticonvulsants");
        addClassIfNotExists("Bronchodilators");
        addClassIfNotExists("Antihistamines");
        addClassIfNotExists("Diuretics");
        addClassIfNotExists("Beta-Blockers");
        addClassIfNotExists("ACE Inhibitors");
        addClassIfNotExists("Statins");
        addClassIfNotExists("NSAIDs");
        addClassIfNotExists("Antiemetics");
        addClassIfNotExists("Laxatives");
        addClassIfNotExists("Antidiarrheals");
        addClassIfNotExists("Antitussives");
        addClassIfNotExists("Expectorants");
        addClassIfNotExists("Antiemetics");
        addClassIfNotExists("Mood Stabilizers");
        addClassIfNotExists("Hypnotics");
        addClassIfNotExists("Sedatives");
        addClassIfNotExists("Muscle Relaxants");
        addClassIfNotExists("Immunosuppressants");
        addClassIfNotExists("Hormone Replacement Therapy");
        addClassIfNotExists("Bone Modifying Agents");
        addClassIfNotExists("Antidiabetics");
        addClassIfNotExists("Antiemetics");
        addClassIfNotExists("Antineoplastics");
        addClassIfNotExists("Antivertigo Agents");
        addClassIfNotExists("Thrombolytics");
        addClassIfNotExists("Antiprotozoals");
        addClassIfNotExists("Antimigraine Agents");
        addClassIfNotExists("Stimulants");
        addClassIfNotExists("Corticosteroids");
        addClassIfNotExists("Hormones");
        addClassIfNotExists("Vaccines");
        addClassIfNotExists("Proton Pump Inhibitors");
        addClassIfNotExists("Calcium Channel Blockers");
        addClassIfNotExists("Antiparkinsonian Agents");
        addClassIfNotExists("Cholesterol Absorption Inhibitors");
        addClassIfNotExists("Thyroid Hormones");
        addClassIfNotExists("Bone Resorption Inhibitors");
        addClassIfNotExists("Antimetabolites");
        addClassIfNotExists("Narcotics (Opioids)");
        addClassIfNotExists("Antispasmodics");
        addClassIfNotExists("Topical Agents");
        addClassIfNotExists("Skeletal Muscle Relaxants");
        addClassIfNotExists("Digestive Enzymes");
        addClassIfNotExists("Hypoglycemics");
        addClassIfNotExists("Anticholinergics");
        addClassIfNotExists("Benzodiazepines");
        addClassIfNotExists("Antiseptics and Disinfectants");
        addClassIfNotExists("Anesthetics");
        addClassIfNotExists("Cough Suppressants");
        addClassIfNotExists("Antiparasitics");
        addClassIfNotExists("Antispasticity Agents");
        addClassIfNotExists("Bipolar Agents");
        addClassIfNotExists("Antimyasthenic Agents");
        addClassIfNotExists("Genitourinary Agents");
        addClassIfNotExists("Hormonal Agents (Adrenal)");
        addClassIfNotExists("Otic Agents");
        addClassIfNotExists("Ophthalmic Agents");
        addClassIfNotExists("Dermatological Agents");
        addClassIfNotExists("Gastrointestinal Agents");
        addClassIfNotExists("Immunosuppressives");
        addClassIfNotExists("Antacids");


        // Add medications to the addMedIfNotExists method
        addMedIfNotExists("Diltiazem");
        addMedIfNotExists("Ramipril 10mg");
        addMedIfNotExists("Escitalopram 5mg");
        addMedIfNotExists("Escitalopram 2.5mg");
        addMedIfNotExists("Amoxicillin Clavulanate 625");
        addMedIfNotExists("Amoxicillin Clavulanate 1g");
        addMedIfNotExists("Azithromycin");
        addMedIfNotExists("Paracetamol 500mg");
        addMedIfNotExists("Ciprofloxacin 500mg");
        addMedIfNotExists("Lorazepam 2.5mg");
        addMedIfNotExists("Lorazepam 5mg");
        addMedIfNotExists("Propranolol");
        addMedIfNotExists("Entecavir");
        addMedIfNotExists("Propranolol");
        addMedIfNotExists("Magnessium 200mg");
        addMedIfNotExists("Hydromorphone 1mg");
        addMedIfNotExists("Hydromorphone 2mg");
        addMedIfNotExists("Gabapentin 100mg");


    }

    public Brand addBrandIfNotExists(String brandName) {
        Brand brand = brandRepository.findByName(brandName).orElse(null);

        if (brand == null) {
            brand = new Brand();
            brand.setName(brandName);
            brandRepository.save(brand);
        } else {
            brand.setName(brandName);
            brandRepository.save(brand);
        }
        return brand;
    }

    public Size addSizeIfNotExists(String sizeName) {
        Size size = sizeRepository.findByName(sizeName).orElse(null);
        if (size == null) {
            size = new Size();
            size.setName(sizeName);
            sizeRepository.save(size);
        } else {
            size.setName(sizeName);
            sizeRepository.save(size);

        }
        return size;
    }

    public BrandForm addFormIfNotExists(String brandFormName) {
        BrandForm brandForm = brandFormRepository.findByName(brandFormName).orElse(null);
        if (brandForm == null) {
            brandForm = new BrandForm();
            brandForm.setName(brandFormName);
            brandFormRepository.save(brandForm);
        } else {
            brandForm.setName(brandFormName);
            brandFormRepository.save(brandForm);
        }
        return brandForm;
    }

    public BrandClass addClassIfNotExists(String brandClassName) {
        BrandClass brandClass = brandClassRepository.findByName(brandClassName).orElse(null);
        if (brandClass == null) {
            brandClass = new BrandClass();
            brandClass.setName(brandClassName);
            brandClassRepository.save(brandClass);
        } else {
            brandClass.setName(brandClassName);
            brandClassRepository.save(brandClass);
        }
        return brandClass;
    }

    public void addMedIfNotExists(String medGenericName) {
        Optional<InventoryDictionary> optional = inventoryDictionaryRepository.findByItemName(medGenericName);
        if (optional.isEmpty()) {
            InventoryDictionary inventoryDictionary = new InventoryDictionary();
            inventoryDictionary.setItemName(medGenericName);
            inventoryDictionary.setInventoryType("MEDICATION");
            inventoryDictionaryRepository.save(inventoryDictionary);
        } else {
            InventoryDictionary inventoryDictionary = optional.get();
            inventoryDictionary.setItemName(medGenericName);
            inventoryDictionary.setInventoryType("MEDICATION");
            inventoryDictionaryRepository.save(inventoryDictionary);
        }
    }

}
