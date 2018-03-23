package org.openmrs.module.kenyaemrpsmart.jsonvalidator.mapper;

import org.openmrs.Concept;
import org.openmrs.api.ConceptService;


public class SHRConcepts {
    static ConceptService conceptService = org.openmrs.api.context.Context.getConceptService();
    static Concept BCG = conceptService.getConcept(886);
    static Concept OPV = conceptService.getConcept(783);
    static Concept IPV = conceptService.getConcept(1422);
    static Concept DPT = conceptService.getConcept(781);
    static Concept PCV = conceptService.getConcept(162342);
    static Concept ROTA = conceptService.getConcept(83531);
    static Concept MEASLESorRUBELLA = conceptService.getConcept(162586);
    static Concept MEASLES = conceptService.getConcept(36);
    static Concept YELLOW_FEVER = conceptService.getConcept(5864);
}
