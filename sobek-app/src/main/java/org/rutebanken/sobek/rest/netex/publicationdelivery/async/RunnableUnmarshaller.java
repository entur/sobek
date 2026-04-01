/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package org.rutebanken.sobek.rest.netex.publicationdelivery.async;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.rutebanken.netex.model.Parking;
import org.rutebanken.netex.model.StopPlace;
import org.rutebanken.netex.model.Vehicle;
import org.rutebanken.netex.model.VehicleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

public class RunnableUnmarshaller implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RunnableUnmarshaller.class);

    public static final Vehicle POISON_VEHICLE = new Vehicle().withId("-100");
    public static final VehicleType POISON_VEHICLE_TYPE = new VehicleType().withId("-100");

    private final InputStream inputStream;
    private final Unmarshaller unmarshaller;
    private final UnmarshalResult unmarshalResult;

    public RunnableUnmarshaller(InputStream inputStream, Unmarshaller unmarshaller, UnmarshalResult unmarshalResult) {
        this.inputStream = inputStream;
        this.unmarshaller = unmarshaller;
        this.unmarshalResult = unmarshalResult;
    }

    @Override
    public void run() {
        final XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
        AtomicInteger vehicles = new AtomicInteger();
        AtomicInteger vehicleTypes = new AtomicInteger();

        final XMLEventReader xmlEventReader;
        try {

            xmlEventReader = xmlInputFactory.createXMLEventReader(inputStream);

            while (xmlEventReader.hasNext()) {

                XMLEvent xmlEvent = xmlEventReader.peek();

                logger.trace("XmlEvent {}", xmlEvent);
                if (xmlEvent.isStartElement()) {
                    StartElement startElement = xmlEvent.asStartElement();
                    String localPartOfName = startElement.getName().getLocalPart();

                    if (localPartOfName.equals("Vehicle")) {
                        Vehicle vehicle = unmarshaller.unmarshal(xmlEventReader, Vehicle.class).getValue();
                        vehicles.incrementAndGet();
                        unmarshalResult.getVehiclesQueue().put(vehicle);

                        if (vehicles.get() % 200 == 0) {
                            logger.info("Unmarshalled vehicle number {}", vehicles.get());
                        }
                        continue;
                    }

                    if (localPartOfName.equals("VehicleType")) {
                        VehicleType vehicleType = unmarshaller.unmarshal(xmlEventReader, VehicleType.class).getValue();
                        vehicleTypes.incrementAndGet();
                        unmarshalResult.getVehicleTypesQueue().put(vehicleType);

                        if (vehicleTypes.get() % 20 == 0) {
                            logger.info("Unmarshalled vehicleType number {}", vehicleTypes.get());
                        }
                        continue;
                    }
                } else if (xmlEvent.isEndElement()) {
                    EndElement endElement = xmlEvent.asEndElement();
                    String localPartOfName = endElement.getName().getLocalPart();
                    if (localPartOfName.equals("vehicles")) {
                        logger.info("End of vehicles in incoming XML. Counter ended at {}. Adding poison pill to the queue.", vehicles.get());
                        unmarshalResult.getVehiclesQueue().put(POISON_VEHICLE);
                    }
                    if (localPartOfName.equals("vehicleTypes")) {
                        logger.info("End of vehicleTypes in incoming XML. Counter ended at {}. Adding poison pill to the queue.", vehicleTypes.get());
                        unmarshalResult.getVehicleTypesQueue().put(POISON_VEHICLE_TYPE);
                    }
                }
                xmlEventReader.next();
            }
        } catch (XMLStreamException | InterruptedException | JAXBException e) {

            logger.error("Could not read netex from events. Stopping. " + e.getMessage(), e);
            try {
                unmarshalResult.getVehiclesQueue().put(POISON_VEHICLE);
                unmarshalResult.getVehicleTypesQueue().put(POISON_VEHICLE_TYPE);
            } catch (InterruptedException e2) {
                logger.warn("Interrupted when adding poison vehicle to queue", e2);
            }
        }
        try {
            // Do this regardless of processing above. If vehicleType is empty, make sure threads can exit.
            // After all, the queue is blocking.
            unmarshalResult.getVehicleTypesQueue().put(POISON_VEHICLE_TYPE);
            unmarshalResult.getVehiclesQueue().put(POISON_VEHICLE);
        } catch (InterruptedException e) {
            // Intentionally empty
        }

        logger.info("Unmarshalling thread finished after {} vehicles, {} vehicleTypes.", vehicles.get(), vehicleTypes.get());
    }

}
