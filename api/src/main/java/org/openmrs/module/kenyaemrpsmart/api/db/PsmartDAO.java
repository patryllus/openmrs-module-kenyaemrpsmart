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
package org.openmrs.module.kenyaemrpsmart.api.db;

import org.openmrs.module.kenyaemrpsmart.PsmartStore;
import org.openmrs.module.kenyaemrpsmart.api.PsmartService;

import java.util.Date;
import java.util.List;

/**
 *  Database methods for {@link PsmartService}.
 */
public interface PsmartDAO {

    public PsmartStore savePsmartStoreObject(PsmartStore psmartStore);

    public PsmartStore getPsmartStoreObject(Integer id);

    public PsmartStore getPsmartStoreByUuid(String uuid);

    public List<PsmartStore> getAllPsmartStoreObjects();

    /**
     *
     * @param from date
     * @param to date
     * @param operation - created, collected
     * @return List<PsmartStore>
     */
    public List<PsmartStore> objectsProcessedSinceDate(Date from, Date to, String operation);
}