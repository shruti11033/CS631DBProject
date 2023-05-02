package servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
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
 * Servlet class for Profile related methods
 * 
 * @author Shruti
 */
public class ProfileServlet extends HttpServlet {

	private static void sendOk(HttpServletResponse resp, String body) throws IOException {
		resp.setStatus(200);
		resp.setContentType("plain/text");
		resp.getWriter().print(body);
		resp.getWriter().close();
	}

	private static void sendErrorResponse(HttpServletResponse resp, int statusCode, String message) throws IOException {
		resp.setStatus(statusCode);
		resp.setContentType("plain/text");
		resp.getWriter().print(message);
		System.out.println(message);
		resp.getWriter().close();
	}

	// Method to submit candidate's profile
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

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

		boolean isSaved;
		try {
			isSaved = JobApplicationDAO.saveJobProfile(profile);
		} catch (Exception e) {
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

		Map<String, String[]> queryParams = req.getParameterMap();
		if (queryParams.size() != 1) {
			sendErrorResponse(resp, 400, "Invalid request, expecting exactly one query param");
			return;
		}

		String key = null;
		String value = null;
		for (String param : queryParams.keySet()) {
			String[] values = queryParams.get(param);
			if (values.length != 1) {
				sendErrorResponse(resp, 400, "Invalid request, expecting exactly one value for query  param");
				return;
			}
			key = param;
			value = values[0];
		}

		List<JobSeekerProfile> readProfiles;
		try {
			switch (key) {
				case "university":
					readProfiles = JobApplicationDAO.getProfileByUniversity(value);
					break;
				case "degree":
					readProfiles = JobApplicationDAO.getProfileByDegree(value);
					break;
				default:
					sendErrorResponse(resp, 400, "Invalid request, unsupported query param: " + key);
					return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			sendErrorResponse(resp, 400, "Failed to read profiles. Error: " + e.getMessage());
			return;
		}

		sendOk(resp, new ObjectMapper().writeValueAsString(readProfiles));
	}

}
