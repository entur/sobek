package org.rutebanken.sobek.exporter;

import org.rutebanken.netex.model.ObjectFactory;
import org.rutebanken.netex.model.PublicationDeliveryStructure;
import org.rutebanken.netex.model.ResourceFrame;
import org.rutebanken.sobek.netex.id.ValidPrefixList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PublicationDeliveryCreator {

    private static final Logger logger = LoggerFactory.getLogger(PublicationDeliveryCreator.class);
    private final String publicationDeliveryId;
    private final ValidPrefixList validPrefixList;

    public PublicationDeliveryCreator(@Value("${netex.profile.version:1.12:NO-NeTEx-vehicle:1.4}") String publicationDeliveryId,
                                      ValidPrefixList validPrefixList) {
        this.publicationDeliveryId = publicationDeliveryId;
        this.validPrefixList = validPrefixList;
    }


    public PublicationDeliveryStructure createPublicationDelivery() {
        return new PublicationDeliveryStructure()
                .withVersion(publicationDeliveryId)
                .withPublicationTimestamp(LocalDateTime.now())
                .withParticipantRef(validPrefixList.getValidNetexPrefix());
    }

    public PublicationDeliveryStructure createPublicationDelivery(org.rutebanken.netex.model.CompositeFrame compositeFrame) {
        PublicationDeliveryStructure publicationDeliveryStructure = createPublicationDelivery();
        publicationDeliveryStructure.withDataObjects(
                new PublicationDeliveryStructure.DataObjects()
                        .withCompositeFrameOrCommonFrame(new ObjectFactory().createCompositeFrame(compositeFrame))
        );

        logger.info("Returning publication delivery {} with composite frame", publicationDeliveryStructure);
        return publicationDeliveryStructure;
    }

    public PublicationDeliveryStructure createPublicationDelivery(ResourceFrame netexResourceFrame
    ) {
        PublicationDeliveryStructure publicationDeliveryStructure = createPublicationDelivery();

        publicationDeliveryStructure.withDataObjects
                (
                        new PublicationDeliveryStructure.DataObjects()
                                .withCompositeFrameOrCommonFrame(new ObjectFactory().createResourceFrame(netexResourceFrame))
                );

        logger.info("Returning publication delivery {} with resource frame", publicationDeliveryStructure);
        return publicationDeliveryStructure;
    }

    public PublicationDeliveryStructure createPublicationDelivery(org.rutebanken.netex.model.ServiceFrame serviceFrame,
                                                                  ResourceFrame netexResourceFrame
    ) {
        PublicationDeliveryStructure publicationDeliveryStructure = createPublicationDelivery();

        publicationDeliveryStructure.withDataObjects
                (
                        new PublicationDeliveryStructure.DataObjects()
                                .withCompositeFrameOrCommonFrame(new ObjectFactory().createServiceFrame(serviceFrame))
                                .withCompositeFrameOrCommonFrame(new ObjectFactory().createResourceFrame(netexResourceFrame))
                );

        logger.info("Returning publication delivery {} with site frame and  service frame", publicationDeliveryStructure);
        return publicationDeliveryStructure;
    }

    public PublicationDeliveryStructure createPublicationDelivery(org.rutebanken.netex.model.SiteFrame siteFrame,
                                                                  org.rutebanken.netex.model.ServiceFrame serviceFrame,
                                                                  ResourceFrame netexResourceFrame
    ) {
        PublicationDeliveryStructure publicationDeliveryStructure = createPublicationDelivery();

        publicationDeliveryStructure.withDataObjects
                (
                        new PublicationDeliveryStructure.DataObjects()
                                .withCompositeFrameOrCommonFrame(new ObjectFactory().createServiceFrame(serviceFrame))
                                .withCompositeFrameOrCommonFrame(new ObjectFactory().createSiteFrame(siteFrame))
                                .withCompositeFrameOrCommonFrame(new ObjectFactory().createResourceFrame(netexResourceFrame))
                );

        logger.info("Returning publication delivery {} with site frame and  service frame", publicationDeliveryStructure);
        return publicationDeliveryStructure;
    }
}
