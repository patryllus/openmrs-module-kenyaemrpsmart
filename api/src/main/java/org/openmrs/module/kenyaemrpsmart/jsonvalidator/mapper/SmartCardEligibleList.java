package org.openmrs.module.kenyaemrpsmart.jsonvalidator.mapper;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrpsmart.kenyaemrUtils.Utils;
import org.openmrs.module.kenyaemrpsmart.metadata.SmartCardMetadata;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SmartCardEligibleList {
    String IMMUNIZATION_FORM_UUID = "b4f3859e-861c-4a63-bdff-eb7392030d47";
    String HTS_INITIAL_TEST_FORM_UUID = "402dc5d7-46da-42d4-b2be-f43ea4ad87b0";
    String HTS_CONFIRMATORY_TEST_FORM_UUID = "b08471f6-0892-4bf7-ab2b-bf79797b8ea4";

    private JsonNodeFactory getJsonNodeFactory () {
        final JsonNodeFactory factory = JsonNodeFactory.instance;
        return factory;
    }

    public ArrayNode getEligibleList() {

        Form HTS_INITIAL_FORM = Context.getFormService().getFormByUuid(HTS_INITIAL_TEST_FORM_UUID);
        Form HTS_CONFIRMATORY_FORM = Context.getFormService().getFormByUuid(HTS_CONFIRMATORY_TEST_FORM_UUID);
        Form IMMUNIZATION_FORM =  Context.getFormService().getFormByUuid(IMMUNIZATION_FORM_UUID);
        List<Encounter> allEncounters = Utils.getEncounters(null, Arrays.asList(HTS_CONFIRMATORY_FORM, HTS_INITIAL_FORM, IMMUNIZATION_FORM));
        PatientIdentifierType SMART_CARD_SERIAL_NUMBER_TYPE = Context.getPatientService().getPatientIdentifierTypeByUuid(SmartCardMetadata._PatientIdentifierType.SMART_CARD_SERIAL_NUMBER);

        ArrayNode node = getJsonNodeFactory().arrayNode();
        Set<Patient> patientList = new HashSet<Patient>();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        int counter = 0;
        for(Encounter encounter: allEncounters) {
            if(counter == 10) {
                break;
            }
            Patient patient = encounter.getPatient();
            List<PatientIdentifier> identifiers = patient.getPatientIdentifiers(SMART_CARD_SERIAL_NUMBER_TYPE);
            ObjectNode patientNode = getJsonNodeFactory().objectNode();
            if(!patientList.contains(patient) && identifiers.size() == 0 && df.format(encounter.getEncounterDatetime()).equals(df.format(new Date())) && patient.getAge() < 10) {
                patientNode.put("PATIENTID", patient.getPatientId());
                patientNode.put("FIRSTNAME", patient.getGivenName());
                patientNode.put("MIDDLENAME", patient.getMiddleName());
                patientNode.put("LASTNAME", patient.getFamilyName());
                patientNode.put("AGE", patient.getAge());
                patientNode.put("GENDER", patient.getGender());
                patientList.add(patient);
                node.add(patientNode);
                counter++;

            }


        }
        /*ObjectNode wrapper = getJsonNodeFactory().objectNode();
        wrapper.put("ELIGIBLE_LIST", node);*/
        return node;
    }
}
