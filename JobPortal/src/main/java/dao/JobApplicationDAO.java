package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import pojo.JobSeekerProfile;

/**
 * @author Julia, Shruti
 */
public class JobApplicationDAO {

	// Flag to enable/disable partitioning logic for read/write
	private static boolean isPartioningEnabled = true;

	private static String dbUser;
	private static String dbPassword;
	private static Map<String, Integer> degreePartitions;
	private static List<String> dbURLS = Arrays.asList(
			"jdbc:mysql://localhost:3306/jobapplication",
			"jdbc:mysql://localhost:3307/jobapplication",
			"jdbc:mysql://localhost:3308/jobapplication");

	private static List<Connection> dbConnections = new ArrayList<>();

	static {
		dbUser = System.getenv("DB_USER");
		dbPassword = System.getenv("DB_PASS");
		if (dbUser == null || dbPassword == null) {
			throw new RuntimeException("DB credentials missing");
		}

		degreePartitions = new HashMap<String, Integer>() {
			{
				put("MS", 0);
				put("MEng", 0);
				put("BS", 0);
				put("BEng", 0);
				put("BA", 1);
				put("BBA", 1);
				put("BFA", 1);
				put("MA", 1);
				put("MBA", 2);
				put("PhD", 2);
				put("MD", 2);
			}
		};

		// load and register JDBC driver for MySQL
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		for (String dbURL : dbURLS) {
			try {
				Connection connection = DriverManager.getConnection(dbURL, dbUser, dbPassword);
				dbConnections.add(connection);
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}
		}
	}

	private static final String INSERT_QUERY = "INSERT INTO profile (FullName, DOB, Address, Phone, Degree, University, YOE, Skills) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

	private static final String SELECT_QUERY = "SELECT ProfileID, FullName, DOB, Address, Phone, Degree, University, YOE, Skills "
			+ "FROM jobapplication.profile where %s = '%s'";

	// partition index is a value in 0 to n-1 range
	// when n === number of databases available
	private static Integer getPartitionIndexForDegree(String degreeName) {
		// another possible partitioning logic is
		// key.hashCode() % dbURLs.size(), this is generic for any string value
		// but it can cause problems if most strings map to same partition index
		return degreePartitions.get(degreeName);
	}

	public static boolean saveJobProfile(JobSeekerProfile profile)
			throws SQLException, ClassNotFoundException, JsonProcessingException {

		int dbServerCount = dbURLS.size();
		Integer dbIndex = null;
		if (isPartioningEnabled) {
			// With Partition Logic we will find the exact db where all the rows for the
			// given degree filter value can be saved
			dbIndex = getPartitionIndexForDegree(profile.getDegree());
		}

		// if not using partitions, randomly pick a server to save the profile
		// dbIndex can also be null, if partition could not be determined
		if (dbIndex == null) {
			Random random = new Random();
			dbIndex = random.nextInt(dbServerCount);
		}

		Connection connection = dbConnections.get(dbIndex);
		try (PreparedStatement statement = connection.prepareStatement(INSERT_QUERY)) {
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
				return true;
			}

		} catch (SQLException | JsonProcessingException e) {
			throw e;
		}
		return false;
	}

	public static List<JobSeekerProfile> getProfileByUniversity(String university)
			throws SQLException, ClassNotFoundException, JsonProcessingException {
		return getProfiles("University", university);
	}

	public static List<JobSeekerProfile> getProfileByDegree(String degree)
			throws SQLException, ClassNotFoundException, JsonProcessingException {
		return getProfiles("Degree", degree);
	}

	private static List<JobSeekerProfile> getProfilesWithQuery(String query, int dbIndex)
			throws SQLException, JsonProcessingException {

		List<JobSeekerProfile> profiles = new ArrayList<JobSeekerProfile>();

		Connection connection = dbConnections.get(dbIndex);
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
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

				profiles.add(new JobSeekerProfile(
						profileID, fullName, dob, address, phone, degree, university, yoe, skills));

				// System.out.println("profiles size: " + counterNodes + " " + profiles.size());
			}
			// System.out.println("Found entries: " + profiles.size());
		} catch (SQLException | JsonProcessingException e) {
			throw e;
		}
		return profiles;
	}

	private static List<JobSeekerProfile> getProfiles(String filter, String value)
			throws SQLException, ClassNotFoundException, JsonProcessingException {

		List<JobSeekerProfile> profiles = new ArrayList<JobSeekerProfile>();
		String query = String.format(SELECT_QUERY, filter, value);

		// load and register JDBC driver for MySQL
		Class.forName("com.mysql.jdbc.Driver");

		if (isPartioningEnabled && filter == "Degree") {
			// with partition only if filter is degree
			Integer dbIndex = getPartitionIndexForDegree(value);
			if (dbIndex != null) {
				// we know the exact db where rows for this partition is saved
				return getProfilesWithQuery(query, dbIndex);
			}
			// we couldn't find the partition, so we need to scan all databases
		}

		// Without Partition Logic
		// querying rows from all databases, one at a time
		// and then collecting them in profiles list
		for (int i = 0; i < dbURLS.size(); i++) {
			profiles.addAll(getProfilesWithQuery(query, i));
		}

		return profiles;
	}
}
