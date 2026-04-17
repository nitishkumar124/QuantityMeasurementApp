package com.app.quantitymeasurement.service;

import com.app.quantitymeasurement.Quantity;
import com.app.quantitymeasurement.entity.QuantityDTO;
import com.app.quantitymeasurement.entity.QuantityMeasurementEntity;
import com.app.quantitymeasurement.repository.IQuantityMeasurementRepository;
import com.app.quantitymeasurement.unit.IMeasurable;
import com.app.quantitymeasurement.unit.UnitRegistry;
import com.app.quantitymeasurement.user.User;
import com.app.quantitymeasurement.user.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class QuantityMeasurementServiceImpl implements IQuantityMeasurementService {

    private final IQuantityMeasurementRepository repository;
    private final UserRepository userRepository;
    private final UnitRegistry unitRegistry; // Injected dependency

    public QuantityMeasurementServiceImpl(IQuantityMeasurementRepository repository,
                                         UserRepository userRepository,
                                         UnitRegistry unitRegistry) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.unitRegistry = unitRegistry;
    }

    /**
     * Generalized conversion from DTO to the domain Quantity model.
     * Works for any IMeasurable unit.
     */
    private Quantity<IMeasurable> toQuantity(QuantityDTO dto) {
        return new Quantity<>(
                dto.getValue(),
                unitRegistry.getUnit(dto.getUnit())
        );
    }

    @Override
    public boolean compare(QuantityDTO q1, QuantityDTO q2) {
        Quantity<IMeasurable> quantity1 = toQuantity(q1);
        Quantity<IMeasurable> quantity2 = toQuantity(q2);

        boolean result = quantity1.equals(quantity2);
        saveAudit("COMPARE", q1.toString(), q2.toString(), String.valueOf(result));
        return result;
    }

    @Override
    public QuantityDTO convert(QuantityDTO q, String targetUnitName) {
        Quantity<IMeasurable> quantity = toQuantity(q);
        IMeasurable targetUnit = unitRegistry.getUnit(targetUnitName);

        Quantity<IMeasurable> result = quantity.convertTo(targetUnit);

        saveAudit("CONVERT", q.toString(), targetUnitName, result.toString());
        return new QuantityDTO(result.getValue(), result.getUnit().getUnitName());
    }

    @Override
    public QuantityDTO add(QuantityDTO q1, QuantityDTO q2) {
        Quantity<IMeasurable> quantity1 = toQuantity(q1);
        Quantity<IMeasurable> quantity2 = toQuantity(q2);

        Quantity<IMeasurable> result = quantity1.add(quantity2);

        saveAudit("ADD", q1.toString(), q2.toString(), result.toString());
        return new QuantityDTO(result.getValue(), result.getUnit().getUnitName());
    }

    @Override
    public QuantityDTO subtract(QuantityDTO q1, QuantityDTO q2) {
        Quantity<IMeasurable> quantity1 = toQuantity(q1);
        Quantity<IMeasurable> quantity2 = toQuantity(q2);

        Quantity<IMeasurable> result = quantity1.subtract(quantity2);

        saveAudit("SUBTRACT", q1.toString(), q2.toString(), result.toString());
        return new QuantityDTO(result.getValue(), result.getUnit().getUnitName());
    }

//    @Override
//    public double divide(QuantityDTO q1, QuantityDTO q2) {
//        Quantity<IMeasurable> quantity1 = toQuantity(q1);
//        Quantity<IMeasurable> quantity2 = toQuantity(q2);
//
//        double result = quantity1.divide(quantity2);
//
//        saveAudit("DIVIDE", q1.toString(), q2.toString(), String.valueOf(result));
//        return result;
//    }
    @Override
    public QuantityDTO divide(QuantityDTO q1, QuantityDTO q2) {
        Quantity<IMeasurable> quantity1 = toQuantity(q1);
        Quantity<IMeasurable> quantity2 = toQuantity(q2);

        // We use the unit of the first quantity as the 'target' 
        // or you could pass a specific unit from the DTO.
        Quantity<IMeasurable> result = quantity1.divide(quantity2, quantity1.getUnit());

        saveAudit("DIVIDE", q1.toString(), q2.toString(), result.toString());

        return new QuantityDTO(
                result.getValue(),
                result.getUnit().getUnitName()
        );
    }

    // --- Private Helpers ---

    private void saveAudit(String operation, String param1, String param2, String resultValue) {
        repository.save(new QuantityMeasurementEntity(
                operation, param1, param2, resultValue, getCurrentUser()
        ));
    }

    private User getCurrentUser() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return null;
        }
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElse(null);
    }
}