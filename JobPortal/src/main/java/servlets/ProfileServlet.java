package servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import dao.JobApplicationDAO;

/**
 * Sample servlet class for Profile related methods
 */
public class ProfileServlet extends HttpServlet {

	// Method to submit candidate's profile
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		System.out.println("Inside Profile Servlet /doPost ************");

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
		} catch (InvalidFormatException e) {
			e.printStackTrace();
			resp.setStatus(400);
			resp.setContentType("plain/text");
			resp.getWriter().println("Invalid format, please check request payload");
			resp.getWriter().close();
			return;
		}

		System.out.println("Saving profile: " + profile);
		JobApplicationDAO dao = new JobApplicationDAO();
		try {
			dao.saveJobProfile(profile);
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			resp.setStatus(400);
			resp.setContentType("plain/text");
			resp.getWriter().println("Error from DB: " + e.getMessage());
			resp.getWriter().close();
			return;
		}
		resp.setStatus(200);
		resp.setContentType("plain/text");
		resp.getWriter().println("Profile submitted successfully!");
		resp.getWriter().close();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("Inside Profile Servlet /doGet ************");
		String univSearchTerm = req.getParameter("university");
		System.out.println("University search term: " + univSearchTerm);
		JobApplicationDAO dao = new JobApplicationDAO();
		List<JobSeekerProfile> readProfiles;
		try {
			readProfiles = dao.getProfileByUniversity(univSearchTerm);
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			resp.setStatus(400);
			resp.setContentType("plain/text");
			resp.getWriter().println("Error retreiving from DB: " + e.getMessage());
			resp.getWriter().close();
			return;
		}
	
		resp.setStatus(200);
		resp.getWriter().println(new ObjectMapper().writeValueAsString(readProfiles));
		resp.getWriter().close();
	}

}
