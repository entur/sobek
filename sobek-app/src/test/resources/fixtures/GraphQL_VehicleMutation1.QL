mutation {
  createOrUpdateVehicle(input: {
    name: {
      value: "City Bus 42"
      lang: "en"
    }
    description: {
      value: "Modern electric city bus"
      lang: "en"
    }
    transportType: {
        netexId: "AKT:VehicleType:124"
    }
    registrationNumber: "AB-12345"
    operationalNumber: "BUS-042"
    chassisNumber: "CHASSIS123456789"
    buildDate: "2024-01-15"
    registrationDate: "2024-02-01"
    dataOwnerRef: "NOG:Authority:cP4aPiJ7c39"
  })
}