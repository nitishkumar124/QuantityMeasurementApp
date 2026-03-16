package com;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.app.quantitymeasurement.controller.QuantityMeasurementController;
import com.app.quantitymeasurement.entity.QuantityDTO;
import com.app.quantitymeasurement.entity.QuantityMeasurementEntity;
import com.app.quantitymeasurement.entity.QuantityModel;
import com.app.quantitymeasurement.exception.QuantityMeasurementException;
import com.app.quantitymeasurement.repository.IQuantityMeasurementRepository;
import com.app.quantitymeasurement.repository.QuantityMeasurementCacheRepository;
import com.app.quantitymeasurement.service.IQuantityMeasurementService;
import com.app.quantitymeasurement.service.QuantityMeasurementServiceImpl;
import com.app.quantitymeasurement.unit.IMeasurable;
import com.app.quantitymeasurement.unit.LengthUnit;

public class QuantityMeasurementAppTest {


IQuantityMeasurementRepository repository;
IQuantityMeasurementService service;

@BeforeEach
void setup() {
    repository = QuantityMeasurementCacheRepository.getInstance();
    service = new QuantityMeasurementServiceImpl(repository);
}

// ================= ENTITY TESTS =================

@Test
void testQuantityEntity_SingleOperandConstruction() {
    QuantityModel<IMeasurable> q1 = new QuantityModel<>(10, LengthUnit.FEET);

    QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, null, "CONVERT", "12 INCHES");

    assertEquals(10, entity.thisValue);
    assertEquals("FEET", entity.thisUnit);
    assertEquals("CONVERT", entity.operation);
}

@Test
void testQuantityEntity_BinaryOperandConstruction() {

    QuantityModel<IMeasurable> q1 =
            new QuantityModel<>(1, LengthUnit.FEET);

    QuantityModel<IMeasurable> q2 =
            new QuantityModel<>(12, LengthUnit.INCHES);

    QuantityModel<IMeasurable> result =
            new QuantityModel<>(2, LengthUnit.FEET);

    QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "ADD", result);

    assertEquals(1, entity.thisValue);
    assertEquals(12, entity.thatValue);
    assertEquals(2, entity.resultValue);
}

@Test
void testQuantityEntity_ErrorConstruction() {

    QuantityModel<IMeasurable> q1 =
            new QuantityModel<>(10, LengthUnit.FEET);

    QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(
                    q1, null, "ADD",
                    "Operation not supported",
                    true);

    assertTrue(entity.isError);
    assertEquals("Operation not supported", entity.errorMessage);
}

@Test
void testQuantityEntity_ToString_Success() {

    QuantityModel<IMeasurable> q1 =
            new QuantityModel<>(1, LengthUnit.FEET);

    QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, null, "COMPARE", "Equal");

    String output = entity.toString();

    assertTrue(output.contains("COMPARE"));
    assertTrue(output.contains("Equal"));
}

@Test
void testQuantityEntity_ToString_Error() {

    QuantityModel<IMeasurable> q1 =
            new QuantityModel<>(1, LengthUnit.FEET);

    QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, null, "ADD", "Invalid", true);

    String output = entity.toString();

    assertTrue(output.contains("[ERROR]"));
    assertTrue(output.contains("Invalid"));
}

// ================= SERVICE TESTS =================

@Test
void testService_CompareEquality_SameUnit_Success() {

    QuantityDTO q1 = new QuantityDTO(1, QuantityDTO.LengthUnit.FEET);
    QuantityDTO q2 = new QuantityDTO(1, QuantityDTO.LengthUnit.FEET);

    assertTrue(service.compare(q1, q2));
}

@Test
void testService_CompareEquality_DifferentUnit_Success() {

    QuantityDTO q1 = new QuantityDTO(1, QuantityDTO.LengthUnit.FEET);
    QuantityDTO q2 = new QuantityDTO(12, QuantityDTO.LengthUnit.INCHES);

    assertTrue(service.compare(q1, q2));
}

@Test
void testService_CompareEquality_CrossCategory_Error() {

    QuantityDTO q1 = new QuantityDTO(1, QuantityDTO.LengthUnit.FEET);
    QuantityDTO q2 = new QuantityDTO(1, QuantityDTO.WeightUnit.KILOGRAMS);

    assertFalse(service.compare(q1, q2));
}

@Test
void testService_Convert_Success() {

    QuantityDTO q1 =
            new QuantityDTO(1, QuantityDTO.LengthUnit.FEET);

    QuantityDTO target =
            new QuantityDTO(0, QuantityDTO.LengthUnit.INCHES);

    QuantityDTO result = service.convert(q1, target);

    assertEquals("INCHES", result.getUnit());
}

@Test
void testService_Add_Success() {

    QuantityDTO q1 =
            new QuantityDTO(1, QuantityDTO.LengthUnit.FEET);

    QuantityDTO q2 =
            new QuantityDTO(12, QuantityDTO.LengthUnit.INCHES);

    QuantityDTO result = service.add(q1, q2);

    assertNotNull(result);
}

@Test
void testService_Add_UnsupportedOperation_Error() {

    QuantityDTO q1 =
            new QuantityDTO(10, QuantityDTO.TemperatureUnit.CELSIUS);

    QuantityDTO q2 =
            new QuantityDTO(20, QuantityDTO.TemperatureUnit.CELSIUS);

    assertThrows(QuantityMeasurementException.class,
            () -> service.add(q1, q2));
}

@Test
void testService_Subtract_Success() {

    QuantityDTO q1 =
            new QuantityDTO(10, QuantityDTO.LengthUnit.FEET);

    QuantityDTO q2 =
            new QuantityDTO(6, QuantityDTO.LengthUnit.INCHES);

    QuantityDTO result = service.subtract(q1, q2);

    assertNotNull(result);
}

@Test
void testService_Divide_Success() {

    QuantityDTO q1 =
            new QuantityDTO(10, QuantityDTO.LengthUnit.FEET);

    QuantityDTO q2 =
            new QuantityDTO(2, QuantityDTO.LengthUnit.FEET);

    double result = service.divide(q1, q2);

    assertEquals(5, result);
}

@Test
void testService_Divide_ByZero_Error() {

    QuantityDTO q1 =
            new QuantityDTO(10, QuantityDTO.LengthUnit.FEET);

    QuantityDTO q2 =
            new QuantityDTO(0, QuantityDTO.LengthUnit.FEET);

    assertThrows(QuantityMeasurementException.class,
            () -> service.divide(q1, q2));
}

// ================= CONTROLLER TESTS =================

@Test
void testController_DemonstrateEquality_Success() {

    QuantityMeasurementController controller =
            new QuantityMeasurementController(service);

    boolean result = controller.performComparison(
            new QuantityDTO(1, QuantityDTO.LengthUnit.FEET),
            new QuantityDTO(12, QuantityDTO.LengthUnit.INCHES));

    assertTrue(result);
}

@Test
void testController_DemonstrateConversion_Success() {

    QuantityMeasurementController controller =
            new QuantityMeasurementController(service);

    QuantityDTO result = controller.performConversion(
            new QuantityDTO(1, QuantityDTO.LengthUnit.FEET),
            new QuantityDTO(0, QuantityDTO.LengthUnit.INCHES));

    assertEquals("INCHES", result.getUnit());
}

@Test
void testController_DemonstrateAddition_Success() {

    QuantityMeasurementController controller =
            new QuantityMeasurementController(service);

    QuantityDTO result = controller.performAddition(
            new QuantityDTO(1, QuantityDTO.LengthUnit.FEET),
            new QuantityDTO(12, QuantityDTO.LengthUnit.INCHES));

    assertEquals("FEET", result.getUnit());
}

// ================= INTEGRATION TEST =================

@Test
void testIntegration_EndToEnd_LengthAddition() {

    QuantityMeasurementController controller =
            new QuantityMeasurementController(service);

    QuantityDTO result =
            controller.performAddition(
                    new QuantityDTO(1, QuantityDTO.LengthUnit.FEET),
                    new QuantityDTO(12, QuantityDTO.LengthUnit.INCHES));

    assertNotNull(result);
}
}
