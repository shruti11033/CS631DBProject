package servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

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

		// TODO: send profile to DB
		System.out.println("Saving profile: " + profile);

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

		// TODO: delete me after getting from DB
		JobSeekerProfile dummyProfile = new JobSeekerProfile();
		dummyProfile.setFullName("John Doe");
		dummyProfile.setUniversity("Stanford");
		dummyProfile.yoe = 42;

		List<JobSeekerProfile> profiles = Collections.singletonList(dummyProfile);
		// TODO: get profiles from DB using university search term

		resp.setStatus(200);
		resp.getWriter().println(new ObjectMapper().writeValueAsString(profiles));
		resp.getWriter().close();
	}

}
