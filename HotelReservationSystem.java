import java.util.Scanner;
import java.sql.*;

public class HotelReservationSystem {
	private static final String url = "jdbc:mysql://localhost:3306/hotel_db"; // private-can be used just within this class; static- do not need to create the object of this class to use these data members.
	private static final String username = "root"; // final - to keep it unchangeable through out the program.
	private static final String password = "pass123";

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		// Loading Driver
		try {
			Class.forName("com.mysql.cj.jdbc.Driver"); // while loading this Driver, one exception occurs by name of ClassNotFound which we have already declared in the main function declaration.
			// System.out.println("Driver loaded successfully."); // and have catched this exception using try-catch statement.
		} catch (ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}

		// Establishing Connection
		try {
			Connection connection = DriverManager.getConnection(url, username, password);
			while (true) {
				System.out.println();
				System.out.println("HOTEL MANAGEMENT SYSTEM");
				Scanner scanner = new Scanner(System.in);
				System.out.println("1. Reserve a room");
				System.out.println("2. View Reservations");
				System.out.println("3. Get Room Number");
				System.out.println("4. Update Reservations");
				System.out.println("5. Delete Reservations");
				System.out.println("0. Exit");
				System.out.print("Choose an option: ");
				int choice = scanner.nextInt();
				switch (choice) {
					case 1:
						reserveRoom(connection, scanner);
						break;
					case 2:
						viewReservations(connection);
						break;
					case 3:
						getRoomNumber(connection, scanner);
						break;
					case 4:
						updateReservation(connection, scanner);
						break;
					case 5:
						deleteReservation(connection, scanner);
						break;
					case 0:
						exit();
						scanner.close();
						return;
					default:
						System.out.println("Invalid choice. Try again.");
				}
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private static void reserveRoom(Connection connection, Scanner sc) {
		try {
			System.out.print("Enter guest name: ");
			String guestName = sc.next();
			sc.nextLine();
			System.out.print("Enter room number: ");
			int roomNumber = sc.nextInt();
			System.out.print("Enter contact number: ");
			String contactNumber = sc.next();

			String sql = "INSERT INTO reservations (guest_name, room_number, contact_number) " +
					"VALUES ('" + guestName + "', " + roomNumber + ", '" + contactNumber + "')";

			try (Statement statement = connection.createStatement()) { // When we use Statement interface then we
				// encounter with SQLException.
				int affectedRows = statement.executeUpdate(sql);

				if (affectedRows > 0) {
					System.out.println("Reservation successful!");
				} else {
					System.out.println("Reservation failed.");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void viewReservations(Connection connection) throws SQLException { // Using just connection interface because here we are not going to take any input form the user, we are just going to sho the values.
		String sql = "SELECT reservation_id, guest_name, room_number, contact_number, reservation_date FROM reservations";

		try (Statement statement = connection.createStatement();
			 ResultSet resultSet = statement.executeQuery(sql)) {
			System.out.println("Current Reservations:");
			System.out.println(
					"+----------------+-----------------+---------------+----------------------+-------------------------+");
			System.out.println(
					"| Reservation ID |    Guest Name    |  Room Number |    Contact Number    |     Reservation Date    |");
			System.out.println(
					"+----------------+-----------------+---------------+----------------------+-------------------------+");

			while (resultSet.next()) { // until the value is present inside the 'resultSet instance', till then it will be true.
				int reservationId = resultSet.getInt("reservation_id");
				String guestName = resultSet.getString("guest_name");
				int roomNumber = resultSet.getInt("room_number");
				String contactNumber = resultSet.getString("contact_number");
				String reservationDate = resultSet.getTimestamp("reservation_date").toString();

				// Format and display the reservation data in a table-like format
				System.out.printf("| %-14d | %-15s | %-13d | %-20s | %-19s   |\n",
						reservationId, guestName, roomNumber, contactNumber, reservationDate);
			}
			System.out.println("+----------------+-----------------+---------------+----------------------+-------------------------+");
		}
	}

	private static void getRoomNumber(Connection connection, Scanner sc) {
		try {
			System.out.print("Enter reservation ID: ");
			int reservationId = sc.nextInt();
			System.out.print("Enter guest name: ");
			String guestName = sc.next();

			String sql = "SELECT room_number FROM reservations " +
					"WHERE reservation_id = " + reservationId +
					" AND guest_name = '" + guestName + "'";

			try (Statement statement = connection.createStatement();
				 ResultSet resultSet = statement.executeQuery(sql)) {

				if (resultSet.next()) {
					int roomNumber = resultSet.getInt("room_number");
					System.out.println("Room number for Reservation ID " + reservationId +
							" and Guest " + guestName + " is: " + roomNumber);
				} else {
					System.out.println("Reservation not found for the given ID and guest name.");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void updateReservation(Connection connection, Scanner sc) {
		try {
			System.out.print("Enter reservation ID to update: ");
			int reservationId = sc.nextInt();
			sc.nextLine(); // Consume the newline character

			if (!reservationExists(connection, reservationId)) {
				System.out.println("Reservation not found for the given ID.");
				return;
			}

			System.out.print("Enter new guest name: ");
			String newGuestName = sc.nextLine();
			System.out.print("Enter new room number: ");
			int newRoomNumber = sc.nextInt();
			System.out.print("Enter new contact number: ");
			String newContactNumber = sc.next();

			String sql = "UPDATE reservations SET guest_name = '" + newGuestName + "', " +
					"room_number = " + newRoomNumber + ", " +
					"contact_number = '" + newContactNumber + "' " +
					"WHERE reservation_id = " + reservationId;

			try (Statement statement = connection.createStatement()) {
				int affectedRows = statement.executeUpdate(sql);

				if (affectedRows > 0) {
					System.out.println("Reservation updated successfully!");
				} else {
					System.out.println("Reservation update failed.");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void deleteReservation(Connection connection, Scanner sc) {
		try {
			System.out.print("Enter reservation ID to delete: ");
			int reservationId = sc.nextInt();

			if (!reservationExists(connection, reservationId)) {
				System.out.println("Reservation not found for the given ID.");
				return;
			}

			String sql = "DELETE FROM reservations WHERE reservation_id = " + reservationId;

			try (Statement statement = connection.createStatement()) {
				int affectedRows = statement.executeUpdate(sql);

				if (affectedRows > 0) {
					System.out.println("Reservation deleted successfully!");
				} else {
					System.out.println("Reservation deletion failed.");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static boolean reservationExists(Connection connection, int reservationId) {
		try {
			String sql = "SELECT reservation_id FROM reservations WHERE reservation_id = " + reservationId;

			try (Statement statement = connection.createStatement();
				 ResultSet resultSet = statement.executeQuery(sql)) {
				return resultSet.next(); // If there's a result, the reservation exists.
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false; // Handle database errors as needed
		}
	}

	public static void exit() throws InterruptedException {
		System.out.print("Exiting System");
		int i = 5;
		while (i != 0) {
			System.out.print(".");
			Thread.sleep(450);
			i--;
		}
		System.out.println();
		System.out.println("ThankYou For Using Hotel Reservation System!!!");
	}
}
