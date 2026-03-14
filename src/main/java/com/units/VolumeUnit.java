package com.units;

public enum VolumeUnit implements IMeasurable{
	LITRE(1.0),   //base unit 
	MILLILITRE(0.001),
	GALLON(3.78541);
	
	private final double conversionFactor;
	
	VolumeUnit(double converisonFactor){
		this.conversionFactor=converisonFactor;
	}
	

	
	
	public double getConversionFactor() {
		return conversionFactor;
	}
	
	public double convertToBaseUnit(double value) {
        return value * conversionFactor;
    }

    public double convertFromBaseUnit(double baseValue) {
        return baseValue / conversionFactor;
    }

	@Override
	public String getUnitName() {
		return this.toString();
	}
	
	@Override
	public String getMeasurementType() {
		return this.getClass().getSimpleName();
	}

	
	@Override
    public IMeasurable getUnitInstance(String unitName) {
        for (VolumeUnit unit : VolumeUnit.values()) {
            if (unit.getUnitName().equalsIgnoreCase(unitName)) return unit;
        }
        throw new IllegalArgumentException("Invalid length unit: " + unitName);
    }

}