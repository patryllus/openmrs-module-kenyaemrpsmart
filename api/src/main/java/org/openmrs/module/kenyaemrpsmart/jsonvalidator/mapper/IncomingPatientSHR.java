package org.openmrs.module.kenyaemrpsmart.jsonvalidator.mapper;

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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
    String IMMUNIZATION_FORM_UUID = "b4f3859e-861c-4a63-bdff-eb7392030d47";

    public IncomingPatientSHR(String shr) {

        this.patientService = Context.getPatientService();
        this.personService = Context.getPersonService();
        this.obsService = Context.getObsService();
        this.administrationService = Context.getAdministrationService();
        this.conceptService = Context.getConceptService();
        this.encounterService = Context.getEncounterService();
        this.incomingSHR = shr;
    }

    public String processIncomingSHR() {
        createOrUpdatePatient();
        savePatientIdentifiers();
        savePersonAddresses();
        savePersonAttributes();
        String msg = "";
        try {
            patientService.savePatient(this.patient);
            msg = "Patient SHR processed successfully";
        } catch (Exception e) {
            e.printStackTrace();
            msg = "There was an error processing patient SHR";
        }

        return msg;
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
/*
    private Patient createTestPatient() {
        Patient patient = new Patient();
        PersonName pName = new PersonName();
        pName.setGivenName("Tom");
        pName.setMiddleName("E.");
        pName.setFamilyName("Patient");
        patient.addName(pName);
        PersonAddress pAddress = new PersonAddress();
        pAddress.setAddress1("123 My street");
        pAddress.setAddress2("Apt 402");
        pAddress.setCityVillage("Anywhere city");
        pAddress.setCountry("Some Country");
        Set<PersonAddress> pAddressList = patient.getAddresses();
        pAddressList.add(pAddress);
        patient.setAddresses(pAddressList);
        patient.addAddress(pAddress);
        patient.setDeathDate(new Date());
        patient.setBirthdate(new Date());
        patient.setBirthdateEstimated(true);
        patient.setGender("male");
        List<PatientIdentifierType> patientIdTypes = ps.getAllPatientIdentifierTypes();
        assertNotNull(patientIdTypes);
        PatientIdentifier patientIdentifier = new PatientIdentifier();
        patientIdentifier.setIdentifier("123-0");
        patientIdentifier.setIdentifierType(patientIdTypes.get(0));
        patientIdentifier.setLocation(new Location(1));
        patientIdentifier.setPreferred(true);
        Set<PatientIdentifier> patientIdentifiers = new TreeSet<PatientIdentifier>();
        patientIdentifiers.add(patientIdentifier);
        patient.setIdentifiers(patientIdentifiers);

        ps.savePatient(patient);
        return patient;
    }*/
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

        }
    }

    private void saveImmunization () {
        for (int i = 0; i< SHRUtils.getSHR(this.incomingSHR).hIV_TEST.length;i++){

            String name = SHRUtils.getSHR(this.incomingSHR).iMMUNIZATION[i].nAME;
            String dateAministered = SHRUtils.getSHR(this.incomingSHR).iMMUNIZATION[i].dATE_ADMINISTERED;
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

/*        String locationIdString = "1";//JsonUtils.readAsString(payload, "$['encounter']['encounter.location_id']");
        Location location = null;
        int locationId;

        if(locationIdString != null){
            locationId = Integer.parseInt(locationIdString);
            location = Context.getLocationService().getLocation(locationId);
        }*/

        String generated = Context.getService(IdentifierSourceService.class).generateIdentifier(openmrsIDType, "Registration");
        PatientIdentifier identifier = new PatientIdentifier(generated, openmrsIDType, Utils.getDefaultLocation());
        return identifier;
    }


}
