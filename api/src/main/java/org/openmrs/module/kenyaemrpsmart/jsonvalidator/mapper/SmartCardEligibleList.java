package org.openmrs.module.kenyaemrpsmart.jsonvalidator.mapper;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrpsmart.kenyaemrUtils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SmartCardEligibleList {
    String IMMUNIZATION_FORM_UUID = "b4f3859e-861c-4a63-bdff-eb7392030d47";
    String HTS_INITIAL_TEST_FORM_UUID = "402dc5d7-46da-42d4-b2be-f43ea4ad87b0";
    String HTS_CONFIRMATORY_TEST_FORM_UUID = "b08471f6-0892-4bf7-ab2b-bf79797b8ea4";

    private JsonNodeFactory getJsonNodeFactory () {
        final JsonNodeFactory factory = JsonNodeFactory.instance;
        return factory;
    }

    public ObjectNode getEligibleList() {

        Form HTS_INITIAL_FORM = Context.getFormService().getFormByUuid(HTS_INITIAL_TEST_FORM_UUID);
        Form HTS_CONFIRMATORY_FORM = Context.getFormService().getFormByUuid(HTS_CONFIRMATORY_TEST_FORM_UUID);
        Form IMMUNIZATION_FORM =  Context.getFormService().getFormByUuid(IMMUNIZATION_FORM_UUID);
        List<Encounter> allEncounters = Utils.getEncounters(null, Arrays.asList(HTS_CONFIRMATORY_FORM, HTS_INITIAL_FORM, IMMUNIZATION_FORM));
        ArrayNode node = getJsonNodeFactory().arrayNode();
        List<Patient> patientList = new ArrayList<Patient>();
        int counter = 0;
        for(Encounter encounter: allEncounters) {
            if(counter > 10) {
                break;
            }
            Patient patient = encounter.getPatient();
            ObjectNode patientNode = getJsonNodeFactory().objectNode();
            if(!patientList.contains(patient) && patient.getAge() < 10) {
                patientNode.put("PATIENT_ID", patient.getPatientId());
                patientNode.put("FIRST_NAME", patient.getGivenName());
                patientNode.put("MIDDLE_NAME", patient.getMiddleName());
                patientNode.put("LAST_NAME", patient.getFamilyName());
                patientNode.put("AGE", patient.getAge());
                patientNode.put("GENDER", patient.getGender());
                patientList.add(patient);
                counter++;

            }
            node.add(patientNode);

        }
        ObjectNode wrapper = getJsonNodeFactory().objectNode();
        wrapper.put("ELIGIBLE_LIST", node);
        return wrapper;
    }
}
