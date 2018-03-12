/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.kenyaemrpsmart.metadata;

import org.openmrs.PatientIdentifierType;
import org.openmrs.module.metadatadeploy.bundle.AbstractMetadataBundle;
import org.springframework.stereotype.Component;

import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.patientIdentifierType;

/**
 * Metadata constants
 */
@Component
public class SmartCardMetadata extends AbstractMetadataBundle {

	public static final String MODULE_ID = "kenyaemrpsmart";

	public static final class _PatientIdentifierType {
		public static final String SMART_CARD_SERIAL_NUMBER = "8f842498-1c5b-11e8-accf-0ed5f89f718b";
		public static final String HTS_NUMBER = "e6af3782-1cb3-11e8-accf-0ed5f89f718b";
		public static final String GODS_NUMBER = "9aedb9ae-1cbd-11e8-accf-0ed5f89f718b";
	}

	@Override
	public void install() throws Exception {
		install(patientIdentifierType("Smart Card Serial Number", "P-SMART Serial Number", null, null,
				null, PatientIdentifierType.LocationBehavior.NOT_USED, false, _PatientIdentifierType.SMART_CARD_SERIAL_NUMBER));

		install(patientIdentifierType("HTS Number", "Number assigned to clients when tested for HIV", null, null,
				null, PatientIdentifierType.LocationBehavior.NOT_USED, false, _PatientIdentifierType.HTS_NUMBER));

		install(patientIdentifierType("GODS Number", "Number assigned by MPI", null, null,
				null, PatientIdentifierType.LocationBehavior.NOT_USED, false, _PatientIdentifierType.GODS_NUMBER));


	}
}
