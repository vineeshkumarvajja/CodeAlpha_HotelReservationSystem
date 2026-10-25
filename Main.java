import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Main {

    static Scanner sc = new Scanner(System.in);
    static ArrayList<Room> rooms = new ArrayList<>();
    static ArrayList<Booking> bookings = new ArrayList<>();

    static Path file = Paths.get("data", "reservations.csv");
    static int nextId = 1001;

    static class Room {
        int number;
        String type;
        double price;

        Room(int number, String type, double price) {
            this.number = number;
            this.type = type;
            this.price = price;
        }
    }

    static class Booking {
        int id, roomNumber;
        String guest, status;
        LocalDate checkIn, checkOut;
        double total;

        Booking(int id, String guest, int roomNumber,
                LocalDate checkIn, LocalDate checkOut,
                double total, String status) {
            this.id = id;
            this.guest = guest;
            this.roomNumber = roomNumber;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.total = total;
            this.status = status;
        }
    }

    public static void main(String[] args) {
        initializeRooms();
        loadBookings();

        while (true) {
            System.out.println("\n=== Hotel Reservation System ===");
            System.out.println("1. View Rooms");
            System.out.println("2. Search Available Rooms");
            System.out.println("3. Book Room");
            System.out.println("4. View Bookings");
            System.out.println("5. Search Booking ID");
            System.out.println("6. Cancel Booking");
            System.out.println("0. Exit");

            int choice = readInt("Enter choice: ");

            switch (choice) {
                case 1: viewRooms(); break;
                case 2: searchRooms(); break;
                case 3: bookRoom(); break;
                case 4: viewBookings(); break;
                case 5: searchBooking(); break;
                case 6: cancelBooking(); break;
                case 0:
                    saveBookings();
                    System.out.println("Data saved. Goodbye!");
                    sc.close();
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    static void initializeRooms() {
        rooms.add(new Room(101, "Standard", 1500));
        rooms.add(new Room(102, "Standard", 1500));
        rooms.add(new Room(201, "Deluxe", 2500));
        rooms.add(new Room(202, "Deluxe", 2500));
        rooms.add(new Room(301, "Suite", 4500));
    }

    static void viewRooms() {
        System.out.println("\nRoom  Type       Price per night");
        for (Room r : rooms) {
            System.out.printf("%d  %-10s Rs. %.2f%n",
                    r.number, r.type, r.price);
        }
    }

    static LocalDate readDate(String message) {
        while (true) {
            System.out.print(message);
            try {
                return LocalDate.parse(sc.nextLine().trim());
            } catch (Exception e) {
                System.out.println("Use date format YYYY-MM-DD.");
            }
        }
    }

    static boolean validDates(LocalDate in, LocalDate out) {
        if (in.isBefore(LocalDate.now())) {
            System.out.println("Check-in cannot be in the past.");
            return false;
        }
        if (!out.isAfter(in)) {
            System.out.println("Check-out must be after check-in.");
            return false;
        }
        return true;
    }

    static boolean isAvailable(Room room,
            LocalDate in, LocalDate out) {
        for (Booking b : bookings) {
            if (b.roomNumber == room.number
                    && b.status.equals("CONFIRMED")
                    && in.isBefore(b.checkOut)
                    && out.isAfter(b.checkIn)) {
                return false;
            }
        }
        return true;
    }

    static void searchRooms() {
        LocalDate in = readDate("Check-in (YYYY-MM-DD): ");
        LocalDate out = readDate("Check-out (YYYY-MM-DD): ");

        if (!validDates(in, out)) return;

        boolean found = false;
        for (Room r : rooms) {
            if (isAvailable(r, in, out)) {
                System.out.printf("Room %d | %s | Rs. %.2f/night%n",
                        r.number, r.type, r.price);
                found = true;
            }
        }

        if (!found) System.out.println("No rooms available.");
    }

    static void bookRoom() {
        System.out.print("Guest name: ");
        String guest = sc.nextLine().trim();

        if (guest.isEmpty() || guest.contains("|")
                || guest.contains(",")) {
            System.out.println("Enter a valid name without , or |.");
            return;
        }

        LocalDate in = readDate("Check-in (YYYY-MM-DD): ");
        LocalDate out = readDate("Check-out (YYYY-MM-DD): ");

        if (!validDates(in, out)) return;

        searchRoomList(in, out);

        int number = readInt("Choose room number: ");
        Room selected = null;

        for (Room r : rooms) {
            if (r.number == number && isAvailable(r, in, out)) {
                selected = r;
                break;
            }
        }

        if (selected == null) {
            System.out.println("Room is not available.");
            return;
        }

        long nights = ChronoUnit.DAYS.between(in, out);
        double total = nights * selected.price;

        System.out.printf("Total for %d night(s): Rs. %.2f%n",
                nights, total);
        System.out.println("Payment is only a simulation.");
        System.out.print("Confirm booking? (y/n): ");

        if (sc.nextLine().trim().equalsIgnoreCase("y")) {
            Booking b = new Booking(nextId++, guest, number,
                    in, out, total, "CONFIRMED");
            bookings.add(b);
            saveBookings();
            System.out.println("Booking successful! ID: " + b.id);
        } else {
            System.out.println("Booking cancelled before confirmation.");
        }
    }

    static void searchRoomList(LocalDate in, LocalDate out) {
        System.out.println("Available rooms:");
        for (Room r : rooms) {
            if (isAvailable(r, in, out)) {
                System.out.printf("%d | %s | Rs. %.2f/night%n",
                        r.number, r.type, r.price);
            }
        }
    }

    static void viewBookings() {
        if (bookings.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }
        for (Booking b : bookings) printBooking(b);
    }

    static void searchBooking() {
        int id = readInt("Enter booking ID: ");
        for (Booking b : bookings) {
            if (b.id == id) {
                printBooking(b);
                return;
            }
        }
        System.out.println("Booking not found.");
    }

    static void cancelBooking() {
        int id = readInt("Enter booking ID to cancel: ");

        for (Booking b : bookings) {
            if (b.id == id) {
                if (b.status.equals("CANCELLED")) {
                    System.out.println("Already cancelled.");
                } else {
                    b.status = "CANCELLED";
                    saveBookings();
                    System.out.println("Booking cancelled.");
                }
                return;
            }
        }
        System.out.println("Booking not found.");
    }

    static void printBooking(Booking b) {
        System.out.printf(
            "ID: %d | Guest: %s | Room: %d | %s to %s"
            + " | Total: Rs. %.2f | Status: %s%n",
            b.id, b.guest, b.roomNumber,
            b.checkIn, b.checkOut, b.total, b.status);
    }

    static int readInt(String message) {
        while (true) {
            System.out.print(message);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid whole number.");
            }
        }
    }

    static void loadBookings() {
        if (!Files.exists(file)) return;

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] p = line.split("\\|", -1);
                if (p.length != 8) continue;

                try {
                    Booking b = new Booking(
                        Integer.parseInt(p[0]), p[1],
                        Integer.parseInt(p[2]),
                        LocalDate.parse(p[3]),
                        LocalDate.parse(p[4]),
                        Double.parseDouble(p[5]), p[6]);

                    bookings.add(b);
                    nextId = Math.max(nextId, b.id + 1);
                } catch (Exception e) {
                    System.out.println("Skipping invalid saved record.");
                }
            }
        } catch (IOException e) {
            System.out.println("Could not load data: " + e.getMessage());
        }
    }

    static void saveBookings() {
        try {
            Files.createDirectories(file.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(file)) {
                for (Booking b : bookings) {
                    writer.write(b.id + "|" + b.guest + "|"
                        + b.roomNumber + "|" + b.checkIn + "|"
                        + b.checkOut + "|" + b.total + "|"
                        + b.status + "|v1");
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("Could not save data: " + e.getMessage());
        }
    }
}