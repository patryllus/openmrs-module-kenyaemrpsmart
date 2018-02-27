package org.openmrs.module.kenyaemrpsmart;

import org.openmrs.BaseOpenmrsData;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.sql.Timestamp;

public class PsmartStore extends BaseOpenmrsData {
    @Id
    @GeneratedValue
    private Integer id;

    private String uuid;

    @Lob
    private String shr;

    @Column(name = "date_created", nullable = false)
    private Timestamp dateCreated;

    private String status;

    @Column(name = "status_date", nullable = false)
    private Timestamp statusDate;

    @Lob
    private String addendum;

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getShr() {
        return shr;
    }

    public void setShr(String shr) {
        this.shr = shr;
    }

    @Override
    public Timestamp getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Timestamp dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getStatusDate() {
        return statusDate;
    }

    public void setStatusDate(Timestamp statusDate) {
        this.statusDate = statusDate;
    }

    public String getAddendum() {
        return addendum;
    }

    public void setAddendum(String addendum) {
        this.addendum = addendum;
    }

    @Override
    public String toString() {
        return "PsmartStore{" +
                "id=" + id +
                ", uuid='" + uuid + '\'' +
                ", shr='" + shr + '\'' +
                ", dateCreated=" + dateCreated +
                ", status='" + status + '\'' +
                ", statusDate=" + statusDate +
                ", addendum='" + addendum + '\'' +
                '}';
    }
}
