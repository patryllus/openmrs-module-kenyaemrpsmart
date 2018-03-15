package org.openmrs.module.kenyaemrpsmart.jsonvalidator.mapper;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class MiddlewareRequest {

    String patientID;
    String cardSerialNumber;

    @JsonCreator
    public MiddlewareRequest(@JsonProperty("PATIENTID") String patientID, @JsonProperty("CARD_SERIAL_NO") String cardSerialNumber) {
        this.patientID = patientID;
        this.cardSerialNumber = cardSerialNumber;
    }

    @JsonProperty("PATIENTID")
    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    @JsonProperty("CARD_SERIAL_NO")
    public String getCardSerialNumber() {
        return cardSerialNumber;
    }

    public void setCardSerialNumber(String cardSerialNumber) {
        this.cardSerialNumber = cardSerialNumber;
    }
}
