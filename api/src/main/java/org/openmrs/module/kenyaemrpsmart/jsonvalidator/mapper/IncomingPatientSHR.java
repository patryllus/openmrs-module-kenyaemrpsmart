package org.openmrs.module.kenyaemrpsmart.jsonvalidator.mapper;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.module.kenyaemrpsmart.jsonvalidator.utils.SHRUtils;
import org.openmrs.module.kenyaemrpsmart.kenyaemrUtils.Utils;
import org.openmrs.module.kenyaemrpsmart.metadata.SmartCardMetadata;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class IncomingPatientSHR {

    private Patient patient;
    private PersonService personService;
    private PatientService patientService;
    private ObsService obsService;
    private ConceptService conceptService;
    private AdministrationService administrationService;
    private EncounterService encounterService;
    private String incomingSHR;

    String TELEPHONE_CONTACT = "b2c38640-2603-4629-aebd-3b54f33f1e3a";
    String CIVIL_STATUS_CONCEPT = "1054AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    String PSMART_ENCOUNTER_TYPE_UUID = "9bc15e94-2794-11e8-b467-0ed5f89f718b";

    public IncomingPatientSHR(String shr) {

        this.patientService = Context.getPatientService();
        this.personService = Context.getPersonService();
        this.obsService = Context.getObsService();
        this.administrationService = Context.getAdministrationService();
        this.conceptService = Context.getConceptService();
        this.encounterService = Context.getEncounterService();
        this.incomingSHR = shr;
    }

    public IncomingPatientSHR(Integer patientID) {

        this.patientService = Context.getPatientService();
        this.personService = Context.getPersonService();
        this.obsService = Context.getObsService();
        this.administrationService = Context.getAdministrationService();
        this.conceptService = Context.getConceptService();
        this.encounterService = Context.getEncounterService();
        this.patient = patientService.getPatient(patientID);
    }

    public String processIncomingSHR() {
        createOrUpdatePatient();
        savePatientIdentifiers();
        savePersonAddresses();
        savePersonAttributes();
        String msg = "";
        try {
            patientService.savePatient(this.patient);

            try {
                saveHivTestData();
                msg = "Successfully saved all Hiv Test Data";
            } catch (Exception ex) {
                msg = "There was an error processing patient HIV tests";
            }

        } catch (Exception e) {
            e.printStackTrace();
            msg = "There was an error processing patient SHR";
        }

        return msg;
    }

    public String assignCardSerialIdentifier(String identifier, String encryptedSHR) {
        PatientIdentifierType SMART_CARD_SERIAL_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(SmartCardMetadata._PatientIdentifierType.SMART_CARD_SERIAL_NUMBER);

        if(identifier != null) {

            // check if no other patient has same identifier
            List<Patient> patientsAssignedId = patientService.getPatients(null, identifier.trim(), Arrays.asList(SMART_CARD_SERIAL_NUMBER_TYPE), false);
            if(patientsAssignedId.size() > 0) {
                return "Identifier already assigned";
            }

            // check if patient already has the identifier
            List<PatientIdentifier> existingIdentifiers = patient.getPatientIdentifiers(SMART_CARD_SERIAL_NUMBER_TYPE);

            boolean found = false;
            for(PatientIdentifier id : existingIdentifiers) {
                if (id.getIdentifier().equals(identifier.trim())) {
                    found = true;
                    return "Client already assigned the card serial";
                }
            }


            if(!found) {
                PatientIdentifier patientIdentifier = new PatientIdentifier();
                patientIdentifier.setIdentifierType(SMART_CARD_SERIAL_NUMBER_TYPE);
                patientIdentifier.setLocation(Utils.getDefaultLocation());
                patientIdentifier.setIdentifier(identifier.trim());
                patient.addIdentifier(patientIdentifier);
                patientService.savePatient(patient);
                return "Card Serial successfully assigned";
            }
        }
        return "No identifier provided";
    }

    private void createOrUpdatePatient () {

        String fName = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.pATIENT_NAME.fIRST_NAME;
        String mName = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.pATIENT_NAME.mIDDLE_NAME;
        String lName = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.pATIENT_NAME.lAST_NAME;
        String dobString   = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.dATE_OF_BIRTH;
        String dobPrecision   = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.dATE_OF_BIRTH_PRECISION;
        Date dob = null;
        try {
            dob   = new SimpleDateFormat("yyyyMMdd").parse(dobString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String gender =SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.sEX;

        this.patient = new Patient();
        this.patient.setGender(gender);
        this.patient.addName(new PersonName(fName, mName, lName));
        if (dob != null) {
            this.patient.setBirthdate(dob);
        }

        if (dobPrecision != null && dobPrecision.equals("ESTIMATED")) {
            this.patient.setBirthdateEstimated(true);
        } else if (dobPrecision != null && dobPrecision.equals("EXACT")) {
            this.patient.setBirthdateEstimated(false);
        }

        //patientService.savePatient(this.patient);

    }

    private void savePatientIdentifiers () {

        String HEI_UNIQUE_NUMBER = "0691f522-dd67-4eeb-92c8-af5083baf338";
        String NATIONAL_ID = "49af6cdc-7968-4abb-bf46-de10d7f4859f";
        String UNIQUE_PATIENT_NUMBER = "05ee9cf4-7242-4a17-b4d4-00f707265c8a";
        String ANC_NUMBER = "161655AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        Set<PatientIdentifier> identifierSet = new HashSet<PatientIdentifier>();

        PatientIdentifierType HEI_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(HEI_UNIQUE_NUMBER);
        PatientIdentifierType CCC_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(UNIQUE_PATIENT_NUMBER);
        PatientIdentifierType NATIONAL_ID_TYPE = patientService.getPatientIdentifierTypeByUuid(NATIONAL_ID);
        PatientIdentifierType SMART_CARD_SERIAL_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(SmartCardMetadata._PatientIdentifierType.SMART_CARD_SERIAL_NUMBER);
        PatientIdentifierType HTS_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(SmartCardMetadata._PatientIdentifierType.HTS_NUMBER);
        PatientIdentifierType GODS_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(SmartCardMetadata._PatientIdentifierType.GODS_NUMBER);

        PatientIdentifier openMRSID = generateOpenMRSID();
        //openMRSID.setPreferred(true);
        identifierSet.add(openMRSID);
        // extract GOD's Number
        String shrGodsNumber =SHRUtils.getSHR(incomingSHR).pATIENT_IDENTIFICATION.eXTERNAL_PATIENT_ID.iD;
        if (shrGodsNumber != null) {
            String godsNumberAssigningFacility = SHRUtils.getSHR(incomingSHR).pATIENT_IDENTIFICATION.eXTERNAL_PATIENT_ID.aSSIGNING_FACILITY;
            PatientIdentifier godsNumber = new PatientIdentifier();
            godsNumber.setIdentifierType(GODS_NUMBER_TYPE);
            godsNumber.setIdentifier(shrGodsNumber);
            godsNumber.setLocation(Utils.getDefaultLocation());
            //godsNumber.setLocation(new Location(Integer.parseInt(godsNumberAssigningFacility)));
            identifierSet.add(godsNumber);
        }

        // process internal identifiers

        for (int x = 0; x<SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.iNTERNAL_PATIENT_ID.length;x++) {

            String idType = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.iNTERNAL_PATIENT_ID[x].iDENTIFIER_TYPE;
            PatientIdentifier patientIdentifier = new PatientIdentifier();
            PatientIdentifierType identifierType = null;

            String identifier = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.iNTERNAL_PATIENT_ID[x].iD;

            if (idType.equals("ANC_NUMBER")) {
               // first save patient
               /* patientService.savePatient(this.patient);
                Obs ancNumberObs = new Obs();
                ancNumberObs.setConcept(conceptService.getConceptByUuid(ANC_NUMBER));
                ancNumberObs.setValueText(identifier);
                ancNumberObs.setPerson(this.patient);
                ancNumberObs.setObsDatetime(new Date());
                obsService.saveObs(ancNumberObs, null);*/

            } else {
                if (idType.equals("HEI_NUMBER")) {
                    identifierType = HEI_NUMBER_TYPE;
                } else if (idType.equals("CCC_NUMBER")) {
                    identifierType = CCC_NUMBER_TYPE;
                } else if (idType.equals("NATIONAL_ID")) {
                    identifierType = NATIONAL_ID_TYPE;
                } else if (idType.equals("CARD_SERIAL_NUMBER")) {
                    identifierType = SMART_CARD_SERIAL_NUMBER_TYPE;
                } else if (idType.equals("HTS_NUMBER")) {
                    identifierType = HTS_NUMBER_TYPE;
                }

                String assigningFacility = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.iNTERNAL_PATIENT_ID[x].aSSIGNING_FACILITY;
                patientIdentifier.setIdentifierType(identifierType);
                patientIdentifier.setIdentifier(identifier);
                patientIdentifier.setLocation(Utils.getDefaultLocation());
                if (x ==0) {
                    patientIdentifier.setPreferred(true);
                }
                identifierSet.add(patientIdentifier);
            }

        }

        if (!identifierSet.isEmpty()) {
            patient.setIdentifiers(identifierSet);
            //patientService.savePatient(this.patient);
        }

    }

    Concept testTypeConverter (String key) {
        Map<String, Concept> testTypeList = new HashMap<String, Concept>();
        testTypeList.put("SCREENING", conceptService.getConcept(162080));
        testTypeList.put("CONFIRMATORY", conceptService.getConcept(162082));
        return testTypeList.get(key);

    }

    Concept hivStatusConverter (String key) {
        Map<String, Concept> hivStatusList = new HashMap<String, Concept>();
        hivStatusList.put("POSITIVE", conceptService.getConcept(703));
        hivStatusList.put("NEGATIVE", conceptService.getConcept(664));
        hivStatusList.put("INCONCLUSIVE", conceptService.getConcept(1138));
        return hivStatusList.get(key);
    }

    Concept testStrategyConverter (String key) {
        Map<String, Concept> hivTestStrategyList = new HashMap<String, Concept>();
        hivTestStrategyList.put("HP", conceptService.getConcept(164163));
        hivTestStrategyList.put("NP", conceptService.getConcept(164953));
        hivTestStrategyList.put("VI", conceptService.getConcept(164954));
        hivTestStrategyList.put("VS", conceptService.getConcept(164955));
        hivTestStrategyList.put("HB", conceptService.getConcept(159938));
        hivTestStrategyList.put("MO", conceptService.getConcept(159939));
        return hivTestStrategyList.get(key);
    }

    private void savePersonAttributes () {
        String tELEPHONE= SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.pHONE_NUMBER;
        PersonAttributeType phoneNumberAttrType = personService.getPersonAttributeTypeByUuid(TELEPHONE_CONTACT);
        Set<PersonAttribute> attributes = new TreeSet<PersonAttribute>();
        if (tELEPHONE != null) {
            PersonAttribute phoneContact = new PersonAttribute();
            phoneContact.setAttributeType(phoneNumberAttrType);
            phoneContact.setValue(tELEPHONE);
            attributes.add(phoneContact);
        }
        patient.setAttributes(attributes);
    }

    private void savePersonAddresses () {
        /**
         * county: personAddress.country
         * sub-county: personAddress.stateProvince
         * ward: personAddress.address4
         * landmark: personAddress.address2
         * postal address: personAddress.address1
         */

        String postaladdress =SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.pATIENT_ADDRESS.pOSTAL_ADDRESS;
        String vILLAGE = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.pATIENT_ADDRESS.pHYSICAL_ADDRESS.vILLAGE;
        String wARD = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.pATIENT_ADDRESS.pHYSICAL_ADDRESS.wARD;
        String sUBCOUNTY = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.pATIENT_ADDRESS.pHYSICAL_ADDRESS.sUB_COUNTY;
        String cOUNTY = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.pATIENT_ADDRESS.pHYSICAL_ADDRESS.cOUNTY;
        String nEAREST_LANDMARK = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.pATIENT_ADDRESS.pHYSICAL_ADDRESS.nEAREST_LANDMARK;

        PersonAddress address = new PersonAddress();
        if (cOUNTY != null) {
            address.setCountry(cOUNTY);
        }

        if (sUBCOUNTY != null) {
            address.setStateProvince(sUBCOUNTY);
        }

        if (wARD != null) {
            address.setAddress4(wARD);
        }

        if (nEAREST_LANDMARK != null) {
            address.setAddress2(nEAREST_LANDMARK);
        }

        if (postaladdress != null) {
            address.setAddress1(postaladdress);
        }

        if (vILLAGE != null) {
            address.setCityVillage(vILLAGE);
        }
        Set<PersonAddress> thisAddress = new TreeSet<PersonAddress>();
        thisAddress.add(address);
        patient.setAddresses(thisAddress);
    }



    private void saveHivTestData () {

        Integer finalHivTestResultConcept = 159427;
        Integer	testTypeConcept = 162084;
        Integer testStrategyConcept = 164956;
        Integer healthProviderConcept = 1473;
        Integer healthFacilityNameConcept = 162724;


        for (int i = 0; i<SHRUtils.getSHR(this.incomingSHR).hIV_TEST.length;i++){

            String dateStr = SHRUtils.getSHR(this.incomingSHR).hIV_TEST[i].dATE;
            String result = SHRUtils.getSHR(this.incomingSHR).hIV_TEST[i].rESULT;
            String type = SHRUtils.getSHR(this.incomingSHR).hIV_TEST[i].tYPE;
            String facility = SHRUtils.getSHR(this.incomingSHR).hIV_TEST[i].fACILITY;
            String strategy = SHRUtils.getSHR(this.incomingSHR).hIV_TEST[i].sTRATEGY;
            String providerDetails = SHRUtils.getSHR(this.incomingSHR).hIV_TEST[i].pROVIDER_DETAILS.nAME;
            Date date = null;

            try{
                date = new SimpleDateFormat("yyyyMMdd").parse(dateStr);
            }
            catch(ParseException ex){

                ex.printStackTrace();
            }


            Encounter enc = new Encounter();
            Location location = Context.getLocationService().getLocation(1);
            enc.setLocation(location);
            enc.setEncounterType(Context.getEncounterService().getEncounterTypeByUuid(SmartCardMetadata._EncounterType.EXTERNAL_PSMART_DATA));
            enc.setEncounterDatetime(date);
            enc.setPatient(patient);
            enc.addProvider(Context.getEncounterService().getEncounterRole(1), Context.getProviderService().getProvider(1));
            enc.setForm(Context.getFormService().getFormByUuid(SmartCardMetadata._Form.PSMART_HIV_TEST));


            // build observations
            // test result
            Obs o = new Obs();
            o.setConcept(conceptService.getConcept(finalHivTestResultConcept));
            o.setDateCreated(new Date());
            o.setCreator(Context.getUserService().getUser(1));
            o.setLocation(location);
            o.setObsDatetime(date);
            o.setPerson(this.patient);
            o.setValueCoded(hivStatusConverter(result.trim()));

            // test type
            Obs o1 = new Obs();
            o1.setConcept(conceptService.getConcept(testTypeConcept));
            o1.setDateCreated(new Date());
            o1.setCreator(Context.getUserService().getUser(1));
            o1.setLocation(location);
            o1.setObsDatetime(date);
            o1.setPerson(this.patient);
            o1.setValueCoded(testTypeConverter(type.trim()));

            // test strategy
            Obs o2 = new Obs();
            o2.setConcept(conceptService.getConcept(testStrategyConcept));
            o2.setDateCreated(new Date());
            o2.setCreator(Context.getUserService().getUser(1));
            o2.setLocation(location);
            o2.setObsDatetime(date);
            o2.setPerson(this.patient);
            o2.setValueCoded(testStrategyConverter(strategy.trim()));

            // test provider
            Obs o3 = new Obs();
            o3.setConcept(conceptService.getConcept(healthProviderConcept));
            o3.setDateCreated(new Date());
            o3.setCreator(Context.getUserService().getUser(1));
            o3.setLocation(location);
            o3.setObsDatetime(date);
            o3.setPerson(this.patient);
            o3.setValueText(providerDetails.trim());

            // test facility
            Obs o4 = new Obs();
            o4.setConcept(conceptService.getConcept(healthFacilityNameConcept));
            o4.setDateCreated(new Date());
            o4.setCreator(Context.getUserService().getUser(1));
            o4.setLocation(location);
            o4.setObsDatetime(date);
            o4.setPerson(this.patient);
            o4.setValueText(facility.trim());

            enc.addObs(o);
            enc.addObs(o1);
            enc.addObs(o2);
            enc.addObs(o3);
            enc.addObs(o4);
            encounterService.saveEncounter(enc);

        }
    }

    private void saveImmunization () {
        for (int i = 0; i< SHRUtils.getSHR(this.incomingSHR).iMMUNIZATION.length;i++){

            String name = SHRUtils.getSHR(this.incomingSHR).iMMUNIZATION[i].nAME;
            String dateAministered = SHRUtils.getSHR(this.incomingSHR).iMMUNIZATION[i].dATE_ADMINISTERED;
            Date date = null;
            try{
                date = new SimpleDateFormat("yyyyMMdd").parse(dateAministered);
            }
            catch(ParseException ex){

                ex.printStackTrace();
            }

            Encounter enc = new Encounter();
            Location location = Context.getLocationService().getLocation(1);
            enc.setLocation(location);
            enc.setEncounterType(Context.getEncounterService().getEncounterTypeByUuid(SmartCardMetadata._EncounterType.EXTERNAL_PSMART_DATA));
            enc.setEncounterDatetime(date);
            enc.setPatient(patient);
            enc.addProvider(Context.getEncounterService().getEncounterRole(1), Context.getProviderService().getProvider(1));
            enc.setForm(Context.getFormService().getFormByUuid(SmartCardMetadata._Form.PSMART_IMMUNIZATION));

        }
    }

    private void saveObsData () {

        String cIVIL_STATUS=SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.mARITAL_STATUS;
        if (cIVIL_STATUS != null) {

        }
    }

    /**
     * Can't save patients unless they have required OpenMRS IDs
     */
    private PatientIdentifier generateOpenMRSID() {
        PatientIdentifierType openmrsIDType = Context.getPatientService().getPatientIdentifierTypeByUuid("dfacd928-0370-4315-99d7-6ec1c9f7ae76");
        String generated = Context.getService(IdentifierSourceService.class).generateIdentifier(openmrsIDType, "Registration");
        PatientIdentifier identifier = new PatientIdentifier(generated, openmrsIDType, Utils.getDefaultLocation());
        return identifier;
    }


}
