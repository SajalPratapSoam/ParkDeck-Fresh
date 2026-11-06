package com.parkdeck;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkdeck.repository.BookingRepository;
import com.parkdeck.repository.ParkingSpaceRepository;
import com.parkdeck.repository.UserRepository;
import com.parkdeck.repository.VehicleRepository;
import com.parkdeck.repository.WishlistRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class MarketplaceApiTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper json;
    @Autowired
    BookingRepository bookings;
    @Autowired
    WishlistRepository wishlist;
    @Autowired
    VehicleRepository vehicles;
    @Autowired
    ParkingSpaceRepository spaces;
    @Autowired
    UserRepository users;

    @BeforeEach
    void clean() {
        bookings.deleteAll();
        wishlist.deleteAll();
        vehicles.deleteAll();
        spaces.deleteAll();
        users.deleteAll();
    }

    @Test
    void registerLoginAndRejectsDuplicateEmail() throws Exception {
        register("Meera Iyer", "meera@example.com", "Parkdeck1", "DRIVER");
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("Meera Iyer", "meera@example.com", "Parkdeck1", "DRIVER")))
                .andExpect(status().isConflict());

        MvcResult login = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"meera@example.com","password":"Parkdeck1"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist())
                .andReturn();
        String token = json.readTree(login.getResponse().getContentAsString()).get("token").asText();

        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("meera@example.com"))
                .andExpect(jsonPath("$.role").value("DRIVER"));

        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"meera@example.com","password":"wrong-pass"}
                                """))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("Short", "bad-email", "short", "DRIVER")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void hostCreatesListingAndPublicCanFilterIt() throws Exception {
        register("Aarav Mehta", "aarav@example.com", "Parkdeck1", "HOST");
        register("Meera Iyer", "meera@example.com", "Parkdeck1", "DRIVER");
        String host = login("aarav@example.com");
        String driver = login("meera@example.com");

        mvc.perform(post("/api/spaces").header("Authorization", "Bearer " + driver)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(spaceJson("Driver bay", "Noida", "40.00")))
                .andExpect(status().isForbidden());

        MvcResult created = mvc.perform(post("/api/spaces").header("Authorization", "Bearer " + host)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(spaceJson("Pari Chowk shade", "Greater Noida", "45.00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hostName").value("Aarav Mehta"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andReturn();
        long spaceId = json.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(get("/api/spaces").param("city", "greater noida").param("maxPrice", "50").param("parkingType", "COVERED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Pari Chowk shade"));

        mvc.perform(get("/api/spaces/{id}", spaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hourlyPrice").value(45.00));

        register("Other Host", "other@example.com", "Parkdeck1", "HOST");
        String other = login("other@example.com");
        mvc.perform(put("/api/spaces/{id}", spaceId).header("Authorization", "Bearer " + other)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(spaceJson("Stolen title", "Delhi", "10.00")))
                .andExpect(status().isForbidden());

        mvc.perform(delete("/api/spaces/{id}", spaceId).header("Authorization", "Bearer " + host))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/spaces/{id}", spaceId)).andExpect(status().isNotFound());
        mvc.perform(get("/api/spaces/{id}", spaceId).header("Authorization", "Bearer " + host))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void bookingPriceOverlapAndOwnership() throws Exception {
        register("Aarav Mehta", "aarav@example.com", "Parkdeck1", "HOST");
        register("Meera Iyer", "meera@example.com", "Parkdeck1", "DRIVER");
        register("Kabir Shah", "kabir@example.com", "Parkdeck1", "DRIVER");
        String host = login("aarav@example.com");
        String meera = login("meera@example.com");
        String kabir = login("kabir@example.com");
        long spaceId = createSpace(host, "80.00");

        LocalDateTime start = LocalDateTime.now(ZONE).plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusMinutes(90);

        mvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(spaceId, start, end)))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/bookings").header("Authorization", "Bearer " + host)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(spaceId, start, end)))
                .andExpect(status().isForbidden());

        MvcResult booked = mvc.perform(post("/api/bookings").header("Authorization", "Bearer " + meera)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(spaceId, start, end)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hourlyRateSnapshot").value(80.00))
                .andExpect(jsonPath("$.serviceFee").value(6.00))
                .andExpect(jsonPath("$.totalPrice").value(126.00))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andReturn();
        long bookingId = json.readTree(booked.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(post("/api/bookings").header("Authorization", "Bearer " + kabir)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(spaceId, start.plusMinutes(30), end.plusMinutes(30))))
                .andExpect(status().isConflict());

        mvc.perform(post("/api/bookings").header("Authorization", "Bearer " + kabir)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(spaceId, end, end.plusHours(1))))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/bookings").header("Authorization", "Bearer " + meera)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(spaceId, start.minusDays(5), start.minusDays(5).plusHours(1))))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/bookings").header("Authorization", "Bearer " + meera)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(spaceId, start.withHour(5), start.withHour(7))))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/bookings").header("Authorization", "Bearer " + meera)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(spaceId, end, start)))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/bookings/{id}", bookingId).header("Authorization", "Bearer " + kabir))
                .andExpect(status().isNotFound());
        mvc.perform(patch("/api/bookings/{id}/cancel", bookingId).header("Authorization", "Bearer " + kabir))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/bookings/my").header("Authorization", "Bearer " + meera))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mvc.perform(patch("/api/bookings/{id}/cancel", bookingId).header("Authorization", "Bearer " + meera))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        mvc.perform(get("/api/bookings/my").header("Authorization", "Bearer " + meera))
                .andExpect(jsonPath("$[0].status").value("CANCELLED"));

        mvc.perform(get("/api/host/summary").header("Authorization", "Bearer " + host))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedBookings").value(1))
                .andExpect(jsonPath("$.cancelledBookings").value(1))
                .andExpect(jsonPath("$.earnings").value(84.00));
        mvc.perform(get("/api/host/summary").header("Authorization", "Bearer " + meera))
                .andExpect(status().isForbidden());
    }

    @Test
    void wishlistProfileAndVehiclesStayWithTheOwner() throws Exception {
        register("Aarav Mehta", "aarav@example.com", "Parkdeck1", "HOST");
        register("Meera Iyer", "meera@example.com", "Parkdeck1", "DRIVER");
        register("Kabir Shah", "kabir@example.com", "Parkdeck1", "DRIVER");
        String host = login("aarav@example.com");
        String meera = login("meera@example.com");
        String kabir = login("kabir@example.com");
        long spaceId = createSpace(host, "50.00");

        mvc.perform(post("/api/wishlist/{id}", spaceId)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/wishlist/{id}", spaceId).header("Authorization", "Bearer " + meera))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test bay"));
        mvc.perform(post("/api/wishlist/{id}", spaceId).header("Authorization", "Bearer " + meera))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/wishlist").header("Authorization", "Bearer " + kabir))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/wishlist").header("Authorization", "Bearer " + meera))
                .andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(delete("/api/wishlist/{id}", spaceId).header("Authorization", "Bearer " + meera))
                .andExpect(status().isNoContent());

        mvc.perform(put("/api/profile").header("Authorization", "Bearer " + meera)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Meera Nair","phone":"+919800000000","newPassword":"Newpass12","currentPassword":"wrong"}
                                """))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/profile").header("Authorization", "Bearer " + meera)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Meera Nair","phone":"+919800000000"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Meera Nair"))
                .andExpect(jsonPath("$.role").value("DRIVER"));

        MvcResult vehicle = mvc.perform(post("/api/vehicles").header("Authorization", "Bearer " + meera)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vehicleType":"Sedan","registrationNumber":"dl8c aa1001","nickname":"Office"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.registrationNumber").value("DL8CAA1001"))
                .andReturn();
        long vehicleId = json.readTree(vehicle.getResponse().getContentAsString()).get("id").asLong();
        mvc.perform(delete("/api/vehicles/{id}", vehicleId).header("Authorization", "Bearer " + kabir))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/vehicles").header("Authorization", "Bearer " + meera))
                .andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(delete("/api/vehicles/{id}", vehicleId).header("Authorization", "Bearer " + meera))
                .andExpect(status().isNoContent());
    }

    private void register(String name, String email, String password, String role) throws Exception {
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson(name, email, password, role)))
                .andExpect(status().isCreated());
    }

    private String login(String email) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"Parkdeck1"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();
        return json.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private long createSpace(String hostToken, String price) throws Exception {
        MvcResult created = mvc.perform(post("/api/spaces").header("Authorization", "Bearer " + hostToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(spaceJson("Test bay", "Noida", price)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode body = json.readTree(created.getResponse().getContentAsString());
        return body.get("id").asLong();
    }

    private String userJson(String name, String email, String password, String role) {
        return """
                {"fullName":"%s","email":"%s","password":"%s","role":"%s"}
                """.formatted(name, email, password, role);
    }

    private String spaceJson(String title, String city, String price) {
        return """
                {
                  "title":"%s",
                  "description":"Covered bay with a straightforward exit.",
                  "address":"Sector 18, %s",
                  "city":"%s",
                  "parkingType":"COVERED",
                  "hourlyPrice":%s,
                  "imageUrl":"https://images.unsplash.com/photo-1506521781263-d8422e82f27a?auto=format&fit=crop&w=800&q=80",
                  "availableFrom":"06:00:00",
                  "availableUntil":"23:00:00"
                }
                """.formatted(title, city, city, price);
    }

    private String bookingJson(long spaceId, LocalDateTime start, LocalDateTime end) {
        return """
                {"parkingSpaceId":%d,"startDateTime":"%s","endDateTime":"%s"}
                """.formatted(spaceId, start, end);
    }
}
