package org.openmrs.module.kenyaemrpsmart.jsonvalidator.mapper;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemr.api.KenyaEmrService;
import org.openmrs.module.kenyaemrpsmart.metadata.SmartCardMetadata;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PatientSHR {
   /* public SHR.PATIENT_IDENTIFICATION pATIENT_IDENTIFICATION;
    public SHR.NEXT_OF_KIN nEXT_OF_KIN[];
    public SHR.HIV_TEST hIV_TEST[];
    public SHR.IMMUNIZATION iMMUNIZATION[];
    public SHR.MERGE_PATIENT_INFORMATION mERGE_PATIENT_INFORMATION;
    public SHR.CARD_DETAILS cARD_DETAILS;*/
   private int patientID;
   private Patient patient;
   private PersonService personService;
   private PatientService patientService;
   private ObsService obsService;
   private AdministrationService administrationService;


    public PatientSHR(int patientID) {
        this.patientID = patientID;
        this.patient = Context.getPatientService().getPatient(patientID);
        this.personService = Context.getPersonService();
        this.patientService = Context.getPatientService();
        this.obsService = Context.getObsService();
        this.administrationService = Context.getAdministrationService();
    }

    private JsonNodeFactory getJsonNodeFactory () {
        final JsonNodeFactory factory = JsonNodeFactory.instance;
        return factory;
    }

    private ObjectNode getPatientName () {
        PersonName pn = patient.getPersonName();
        ObjectNode nameNode = getJsonNodeFactory().objectNode();
        nameNode.put("FIRST_NAME", pn.getGivenName());
        nameNode.put("MIDDLE_NAME", pn.getMiddleName());
        nameNode.put("LAST_NAME", pn.getFamilyName());
        return nameNode;
    }

    private String getSHRDateFormat() {
        return "yyyyMMdd";
    }

    private SimpleDateFormat getSimpleDateFormat(String pattern) {
        return new SimpleDateFormat(pattern);
    }

    private ObjectNode getPatientAddress() {
        //String PHONE_CONTACT = CommonMetadata._PersonAttributeType.TELEPHONE_CONTACT;
        /**
         * county: personAddress.country
         * sub-county: personAddress.stateProvince
         * ward: personAddress.address4
         * landmark: personAddress.address2
         * postal address: personAddress.address1
         */

        Set<PersonAddress> addresses = patient.getAddresses();
        //patient address
        ObjectNode patientAddressNode = getJsonNodeFactory().objectNode();
        ObjectNode physicalAddressNode = getJsonNodeFactory().objectNode();
        String postalAddress = "";
        String county = "";
        String sub_county = "";
        String ward = "";
        String landMark = "";

        for (PersonAddress address : addresses) {
            if (address.getAddress1() != null) {
                postalAddress = address.getAddress1();
            }
            if (address.getCountry() != null) {
                county = address.getCountry() != null? address.getCountry(): "";
            } else if (address.getStateProvince() != null) {
                sub_county = address.getStateProvince() != null? address.getStateProvince(): "";
            } else if (address.getAddress4() != null) {
                ward = address.getAddress4() != null? address.getAddress4(): "";
            } else if (address.getAddress2() != null) {
                landMark =  address.getAddress2() != null? address.getAddress2(): "";
            }

        }

        physicalAddressNode.put("COUNTY", county);
        physicalAddressNode.put("SUB_COUNTY", sub_county);
        physicalAddressNode.put("WARD", ward);
        physicalAddressNode.put("NEAREST_LANDMARK", landMark);

        //combine all addresses
        patientAddressNode.put("PHYSICAL_ADDRESS", physicalAddressNode);
        patientAddressNode.put("POSTAL_ADDRESS", postalAddress);

        return patientAddressNode;
    }


    public ObjectNode patientIdentification () {


        String HEI_UNIQUE_NUMBER = "0691f522-dd67-4eeb-92c8-af5083baf338";
        String NATIONAL_ID = "49af6cdc-7968-4abb-bf46-de10d7f4859f";
        String UNIQUE_PATIENT_NUMBER = "05ee9cf4-7242-4a17-b4d4-00f707265c8a";
        String ANC_NUMBER = "161655AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

        PatientIdentifierType HEI_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(HEI_UNIQUE_NUMBER);
        PatientIdentifierType CCC_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(UNIQUE_PATIENT_NUMBER);
        PatientIdentifierType NATIONAL_ID_TYPE = patientService.getPatientIdentifierTypeByUuid(NATIONAL_ID);
        PatientIdentifierType SMART_CARD_SERIAL_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(SmartCardMetadata._PatientIdentifierType.SMART_CARD_SERIAL_NUMBER);
        PatientIdentifierType HTS_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(SmartCardMetadata._PatientIdentifierType.HTS_NUMBER);
        PatientIdentifierType GODS_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(SmartCardMetadata._PatientIdentifierType.GODS_NUMBER);



        List<PatientIdentifier> identifierList = patientService.getPatientIdentifiers(null, Arrays.asList(HEI_NUMBER_TYPE, CCC_NUMBER_TYPE, NATIONAL_ID_TYPE, SMART_CARD_SERIAL_NUMBER_TYPE, HTS_NUMBER_TYPE, GODS_NUMBER_TYPE), null, Arrays.asList(patient), null);
        Map<String, String> patientIdentifiers = new HashMap<String, String>();
        String facilityMFL = getFacilityMFL();
        JsonNodeFactory factory = getJsonNodeFactory();
        ObjectNode identifiers = factory.objectNode();
        ArrayNode internalIdentifiers = factory.arrayNode();
        ObjectNode externalIdentifiers = factory.objectNode();

        for (PatientIdentifier identifier: identifierList) {
            PatientIdentifierType identifierType = identifier.getIdentifierType();
            ObjectNode element = factory.objectNode();
            if (identifierType.equals(HEI_NUMBER_TYPE)) {
                patientIdentifiers.put("HEI_NUMBER", identifier.getIdentifier());

                element.put("ID", identifier.getIdentifier());
                element.put("IDENTIFIER_TYPE", "HEI_NUMBER");
                element.put("ASSIGNING_AUTHORITY", "MCH");
                element.put("ASSIGNING_FACILITY", facilityMFL);

            } else if (identifierType.equals(CCC_NUMBER_TYPE)) {
                patientIdentifiers.put("CCC_NUMBER", identifier.getIdentifier());
                element.put("ID", identifier.getIdentifier());
                element.put("IDENTIFIER_TYPE", "CCC_NUMBER");
                element.put("ASSIGNING_AUTHORITY", "CCC");
                element.put("ASSIGNING_FACILITY", facilityMFL);

            } else if (identifierType.equals(NATIONAL_ID_TYPE)) {
                patientIdentifiers.put("NATIONAL_ID", identifier.getIdentifier());
                element.put("ID", identifier.getIdentifier());
                element.put("IDENTIFIER_TYPE", "NATIONAL_ID");
                element.put("ASSIGNING_AUTHORITY", "GOK");
                element.put("ASSIGNING_FACILITY", facilityMFL);

            } else if (identifierType.equals(SMART_CARD_SERIAL_NUMBER_TYPE)) {
                patientIdentifiers.put("CARD_SERIAL_NUMBER", identifier.getIdentifier());
                element.put("ID", identifier.getIdentifier());
                element.put("IDENTIFIER_TYPE", "CARD_SERIAL_NUMBER");
                element.put("ASSIGNING_AUTHORITY", "CARD_REGISTRY");
                element.put("ASSIGNING_FACILITY", facilityMFL);

            } else if (identifierType.equals(HTS_NUMBER_TYPE)) {
                patientIdentifiers.put("HTS_NUMBER", identifier.getIdentifier());
                element.put("ID", identifier.getIdentifier());
                element.put("IDENTIFIER_TYPE", "HTS_NUMBER");
                element.put("ASSIGNING_AUTHORITY", "HTS");
                element.put("ASSIGNING_FACILITY", facilityMFL);
            }

            internalIdentifiers.add(element);

            if (identifierType.equals(GODS_NUMBER_TYPE)) {
                patientIdentifiers.put("GODS_NUMBER", identifier.getIdentifier());
                externalIdentifiers.put("ID", identifier.getIdentifier());
                externalIdentifiers.put("IDENTIFIER_TYPE", "GODS_NUMBER");
                externalIdentifiers.put("ASSIGNING_AUTHORITY", "MPI");
                externalIdentifiers.put("ASSIGNING_FACILITY", facilityMFL);
            }

        }

        List<Obs> ancNumberObs = obsService.getObservationsByPersonAndConcept(patient, Context.getConceptService().getConceptByUuid(ANC_NUMBER));
        Obs ancNumber = null;
        if (ancNumberObs != null && !ancNumberObs.isEmpty()) 
            ancNumber = ancNumberObs.get(0);
        if (ancNumber != null) {
            ObjectNode element = factory.objectNode();
            patientIdentifiers.put("ANC_NUMBER", ancNumber.getValueText());
            element.put("ID", ancNumber.getValueText());
            element.put("IDENTIFIER_TYPE", "GODS_NUMBER");
            element.put("ASSIGNING_AUTHORITY", "MPI");
            element.put("ASSIGNING_FACILITY", facilityMFL);
            internalIdentifiers.add(element);
        }

        // get other patient details

        String dob = getSimpleDateFormat(getSHRDateFormat()).format(patient.getBirthdate());
        String dobPrecision = patient.getBirthdateEstimated()? "ESTIMATED" : "EXACT";
        String sex = patient.getGender();

        // get death details
        String deathDate;
        String deathIndicator;
        if (patient.getDeathDate() != null) {
            deathDate = getSimpleDateFormat(getSHRDateFormat()).format(patient.getDeathDate());
            deathIndicator = "Y";
        }
        else {
            deathDate = "";
            deathIndicator = "N";
        }


        identifiers.put("INTERNAL_PATIENT_ID", internalIdentifiers);
        identifiers.put("EXTERNAL_PATIENT_ID", externalIdentifiers);
        identifiers.put("PATIENT_NAME", getPatientName());
        identifiers.put("DATE_OF_BIRTH", dob);
        identifiers.put("DATE_OF_BIRTH_PRECISION", dobPrecision);
        identifiers.put("SEX", sex);
        identifiers.put("DEATH_DATE", deathDate);
        identifiers.put("DEATH_INDICATOR", deathIndicator);
        identifiers.put("PATIENT_ADDRESS", getPatientAddress());
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
       return Context.getService(KenyaEmrService.class).getDefaultLocationMflCode();
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