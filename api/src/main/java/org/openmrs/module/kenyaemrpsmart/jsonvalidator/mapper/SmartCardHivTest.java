package org.openmrs.module.kenyaemrpsmart.jsonvalidator.mapper;

import org.openmrs.Concept;
import org.openmrs.logic.op.In;

import java.util.Date;

public class SmartCardHivTest {
    private Concept result;
    private String facility;
    private Concept strategy;
    private Date dateTested;
    private String type;

    public SmartCardHivTest(Concept result, String facility, Concept strategy, Date dateTested, String type) {
        this.result = result;
        this.facility = facility;
        this.strategy = strategy;
        this.dateTested = dateTested;
        this.type = type;
    }

    public Concept getResult() {
        return result;
    }

    public void setResult(Concept result) {
        this.result = result;
    }

    public String getFacility() {
        return facility;
    }

    public void setFacility(String facility) {
        this.facility = facility;
    }

    public Concept getStrategy() {
        return strategy;
    }

    public void setStrategy(Concept strategy) {
        this.strategy = strategy;
    }

    public Date getDateTested() {
        return dateTested;
    }

    public void setDateTested(Date dateTested) {
        this.dateTested = dateTested;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
