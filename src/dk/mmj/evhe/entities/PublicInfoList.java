package dk.mmj.evhe.entities;

import java.util.List;

/**
 * Simple entity for sending list of public information entities
 */
@SuppressWarnings("unused, JavaDocs")
public class PublicInfoList {
    private List<PublicInformationEntity> informationEntities;

    public PublicInfoList() {
    }

    public PublicInfoList(List<PublicInformationEntity> informationEntities) {
        this.informationEntities = informationEntities;
    }

    public List<PublicInformationEntity> getInformationEntities() {
        return informationEntities;
    }

    public void setInformationEntities(List<PublicInformationEntity> informationEntities) {
        this.informationEntities = informationEntities;
    }
}
