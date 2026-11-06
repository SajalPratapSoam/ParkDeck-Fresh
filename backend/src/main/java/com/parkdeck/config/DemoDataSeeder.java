package com.parkdeck.config;

import com.parkdeck.entity.ParkingSpace;
import com.parkdeck.entity.ParkingType;
import com.parkdeck.entity.Role;
import com.parkdeck.entity.User;
import com.parkdeck.entity.Vehicle;
import com.parkdeck.repository.ParkingSpaceRepository;
import com.parkdeck.repository.UserRepository;
import com.parkdeck.repository.VehicleRepository;
import java.math.BigDecimal;
import java.time.LocalTime;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DemoDataSeeder implements ApplicationRunner {

    static final String HOST_EMAIL = "aarav.host@parkdeck.local";
    static final String DRIVER_EMAIL = "meera.driver@parkdeck.local";
    static final String DEMO_PASSWORD = "Parkdeck1";

    private final UserRepository users;
    private final ParkingSpaceRepository spaces;
    private final VehicleRepository vehicles;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(
            UserRepository users,
            ParkingSpaceRepository spaces,
            VehicleRepository vehicles,
            PasswordEncoder passwordEncoder) {
        this.users = users;
        this.spaces = spaces;
        this.vehicles = vehicles;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        User host = users.findByEmailIgnoreCase(HOST_EMAIL).orElseGet(() -> {
            User created = new User();
            created.setFullName("Aarav Mehta");
            created.setEmail(HOST_EMAIL);
            created.setPasswordHash(passwordEncoder.encode(DEMO_PASSWORD));
            created.setRole(Role.HOST);
            created.setPhone("+919810011223");
            return users.save(created);
        });
        users.findByEmailIgnoreCase(DRIVER_EMAIL).orElseGet(() -> {
            User created = new User();
            created.setFullName("Meera Iyer");
            created.setEmail(DRIVER_EMAIL);
            created.setPasswordHash(passwordEncoder.encode(DEMO_PASSWORD));
            created.setRole(Role.DRIVER);
            created.setPhone("+919876512340");
            User saved = users.save(created);
            Vehicle vehicle = new Vehicle();
            vehicle.setOwner(saved);
            vehicle.setVehicleType("Hatchback");
            vehicle.setRegistrationNumber("UP16AB4321");
            vehicle.setNickname("City runabout");
            vehicles.save(vehicle);
            return saved;
        });
        if (spaces.count() > 0) {
            return;
        }
        seed(host, "Shaded bay by Pari Chowk",
                "A covered slot a short walk from the Pari Chowk metro plaza. Easy in-out for evening markets and weekend plans.",
                "B-12, Pari Chowk Metro Parking, Greater Noida", "Greater Noida", ParkingType.COVERED, "45.00",
                "https://images.unsplash.com/photo-1506521781263-d8422e82f27a?auto=format&fit=crop&w=1400&q=80",
                "06:00", "23:00");
        seed(host, "Quiet basement in Alpha 1",
                "Reserved basement bay in the Alpha 1 commercial belt. Available through the night when you need a longer stay.",
                "Basement B2, Alpha 1 Commercial Belt, Greater Noida", "Greater Noida", ParkingType.BASEMENT, "55.00",
                "https://images.unsplash.com/photo-1573348722427-f1d6819fdf98?auto=format&fit=crop&w=1400&q=80",
                "00:00", "23:59");
        seed(host, "Open lot at Knowledge Park II",
                "An open marked bay near the colleges and tech offices of Knowledge Park II. Best for daytime classes and meetings.",
                "Bay 4, Knowledge Park II, Greater Noida", "Greater Noida", ParkingType.OPEN, "30.00",
                "https://images.unsplash.com/photo-1590674899484-d5640e854abe?auto=format&fit=crop&w=1400&q=80",
                "07:00", "22:00");
        seed(host, "Market hours slot, Sector 18",
                "Podium parking beside the Sector 18 market. Leave the car and walk to the shops without circling the block.",
                "Podium level, Sector 18 Market, Noida", "Noida", ParkingType.COVERED, "80.00",
                "https://images.unsplash.com/photo-1486006920555-c77dcf18193c?auto=format&fit=crop&w=1400&q=80",
                "08:00", "23:00");
        seed(host, "Office-tower basement, Sector 62",
                "Visitor bay under a Sector 62 office tower. Useful for client meetings when the building stack is full.",
                "Tower C visitors, Sector 62, Noida", "Noida", ParkingType.BASEMENT, "60.00",
                "https://images.unsplash.com/photo-1617886322207-6f504e7472c5?auto=format&fit=crop&w=1400&q=80",
                "06:00", "22:00");
        seed(host, "Metro-side open parking",
                "Open parking by Botanical Garden metro. A practical hop-off point before you switch to the train.",
                "Gate 2, Botanical Garden, Noida", "Noida", ParkingType.OPEN, "40.00",
                "https://images.unsplash.com/photo-1470224114660-3f6686c562eb?auto=format&fit=crop&w=1400&q=80",
                "06:00", "21:00");
        seed(host, "Connaught Place inner circle",
                "Basement bay inside the inner circle. Book it before you head in for dinner so you are not hunting Radial Road.",
                "Inner circle basement, Connaught Place, Delhi", "Delhi", ParkingType.BASEMENT, "120.00",
                "https://images.unsplash.com/photo-1506521781263-d8422e82f27a?auto=format&fit=crop&w=1400&q=70",
                "07:00", "23:00");
        seed(host, "Saket cinema lane",
                "A street bay on the cinema side of Saket. Meant for a film and a late coffee, not an all-night stay.",
                "PVR side lane, Saket, Delhi", "Delhi", ParkingType.STREET, "70.00",
                "https://images.unsplash.com/photo-1449824913935-59a10b8d2000?auto=format&fit=crop&w=1400&q=80",
                "09:00", "23:00");
        seed(host, "Khan Market service lane",
                "Compact street slot on the Khan Market service lane. Walk to the bookshops once the car is settled.",
                "Service lane, Khan Market, Delhi", "Delhi", ParkingType.STREET, "100.00",
                "https://images.unsplash.com/photo-1511919884226-fd3cad34687c?auto=format&fit=crop&w=1400&q=80",
                "08:00", "22:00");
        seed(host, "Nehru Place covered stack",
                "Covered stack parking near the computer market. Shade and a straightforward exit after errands.",
                "Covered stack, Nehru Place, Delhi", "Delhi", ParkingType.COVERED, "65.00",
                "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?auto=format&fit=crop&w=1400&q=80",
                "07:00", "22:00");
    }

    private void seed(
            User host,
            String title,
            String description,
            String address,
            String city,
            ParkingType type,
            String price,
            String image,
            String from,
            String until) {
        ParkingSpace space = new ParkingSpace();
        space.setHost(host);
        space.setTitle(title);
        space.setDescription(description);
        space.setAddress(address);
        space.setCity(city);
        space.setParkingType(type);
        space.setHourlyPrice(new BigDecimal(price));
        space.setImageUrl(image);
        space.setAvailableFrom(LocalTime.parse(from));
        space.setAvailableUntil(LocalTime.parse(until));
        space.setActive(true);
        spaces.save(space);
    }
}
