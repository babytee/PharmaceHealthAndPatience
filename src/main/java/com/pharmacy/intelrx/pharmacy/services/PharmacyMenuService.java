package com.pharmacy.intelrx.pharmacy.services;

import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.models.Expenditure;
import com.pharmacy.intelrx.pharmacy.models.Order;
import com.pharmacy.intelrx.pharmacy.repositories.ExpenditureRepository;
import com.pharmacy.intelrx.pharmacy.repositories.OrderRepository;
import com.pharmacy.intelrx.utility.UserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service("PharmacyMenuService")
public class PharmacyMenuService {
    private final ExpenditureRepository expenditureRepository;
    private final UserDetailsService userDetailsService;
    private final OrderRepository orderRepository;

    public ResponseEntity<?> expenseMenuCount() {
        var intelRxId = userDetailsService.getIntelRxId();
        var branch = userDetailsService.getBranch();
        List<Expenditure> expenditures =
                expenditureRepository.findAllByIntelRxIdAndBranchAndApproved
                        (intelRxId, branch, false);
        int expenseCount = 0;
        if (expenditures.isEmpty()) {
            return ResponseEntity.ok(StandardResponse.success(expenseCount));
        }
        expenseCount = expenditures.size();
        return ResponseEntity.ok(StandardResponse.success(expenseCount));
    }

    public ResponseEntity<?> queuedItemMenuCount() {
        var intelRxId = userDetailsService.getIntelRxId();
        var branch = userDetailsService.getBranch();
        List<Order> orders =
                orderRepository.findAllByIntelRxIdAndPharmacyBranchAndStatus
                        (intelRxId, branch, false);
        int queuedCount = 0;
        if (orders.isEmpty()) {
            return ResponseEntity.ok(StandardResponse.success(queuedCount));
        }
        queuedCount = orders.size();
        return ResponseEntity.ok(StandardResponse.success(queuedCount));
    }

}
