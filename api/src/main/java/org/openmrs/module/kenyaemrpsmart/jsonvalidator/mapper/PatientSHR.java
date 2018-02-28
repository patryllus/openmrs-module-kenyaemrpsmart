package org.openmrs.module.kenyaemrpsmart.jsonvalidator.mapper;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemr.Metadata;
import org.openmrs.module.kenyaemrpsmart.metadata.SmartCardMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatientSHR {
   /* public SHR.PATIENT_IDENTIFICATION pATIENT_IDENTIFICATION;
    public SHR.NEXT_OF_KIN nEXT_OF_KIN[];
    public SHR.HIV_TEST hIV_TEST[];
    public SHR.IMMUNIZATION iMMUNIZATION[];
    public SHR.MERGE_PATIENT_INFORMATION mERGE_PATIENT_INFORMATION;
    public SHR.CARD_DETAILS cARD_DETAILS;*/
   private int patientID;
   private Patient patient;
   @Autowired
    PatientService patientService;

   @Autowired
    EncounterService encounterService;

   @Autowired
    PersonService personService;

   @Autowired
    ObsService obsService;

    public PatientSHR(int patientID) {
        this.patientID = patientID;
        this.patient = patientService.getPatient(patientID);
    }

    private ObjectNode patientIdentification () {

        PatientIdentifierType HEI_NUMBER = patientService.getPatientIdentifierTypeByUuid(Metadata.IdentifierType.HEI_UNIQUE_NUMBER);
        PatientIdentifierType CCC_NUMBER = patientService.getPatientIdentifierTypeByUuid(Metadata.IdentifierType.UNIQUE_PATIENT_NUMBER);
        PatientIdentifierType NATIONAL_ID = patientService.getPatientIdentifierTypeByUuid(Metadata.IdentifierType.NATIONAL_ID);
        PatientIdentifierType SMART_CARD_SERIAL_NUMBER = patientService.getPatientIdentifierTypeByUuid(SmartCardMetadata._PatientIdentifierType.SMART_CARD_SERIAL_NUMBER);
        PatientIdentifierType HTS_NUMBER = patientService.getPatientIdentifierTypeByUuid(SmartCardMetadata._PatientIdentifierType.HTS_NUMBER);
        PatientIdentifierType GODS_NUMBER = patientService.getPatientIdentifierTypeByUuid(SmartCardMetadata._PatientIdentifierType.GODS_NUMBER);

        String ANC_NUMBER = "161655AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

        List<PatientIdentifier> identifierList = patientService.getPatientIdentifiers(null, Arrays.asList(HEI_NUMBER, CCC_NUMBER, NATIONAL_ID, SMART_CARD_SERIAL_NUMBER, HTS_NUMBER, GODS_NUMBER), null, Arrays.asList(patient), null);
        Map<String, String> patientIdentifiers = new HashMap<String, String>();

        final JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode identifiers = factory.objectNode();
        ArrayNode internalIdentifiers = factory.arrayNode();
        ObjectNode externalIdentifiers = factory.objectNode();
        for (PatientIdentifier identifier: identifierList) {
            PatientIdentifierType identifierType = identifier.getIdentifierType();
            ObjectNode element = factory.objectNode();
            if (identifierType.equals(HEI_NUMBER)) {
                patientIdentifiers.put("HEI_NUMBER", identifier.getIdentifier());

                element.put("ID", identifier.getIdentifier());
                element.put("IDENTIFIER_TYPE", "HEI_NUMBER");
                element.put("ASSIGNING_AUTHORITY", "MCH");
                element.put("ASSIGNING_FACILITY", "10829");
            } else if (identifierType.equals(CCC_NUMBER)) {
                patientIdentifiers.put("CCC_NUMBER", identifier.getIdentifier());
                element.put("ID", identifier.getIdentifier());
                element.put("IDENTIFIER_TYPE", "CCC_NUMBER");
                element.put("ASSIGNING_AUTHORITY", "CCC");
                element.put("ASSIGNING_FACILITY", "10829");
            } else if (identifierType.equals(NATIONAL_ID)) {
                patientIdentifiers.put("NATIONAL_ID", identifier.getIdentifier());
                element.put("ID", identifier.getIdentifier());
                element.put("IDENTIFIER_TYPE", "NATIONAL_ID");
                element.put("ASSIGNING_AUTHORITY", "GOK");
                element.put("ASSIGNING_FACILITY", "10829");
            } else if (identifierType.equals(SMART_CARD_SERIAL_NUMBER)) {
                patientIdentifiers.put("CARD_SERIAL_NUMBER", identifier.getIdentifier());
                element.put("ID", identifier.getIdentifier());
                element.put("IDENTIFIER_TYPE", "CARD_SERIAL_NUMBER");
                element.put("ASSIGNING_AUTHORITY", "CARD_REGISTRY");
                element.put("ASSIGNING_FACILITY", "10829");
            } else if (identifierType.equals(HTS_NUMBER)) {
                patientIdentifiers.put("HTS_NUMBER", identifier.getIdentifier());
                element.put("ID", identifier.getIdentifier());
                element.put("IDENTIFIER_TYPE", "HTS_NUMBER");
                element.put("ASSIGNING_AUTHORITY", "HTS");
                element.put("ASSIGNING_FACILITY", "10829");
            }

            internalIdentifiers.add(element);

            if (identifierType.equals(GODS_NUMBER)) {
                patientIdentifiers.put("GODS_NUMBER", identifier.getIdentifier());
                externalIdentifiers.put("ID", identifier.getIdentifier());
                externalIdentifiers.put("IDENTIFIER_TYPE", "GODS_NUMBER");
                externalIdentifiers.put("ASSIGNING_AUTHORITY", "MPI");
                externalIdentifiers.put("ASSIGNING_FACILITY", "10829");
            }

        }

        Obs ancNumber = obsService.getObservationsByPersonAndConcept(patient, Context.getConceptService().getConcept(ANC_NUMBER)).get(0);
        if (ancNumber != null) {
            ObjectNode element = factory.objectNode();
            patientIdentifiers.put("ANC_NUMBER", ancNumber.getValueText());
            element.put("ID", ancNumber.getValueText());
            element.put("IDENTIFIER_TYPE", "GODS_NUMBER");
            element.put("ASSIGNING_AUTHORITY", "MPI");
            element.put("ASSIGNING_FACILITY", "10829");
            internalIdentifiers.add(element);
        }
        identifiers.put("INTERNAL_PATIENT_ID", internalIdentifiers);
        identifiers.put("EXTERNAL_PATIENT_ID", externalIdentifiers);
        return identifiers;
   }

   private JSONPObject getPatientIdentifiers () {
       return null;
   }

   private JSONPObject getNextOfKinDetails () {
       return null;
   }

   private JSONPObject getHivTestDetails () {
       return null;
   }

   private String getFacilityMFL () {
       return null;
   }

   private JSONPObject getImmunizationDetails () {
       return null;
   }

    public int getPatientID() {
        return patientID;
    }

    public void setPatientID(int patientID) {
        this.patientID = patientID;
    }
}
