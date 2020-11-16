package org.egov.ukdcustomservice.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.egov.tracer.model.CustomException;
import org.egov.ukdcustomservice.web.models.Demand;
import org.egov.ukdcustomservice.web.models.DemandDetail;
import org.egov.ukdcustomservice.web.models.DemandRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DemandValidationService {

    public ResponseEntity<?> validate(DemandRequest demandRequest) {

        List<Demand> demands =demandRequest.getDemands();
        log.info("the demand request object : " + demands);
        Map<String, String> errors = new HashMap<>();

        List<Integer> demandYears = demands.stream().map(
                d -> Instant.ofEpochMilli(d.getTaxPeriodFrom()).atZone(ZoneId.systemDefault()).toLocalDate().getYear())
                .collect(Collectors.toList());
        int minYear = Collections.min(demandYears);
        int currentFinancialYear = LocalDate.now().getMonthValue() < 4 ? LocalDate.now().getYear() - 1
                : LocalDate.now().getYear();

        for (int i = currentFinancialYear; i >= minYear; i--) {
            if (!demandYears.contains(i)) {
                errors.put(i + "-" + (i + 1), "MissingDemandForYear");
            }
        }

        for (Demand demand : demands) {
            Set<String> taxHeads = new HashSet<>();
            for (DemandDetail demandDetail : demand.getDemandDetails()) {
                if (!taxHeads.add(demandDetail.getTaxHeadMasterCode())) {
                    errors.put(demand.getId(), "DuplicateDemandDetails");
                }
            }
        }

        if (errors.size() > 0) {
            CustomException e = new CustomException(errors);
            e.setMessage(errors.toString());
            return new ResponseEntity<>(e , HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>(demandRequest, HttpStatus.OK);
        }

    }

}