package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import pojo.JobSeekerProfile;

/**
 * @author Julia
 */
public class JobApplicationDAO {

	private static String dbUser;
	private static String dbPassword;
	static {
		dbUser = System.getenv("DB_USER");
		dbPassword = System.getenv("DB_PASS");
		if (dbUser == null || dbPassword == null) {
			throw new RuntimeException("DB credentials missing");
		}
	}

	private final static String DB_URL = "jdbc:mysql://localhost:3306/jobapplication";

	private static final String INSERT_QUERY = "INSERT INTO profile (FullName, DOB, Address, Phone, Degree, University, YOE, Skills) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

	private static final String SELECT_QUERY = "SELECT ProfileID, FullName, DOB, Address, Phone, Degree, University, YOE, Skills "
			+ "FROM jobapplication.profile where %s = %s";

	public static boolean saveJobProfile(JobSeekerProfile profile)
			throws SQLException, ClassNotFoundException, JsonProcessingException {
		// load and register JDBC driver for MySQL
		Class.forName("com.mysql.jdbc.Driver");

		try (Connection connection = DriverManager.getConnection(DB_URL, dbUser, dbPassword);
				PreparedStatement statement = connection.prepareStatement(INSERT_QUERY)) {
			// statement.setInt(1, profile.getProfileId());
			statement.setString(1, profile.getFullName());
			statement.setDate(2, new java.sql.Date(profile.getDob().getTime()));
			statement.setString(3, profile.getAddress());
			statement.setString(4, profile.getPhone());
			statement.setString(5, profile.getDegree());
			statement.setString(6, profile.getUniversity());
			statement.setInt(7, profile.getYoe());
			statement.setString(8, new ObjectMapper().writeValueAsString(profile.getSkills()));
			int rowsInserted = statement.executeUpdate();
			if (rowsInserted == 1) {
				System.out.println("Profile added successfully!");
				return true;
			}

		} catch (SQLException | JsonProcessingException e) {
			System.out.println("Error adding profile in jobapplication: " + e.getMessage());
			throw e;
		}
		return false;
	}


	public static List<JobSeekerProfile> getProfileByUniversity(String university)
			throws SQLException, ClassNotFoundException, JsonProcessingException {
		return getProfile("University", "'" + university + "'");
	}

	public static List<JobSeekerProfile> getProfileByDegree(String degree)
			throws SQLException, ClassNotFoundException, JsonProcessingException {
		return getProfile("Degree", "'" + degree + "'");
	}

	private static List<JobSeekerProfile> getProfile(String filter, String value)
			throws SQLException, ClassNotFoundException, JsonProcessingException {
		List<JobSeekerProfile> profiles = new ArrayList<JobSeekerProfile>();
		String query = String.format(SELECT_QUERY, filter, value);

		// load and register JDBC driver for MySQL
		Class.forName("com.mysql.jdbc.Driver");

		try (Connection connection = DriverManager.getConnection(DB_URL, dbUser, dbPassword);
				PreparedStatement stmt = connection.prepareStatement(query)) {
			ResultSet rs = stmt.executeQuery();
			// iterate through the java resultset
			while (rs.next()) {
				int profileID = rs.getInt("ProfileID");
				String fullName = rs.getString("FullName");
				Date dob = rs.getDate("DOB");
				String address = rs.getString("Address");
				String phone = rs.getString("Phone");
				String degree = rs.getString("Degree");
				String university = rs.getString("University");
				int yoe = rs.getInt("YOE");
				String skillStr = rs.getString("Skills");
				List<String> skills = new ObjectMapper().readValue(skillStr, List.class);

				JobSeekerProfile profile = new JobSeekerProfile(
						profileID, fullName, dob, address, phone, degree, university, yoe, skills);
				profiles.add(profile);

			}
			System.out.println("Found entries: " + profiles.size());

		} catch (SQLException | JsonProcessingException e) {
			System.out.println("Error reading profiles using filter " + filter + "=" + value);
			throw e;
		}
		return profiles;
	}
}
