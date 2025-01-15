package com.pharmacy.intelrx.pharmacy.controllers;

import com.pharmacy.intelrx.pharmacy.dto.inventory.TransferInventoryRequest;
import com.pharmacy.intelrx.pharmacy.services.TransferInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping({"/api/v1/user/pharmacy"})
@RestController("TransferInventoryController")
public class TransferInventoryController {
    private final TransferInventoryService transferInventoryService;

    @GetMapping({"cancel_transfer/{transferId}"})
    public ResponseEntity<?>cancelTransfer(@PathVariable Long transferId){
        return transferInventoryService.cancelTransfer(transferId);
    }

    @GetMapping({"confirm_transfer/{transferId}"})
    public ResponseEntity<?>confirmTransfer(@PathVariable Long transferId){
        return transferInventoryService.confirmTransfer(transferId);
    }

    @PostMapping({"inventory_transfer"})
    public ResponseEntity<?>transfer(@RequestBody TransferInventoryRequest request){
        return transferInventoryService.Transfer(request);
    }


    @GetMapping({"get_transferred_inventory"})
    public ResponseEntity<?>getTransferInventories(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "50") int pageSize){
        // Adjust the page number if it is 1
        int adjustedPage = (page <= 1) ? 0 : page - 1;
        // Pass the pagination parameters to the service method
        Pageable pageable = PageRequest.of(adjustedPage, pageSize);
        return transferInventoryService.getTransferInventories(pageable);
    }

}
