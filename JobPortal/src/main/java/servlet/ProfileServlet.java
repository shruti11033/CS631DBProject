package servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dao.JobApplicationDAO;
import pojo.JobSeekerProfile;

/**
 * Sample servlet class for Profile related methods
 */
public class ProfileServlet extends HttpServlet {

	private static void sendOk(HttpServletResponse resp, String body) throws IOException {
		resp.setStatus(200);
		resp.setContentType("plain/text");
		resp.getWriter().println(body);
		resp.getWriter().close();
	}

	private static void sendErrorResponse(HttpServletResponse resp, int statusCode, String message) throws IOException {
		resp.setStatus(statusCode);
		resp.setContentType("plain/text");
		resp.getWriter().println(message);
		resp.getWriter().close();
	}

	// Method to submit candidate's profile
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		System.out.println("********** ProfileServlet/doPost **********");

		StringBuilder reqBodyBuilder = new StringBuilder();
		BufferedReader reader = req.getReader();
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				reqBodyBuilder.append(line).append('\n');
			}
		} finally {
			reader.close();
		}
		System.out.println("Got request body: " + reqBodyBuilder.toString());

		// ObjectMapper instantiation
		ObjectMapper objectMapper = new ObjectMapper();
		JobSeekerProfile profile;
		try {
			// Deserialization into the `JobSeeker` class
			profile = objectMapper.readValue(reqBodyBuilder.toString(), JobSeekerProfile.class);
		} catch (JacksonException e) {
			e.printStackTrace();
			sendErrorResponse(resp, 400, "Invalid input, please check request payload. Error: " + e.getMessage());
			return;
		}

		System.out.println("Saving profile: " + profile);
		boolean isSaved;
		try {
			isSaved = JobApplicationDAO.saveJobProfile(profile);
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			sendErrorResponse(resp, 500, "Failed to save profile. Error: " + e.getMessage());
			return;
		}

		if (!isSaved) {
			sendErrorResponse(resp, 500, "Failed to save profile.");
			return;
		}

		sendOk(resp, "Profile submitted successfully!");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		System.out.println("********** ProfileServlet/doGet **********");

		String univSearchTerm = req.getParameter("university");

		if (univSearchTerm == null || univSearchTerm == "") {
			sendErrorResponse(resp, 400, "Invalid request, missing search query param");
			return;
		}

		System.out.println("University search term: " + univSearchTerm);
		List<JobSeekerProfile> readProfiles;
		try {
			readProfiles = JobApplicationDAO.getProfileByUniversity(univSearchTerm);
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			sendErrorResponse(resp, 400, "Failed to read profiles. Error: " + e.getMessage());
			return;
		}

		sendOk(resp, new ObjectMapper().writeValueAsString(readProfiles));
	}

}
