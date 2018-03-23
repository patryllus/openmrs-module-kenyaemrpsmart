<%
    ui.decorateWith("kenyaemr", "standardPage", [ patient: currentPatient, layout: "sidebar" ])
    def menuItems = [
            [ label: "Back to home", iconProvider: "kenyaui", icon: "buttons/back.png", label: "Back to home", href: ui.pageLink("kenyaemr", "clinician/clinicianViewPatient", [ patient: currentPatient, patientId: currentPatient.patientId]) ]
    ]
%>
<style>
div.grid      { display:block; }
div.grid div  { float: left; height: 30px; }
div.column-one    { width: 300px; }
div.column-two    { width: 100px; }
div.column-three    { width: 120px; }
div.column-four      { width: 120px; }
div.column-five       { width: 120px; }
div.column-six       { width: 100px; }
div.clear     { clear: both; }
.col-header {font-weight: bold; font-size: 14px;}
div.section-title {
    color: black;
    font-weight: bold;
    display: block;
    width: 550px;
    float: left;
    font-size: 16px;
}
</style>

<div class="ke-page-sidebar">
    <div class="ke-panel-frame">
        ${ ui.includeFragment("kenyaui", "widget/panelMenu", [ heading: "Navigation", items: menuItems ]) }
    </div>
</div>

<div class="ke-page-content">
    <div class="ke-panel-frame">
        <div class="ke-panel-heading">P-Smart Data History </div>
        <div class="ke-panel-content">
            <div class="section-title">Statistics</div><div class="clear"></div>
            <div class="grid">
                <div class="column-one">&nbsp;</div>
                <div class="column-two col-header">Total Tests</div>
                <div class="column-three col-header">Total Immunizations</div>
                <div class="column-four col-header">&nbsp;</div>
                <div class="column-five col-header">&nbsp;</div>
            </div>
            <div class="clear"></div>
            <div class="grid">
                <div class="column-one">&nbsp;</div>
                <div class="column-two col-header">${summaries.totalTests}</div>
                <div class="column-three col-header">${summaries.totalImmunizations}</div>
                <div class="column-four col-header">&nbsp;</div>
                <div class="column-five col-header">&nbsp;</div>
            </div>
            <div class="clear"></div>

            <div class="section-title">Test History</div><div class="clear"></div>
            <% if (existingTests) { %>
            <div class="grid">
                <div class="column-one">&nbsp;</div>
                <div class="column-two col-header">Date Tested</div>
                <div class="column-three col-header">Result</div>
                <div class="column-four col-header">Test Type </div>
                <div class="column-five col-header">Test Strategy</div>
                <div class="column-six col-header">Test Facility</div>
            </div>
            <div class="clear"></div>
            <% existingTests.each { rel -> %>
            <div class="ke-stack-item">
                <div class="grid">
                    <div class="column-one">
                        &nbsp;
                    </div>
                    <div class="column-two">${rel.dateTested}</div>
                    <div class="column-three">${rel.result}</div>
                    <div class="column-four">${rel.type}</div>
                    <div class="column-five">${rel.strategy}</div>
                    <div class="column-six">${rel.facility}</div>
                </div>
                <div class="clear"></div>


            </div>
            <% } } else {%>
            No HIV Test found
            <% } %>

            <div class="section-title">Immunizations History</div><div class="clear"></div>
            <% if (existingImmunizations) { %>
            <div class="grid">
                <div class="column-one">&nbsp;</div>
                <div class="column-two col-header">Date</div>
                <div class="column-three col-header">Vaccination</div>
                <div class="column-four col-header">&nbsp;</div>
                <div class="column-five col-header">&nbsp;</div>
                <div class="column-six col-header">&nbsp;</div>
            </div>
            <div class="clear"></div>
            <% existingImmunizations.each { rel -> %>
            <div class="ke-stack-item">
                <div class="grid">
                    <div class="column-one">
                        &nbsp;
                    </div>
                    <div class="column-two">${rel.vaccinationDate}</div>
                    <div class="column-three">${rel.vaccination}</div>
                    <div class="column-four">&nbsp;</div>
                    <div class="column-five">&nbsp;</div>
                    <div class="column-six">&nbsp;</div>
                </div>
                <div class="clear"></div>


            </div>
            <% } } else {%>
            No Immunization data found
            <% } %>
        </div>

    </div>
</div>
