package org.openmrs.module.kenyaemrpsmart.jsonvalidator.mapper;

import org.openmrs.Concept;

import java.util.Date;

class ImmunizationWrapper {
    private Concept vaccine;
    private Integer sequenceNumber;
    private Date vaccineDate;

    public ImmunizationWrapper() {
    }

    public ImmunizationWrapper(Concept vaccine, Integer sequenceNumber, Date vaccineDate) {
        this.vaccine = vaccine;
        this.sequenceNumber = sequenceNumber;
        this.vaccineDate = vaccineDate;
    }

    public Concept getVaccine() {
        return vaccine;
    }

    public void setVaccine(Concept vaccine) {
        this.vaccine = vaccine;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public Date getVaccineDate() {
        return vaccineDate;
    }

    public void setVaccineDate(Date vaccineDate) {
        this.vaccineDate = vaccineDate;
    }


}
