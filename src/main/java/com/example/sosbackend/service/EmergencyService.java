package com.example.sosbackend.service;

import java.util.ArrayList;
import java.util.List;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.springframework.stereotype.Service;

import com.example.sosbackend.model.EmergencyServicesModel;
import com.example.sosbackend.repository.EmergencyServiceRepository;
import com.example.sosbackend.dto.CreateEmergencyServiceRequest;
import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.builder.DSL.point;
import jakarta.transaction.Transactional;

@Service
public class EmergencyService {
  private final EmergencyServiceRepository emergencyServiceRepository;

  public EmergencyService(EmergencyServiceRepository emergencyServiceRepository) {
    this.emergencyServiceRepository = emergencyServiceRepository;
  }

  @Transactional
  public List<EmergencyServicesModel> createEmergencyServices(List<CreateEmergencyServiceRequest> requests) {
    List<EmergencyServicesModel> emergencyServices = new ArrayList<>();

    for (CreateEmergencyServiceRequest request : requests) {
      EmergencyServicesModel emergencyService = new EmergencyServicesModel();

      // Set all fields for the model
      emergencyService.setName(request.getName());
      emergencyService.setType(request.getType());
      emergencyService.setCallcode(request.getCallcode());
      emergencyService.setLocation(request.getLocation());

      // make the coordinate with the right coordinate reference system
      Point<G2D> coordinates = point(CoordinateReferenceSystems.WGS84,
          g(request.getLongitude(), request.getLatitude()));

      emergencyService.setCoordinates(coordinates);

      emergencyServices.add(emergencyService);

    }

    // finally save to the database
    return emergencyServiceRepository.saveAll(emergencyServices);

  }

  public List<EmergencyServicesModel> findNearbyEmergencyServices(double longitude, double latitude, double radius,
      int limit, int page, String type) {

    List<EmergencyServicesModel> emergencyServices = new ArrayList<EmergencyServicesModel>();

    // page offset
    int offset = ((page - 1) * limit);

    switch (type.toLowerCase()) {
      case "policestations":
        for (EmergencyServicesModel policeStation : emergencyServiceRepository.findNearbyPoliceStations(longitude,
            latitude, radius, limit, offset)) {
          emergencyServices.add(policeStation);
        }
        break;
      case "hospitals":
        for (EmergencyServicesModel hospital : emergencyServiceRepository.findNearbyHospitals(longitude, latitude,
            radius, limit, offset)) {
          emergencyServices.add(hospital);
        }
        break;
      case "firestations":

        for (EmergencyServicesModel fireStation : emergencyServiceRepository.findNearbyFireStations(longitude, latitude,
            radius, limit, offset)) {
          emergencyServices.add(fireStation);
        }

        break;
      default:

        for (EmergencyServicesModel emergencyService : emergencyServiceRepository.findNearbyEmergencyServices(longitude,
            latitude,
            radius, limit, offset)) {
          emergencyServices.add(emergencyService);
        }

    }

    return emergencyServices;
    // .orElseThrow(() -> new ResourceNotFoundException("No nearby emergency
    // services found for this coordinate"));
  }

}
