-- A fleet can refer to an organisation (NeTEx TransportOrganisationRef), kept as the id of the organisation in the
-- organisation registry, whatever the type of the organisation is. A fleet can also have a contract number.
ALTER TABLE fleet
    ADD COLUMN transport_organisation_ref VARCHAR(255),
    ADD COLUMN contract_number VARCHAR(255);
