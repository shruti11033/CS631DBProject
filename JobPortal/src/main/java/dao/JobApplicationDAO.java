package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import servlets.JobSeekerProfile;

/**
 * @author Julia
 */
public class JobApplicationDAO {
	final String DB_URL = "jdbc:mysql://localhost:3306/jobapplication";

    private String INSERT_QUERY = "INSERT INTO Profile (ProfileId, FullName, DOB, Address, Phone, Degree, University, YOE) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	
	private String SELECT_QUERY = "SELECT ProfileID, FullName, DOB, Address, Phone, Degree, University, YOE "
			+ "FROM jobapplication.Profile where %s = %s";

	public boolean saveJobProfile(JobSeekerProfile profile) throws SQLException, ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");
		try (Connection connection = DriverManager.getConnection(DB_URL, "root", "julia@MySQL123");
		PreparedStatement statement = connection.prepareStatement(INSERT_QUERY)) {
			statement.setInt(1, profile.getProfileId());
			statement.setString(2, profile.getFullName());
			statement.setDate(3, new java.sql.Date(profile.getDob().getTime()));
			statement.setString(4, profile.getAddress());
			statement.setString(5, profile.getPhone());
			statement.setString(6, profile.getDegree());
			statement.setString(7, profile.getUniversity());
			statement.setInt(8, profile.getYoe());
			int rowsInserted = statement.executeUpdate();
			if (rowsInserted > 0) {
				System.out.println("Profile added successfully!");
				return true;
			}

		} catch (SQLException e) {
			System.out.println("Error adding profile in jobapplication: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
		return false;

	}
	
	public List<JobSeekerProfile> getProfileById(int id) throws SQLException, ClassNotFoundException {
		return getProfile("ProfileID", String.valueOf(id));
	}
	
	public List<JobSeekerProfile> getProfileByUniversity(String university) throws SQLException, ClassNotFoundException {
		return getProfile("University", "'" + university + "'");
	}
	
	public List<JobSeekerProfile> getProfileByDegree(String degree) throws SQLException, ClassNotFoundException {
		return getProfile("Degree", "'" + degree + "'");
	}

	private List<JobSeekerProfile> getProfile(String filter, String value) throws SQLException, ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");
		List<JobSeekerProfile> profiles = new ArrayList<JobSeekerProfile>();
		String query = String.format(SELECT_QUERY, filter, value);
		try (Connection connection = DriverManager.getConnection(DB_URL, "root", "julia@MySQL123");
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

				JobSeekerProfile profile = new JobSeekerProfile(profileID, fullName, dob, address, phone, degree,
						university, Collections.EMPTY_LIST, yoe);
				profiles.add(profile);
				
			}
			System.out.println("Found entries: " + profiles.size());

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		return profiles;

	}
}
