package com.revature.rideshare.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.rideshare.models.Trip;
import com.revature.rideshare.models.TripDTO;
import com.revature.rideshare.models.User;
import com.revature.rideshare.models.UserDTO;
import com.revature.rideshare.services.TripService;
import com.revature.rideshare.services.UserService;
import com.revature.rideshare.utilities.MockObjects;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(TripController.class)
public class TripControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TripService tripService;

    @MockBean
    private UserService userService;

    @Test
    public void testGetTrips() throws Exception {
        List<TripDTO> trips = new ArrayList<>();
        trips.add(new TripDTO(MockObjects.getTrip()));
        trips.add(new TripDTO(MockObjects.getTrip()));

        when(tripService.getTripsDTO()).thenReturn(trips);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    public void testGetTripById() throws Exception {
        Trip trip = MockObjects.getTrip();

        when(tripService.getTripById(1)).thenReturn(Optional.of(trip));

        mockMvc.perform(get("/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tripId").value(1));
    }

    @Test
    public void testAddTrip() throws Exception {
        Trip trip = MockObjects.getTrip();
        TripDTO tripDTO = new TripDTO(trip);

        when(tripService.addTrip(new Trip(tripDTO))).thenReturn(trip);

        mockMvc.perform(post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trip)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Grocery store"));
    }

    @Test
    public void testUpdateTrip() throws Exception {
        Trip trip = MockObjects.getTrip();
        TripDTO tripDTO = new TripDTO(trip);

        when(tripService.getTripById(trip.getTripId())).thenReturn(Optional.of(trip));
        when(tripService.updateTrip(new Trip(tripDTO))).thenReturn(trip);

        mockMvc.perform(put("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trip)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Grocery store"))
                .andReturn();
    }

    @Test
    public void testDeleteTripById() throws Exception {
        Trip trip = MockObjects.getTrip();
        String success = "Trip with id: " + trip.getTripId() + " was deleted";

        when(tripService.getTripById(trip.getTripId())).thenReturn(Optional.of(trip));
        when(tripService.deleteTripById(1)).thenReturn(success);

        mockMvc.perform(delete("/{id}", 1))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$").value(success));
    }

    @Test
    public void testGetTripsByRiderId() throws Exception {
        List<TripDTO> trips = new ArrayList<>();
        TripDTO trip1 = new TripDTO(MockObjects.getTrip());
        TripDTO trip2 = new TripDTO(MockObjects.getTrip());

        List<UserDTO> riders = new ArrayList<>();
        UserDTO rider = new UserDTO(MockObjects.getAdonis());
        riders.add(rider);

        trip1.setRiders(riders);
        trip2.setRiders(riders);

        trips.add(trip1);
        trips.add(trip2);

        when(tripService.getTripsByRiderIdDTO(rider.getUserId())).thenReturn(trips);

        mockMvc.perform(get(String.format("%s%s%s", "/rider", "?riderId=", rider.getUserId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    public void testUpdateTripRiders() throws Exception {
        Trip trip = MockObjects.getTrip();
        trip.setRiders(new ArrayList<>());
        User rider = MockObjects.getAdonis();

        when(tripService.getTripById(trip.getTripId())).thenReturn(Optional.of(trip));
        when(userService.getUserById(rider.getUserId())).thenReturn(Optional.of(rider));

        when(tripService.updateTrip(trip)).thenReturn(trip);

        mockMvc.perform(post(String.format(
                "%s%s%s%s",
                "/rider?tripId=",
                trip.getTripId(),
                "&riderId=",
                rider.getUserId()
        )))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.riders", hasSize(1)));
    }

    @Test
    public void testDeleteTripRider() throws Exception {
        Trip trip = MockObjects.getTrip();
        User rider = MockObjects.getAdonis();
        List<User> riders = new ArrayList<>();
        riders.add(rider);
        trip.setRiders(riders);

        when(tripService.getTripById(trip.getTripId())).thenReturn(Optional.of(trip));
        when(userService.getUserById(rider.getUserId())).thenReturn(Optional.of(rider));

        when(tripService.updateTrip(trip)).thenReturn(trip);

        mockMvc.perform(delete(String.format(
                "%s%s%s%s",
                "/rider?tripId=",
                trip.getTripId(),
                "&riderId=",
                rider.getUserId()
        )))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.riders", hasSize(0)));
    }

    @Test
    public void testGetTripsByDriverId() throws Exception {
        List<TripDTO> trips = new ArrayList<>();
        TripDTO trip1 = new TripDTO(MockObjects.getTrip());
        TripDTO trip2 = new TripDTO(MockObjects.getTrip());

        trips.add(trip1);
        trips.add(trip2);

        when(tripService.getTripsByDriverIdDTO(trip1.getDriver().getUserId())).thenReturn(trips);

        mockMvc.perform(get(String.format(
                "%s%s%s",
                "/driver",
                "?driverId=",
                trip1.getDriver().getUserId()
        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}
