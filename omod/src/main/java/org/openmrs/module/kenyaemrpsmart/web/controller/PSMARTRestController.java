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
package org.openmrs.module.kenyaemrpsmart.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrpsmart.jsonvalidator.mapper.PatientSHR;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

/**
 * The main controller.
 */
@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/smartcard")
public class PSMARTRestController extends BaseRestController {

	protected final Log log = LogFactory.getLog(getClass());

	@RequestMapping(method = RequestMethod.POST, value = "/receiveshr")
	@ResponseBody
	public Object receiveSHR(WebRequest request) {
		int patientID = request.getParameter("patientID") != null? Integer.parseInt(request.getParameter("patientID")): 0;
		if (patientID != 0) {
			PatientSHR shr = new PatientSHR(patientID);
			return new SimpleObject().add("sessionId", request.getSessionId()).add("authenticated", Context.isAuthenticated()).add("identification", shr.patientIdentification().toString());

		}
		return new SimpleObject().add("sessionId", request.getSessionId()).add("authenticated", Context.isAuthenticated()).add("identification", "No patient id specified in the request: Got this: => " + request.getParameter("patientID"));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/prepareshr")
	@ResponseBody
	public Object prepareSHR(WebRequest request) {
		return new SimpleObject().add("sessionId", request.getSessionId()).add("authenticated", Context.isAuthenticated());
	}

	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController#getNamespace()
	 */

	@Override
	public String getNamespace() {
		return "v1/kenyaemrpsmart";
	}

}
