package com.pharmacy.intelrx.PCNAPICrawling;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/api/v1/intelrx/pcn")
@RestController("PharmacyPremisesController")
@RequiredArgsConstructor
public class PharmacyPremisesController {
    private final PharmacyPremisesServices premisesServices;

    //premises-details
    @GetMapping("premises-details")
    public ResponseEntity<?> premiseDetails(@RequestParam(required = true, name = "premisesFilter")
                                            String premisesFilter) {
        return premisesServices.getPremiseDetails(premisesFilter);
    }

    @GetMapping("single-premises-details")
    public ResponseEntity<?> getSinglePremiseDetails(@RequestParam(required = true, name = "PremisesId")
                                            String PremisesId) {
        return premisesServices.getSinglePremiseDetails(PremisesId);
    }

    @PostMapping("submitPharmacyPremise")
    public ResponseEntity<?> submitPharmacyPremise(@RequestBody PharmacyPremiseRequest premiseRequest)
            throws Exception {
//        return ResponseEntity.ok(StandardResponse.success(premiseRequest));
        return premisesServices.submitPharmacyPremise(premiseRequest);
    }

    @PostMapping("submitPharmacyPremiseManually")
    public ResponseEntity<?> submitPharmacyPremiseManually(@RequestBody PharmacyPremiseRequest premiseRequest)
            throws Exception {
        return premisesServices.submitPharmacyPremiseManually(premiseRequest);
    }

}
