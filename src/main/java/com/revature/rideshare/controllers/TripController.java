package com.revature.rideshare.controllers;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.revature.rideshare.models.Trip;
import com.revature.rideshare.models.TripDTO;
import com.revature.rideshare.models.User;
import com.revature.rideshare.services.TripService;
import com.revature.rideshare.services.UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@CrossOrigin
@Validated
@Api(tags = {"Trips"})
public class TripController {
    private TripService tripService;
    private UserService userService;

    @Autowired
    public TripController(TripService tripService, UserService userService) {
        super();
        this.tripService = tripService;
        this.userService = userService;
    }

    /**
     * HTTP GET method (/trips)
     *
     * @param offset optional: how many entries to skip over.
     * @return a list of all the trips along with a 200 code.
     */
    @ApiOperation(value = "Return all trips", tags = {"Trip"})
    @GetMapping
    public ResponseEntity<List<TripDTO>> getTrips(
            @PositiveOrZero
            @RequestParam(name = "offset", required = false)
                    Integer offset
    ) {
        List<TripDTO> trips;
        if (offset != null) {
            trips = tripService.getTripsDTO(offset);
        } else {
            trips = tripService.getTripsDTO();
        }

        if (!trips.isEmpty()) return ResponseEntity.ok(trips);

        return ResponseEntity.noContent().build();
    }

    /**
     * HTTP GET method (/trips/{tripId})
     *
     * @param id represent the Trip's ID.
     * @return a trip that matches the ID along with a 200 code.
     */
    @ApiOperation(value = "Return trip by ID", tags = {"Trip"})
    @GetMapping("/{id}")
    public ResponseEntity<TripDTO> getTripById(
            @Positive
            @PathVariable("id")
                    int id
    ) {
        Optional<Trip> trip = tripService.getTripById(id);

        if (trip.isPresent()) return ResponseEntity.ok(new TripDTO(trip.get()));

        return ResponseEntity.noContent().build();
    }

    /**
     * HTTP POST method (/trips)
     * @param tripDTO represents the new Trip object being sent.
     * @return the newly created object along with a 201 code.
     */
    @ApiOperation(value = "Adds a new trip")
    @PostMapping
    public ResponseEntity<TripDTO> addTrip(@Valid @RequestBody TripDTO tripDTO) {
        Optional<Trip> newTrip = tripService.getTripById(tripDTO.getTripId());

        if (newTrip.isPresent()) return ResponseEntity.badRequest().build();

        return ResponseEntity.status(201).body(new TripDTO(tripService.addTrip(new Trip(tripDTO))));
    }

    /**
     * HTTP PUT method (/trips)
     *
     * @param tripDTO represents the updated Trip object being sent.
     * @return the newly updated Trip object along with a 201 code.
     */
    @ApiOperation(value = "Updates a trip", tags = {"Trip"})
    @PutMapping
    public ResponseEntity<TripDTO> updateTrip(@Valid @RequestBody TripDTO tripDTO) {
        Optional<Trip> existingTrip = tripService.getTripById(tripDTO.getTripId());

        if (existingTrip.isPresent()) return ResponseEntity.status(201).body(new TripDTO(tripService.updateTrip(new Trip(tripDTO))));

        return ResponseEntity.badRequest().build();
    }

    /**
     * HTTP DELETE method (/trips/{id})
     *
     * @param id represents the Trip's ID.
     * @return a string that says which Trip was deleted.
     */
    @ApiOperation(value = "Deletes a trip by ID", tags = {"Trip"})
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTripById(
            @Positive
            @PathVariable("id")
                    int id
    ) {
        Optional<Trip> trip = tripService.getTripById(id);

        if (trip.isPresent()) {
            String message = tripService.deleteTripById(id);

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(message);
        }

        return ResponseEntity.notFound().build();
    }

    /**
     * HTTP GET method (/trips/rider)
     *
     * @param riderId represents the rider User's ID.
     * @param offset optional: how many entries to skip over.
     * @return a list of trips matching the rider's ID.
     */
    @ApiOperation(value = "Return all trips", tags = {"Trip"})
    @GetMapping("/rider")
    public ResponseEntity<List<TripDTO>> getTripsByRiderId(
            @Positive
            @RequestParam(name = "riderId")
                    Integer riderId,

            @Positive
            @RequestParam(name = "offset", required = false)
                    Integer offset
    ) {
        List<TripDTO> trips;
        if (offset != null) {
            trips = tripService.getTripsByRiderIdDTO(riderId, offset);
        } else {
            trips = tripService.getTripsByRiderIdDTO(riderId);
        }

        if (!trips.isEmpty()) return ResponseEntity.ok(trips);

        return ResponseEntity.noContent().build();
    }

    /**
     * HTTP POST method (/trips/rider)
     *
     * @param tripId represents the ID of the Trip the person is joining.
     * @param riderId represents the ID of the User joining the trip.
     * @return the newly updated Trip object.
     */
    @ApiOperation(value = "Adds a user to a trip", tags = {"Rider", "Trip"})
    @PostMapping("/rider")
    public ResponseEntity<TripDTO> updateTripRiders(
            @Positive
            @RequestParam(name = "tripId")
                    Integer tripId,

            @Positive
            @RequestParam(name = "riderId")
                    Integer riderId
    ) {
        Optional<Trip> existingTrip = tripService.getTripById(tripId);
        Optional<User> existingUser = userService.getUserById(riderId);

        if (existingTrip.isPresent() && existingUser.isPresent()) {
            Trip trip = existingTrip.get();
            List<User> riders = trip.getRiders();
            riders.add(existingUser.get());
            int numberOfSeats = trip.getAvailableSeats() - 1;
            if (numberOfSeats < 0) return ResponseEntity.status(HttpStatus.CONFLICT).build();
            trip.setAvailableSeats(numberOfSeats);
            tripService.updateTrip(trip);
            return ResponseEntity.accepted().body(new TripDTO(trip));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * HTTP DELETE method (/trips/rider)
     *
     * @param tripId represents the ID of the Trip the person is joining.
     * @param riderId represents the ID of the User joining the trip.
     * @return the newly updated Trip object.
     */
    @ApiOperation(value = "Removes a user from a trip", tags = {"Rider", "Trip"})
    @DeleteMapping("/rider")
    public ResponseEntity<TripDTO> deleteTripRider(
            @Positive
            @RequestParam(name = "tripId")
                    Integer tripId,

            @Positive
            @RequestParam(name = "riderId")
                    Integer riderId
    ) {
        Optional<Trip> existingTrip = tripService.getTripById(tripId);
        Optional<User> existingUser = userService.getUserById(riderId);

        if (existingTrip.isPresent() && existingUser.isPresent()) {
            Trip trip = existingTrip.get();
            List<User> riders = trip.getRiders();
            int riderLength = riders.size();
            riders.remove(existingUser.get());
            if (riders.size() < riderLength) {
                trip.setAvailableSeats(trip.getAvailableSeats() + 1);
                tripService.updateTrip(trip);
                return ResponseEntity.accepted().body(new TripDTO(trip));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * HTTP GET method (/trips/driver)
     *
     * @param driverId represents the ID of the User designated as driver.
     * @param riderId optional: filters only the trips that also have that rider.
     * @param offset optional: how many entries to skip over.
     * @return a list of trips matching the driver's ID.
     */
    @ApiOperation(value = "Return all trips matching a driver's ID", tags = {"Trip", "Driver"})
    @GetMapping("/driver")
    public ResponseEntity<List<TripDTO>> getTripsByDriverId(
            @Positive
            @RequestParam(name = "driverId")
                    Integer driverId,

            @Positive
            @RequestParam(name = "riderId", required = false)
                    Integer riderId,

            @PositiveOrZero
            @RequestParam(name = "offset", required = false)
                    Integer offset
    ) {
        List<TripDTO> trips;
        if (riderId == null) {
            if (offset != null) {
                trips = tripService.getTripsByDriverIdDTO(driverId, offset);
            } else {
                trips = tripService.getTripsByDriverIdDTO(driverId);
            }
        } else {
            if (offset != null) {
                trips = tripService.getTripsByDriverIdAndByRiderIdDTO(driverId, riderId, offset);
            } else {
                trips = tripService.getTripsByDriverIdAndByRiderIdDTO(driverId, riderId);
            }
        }

        if (!trips.isEmpty()) return ResponseEntity.ok(trips);

        return ResponseEntity.noContent().build();
    }
}
