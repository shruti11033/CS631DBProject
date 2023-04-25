package servlets;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JobSeekerProfile {

    private int profileId;

    private String fullName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy")
    private Date dob;

    private String address;

    private String phone;

    private String degree;

    private String university;

    private List<String> skills;

    private int yoe;

    public JobSeekerProfile() {
        
    }

    public JobSeekerProfile(int id, String fullName, Date dob, String address, String phone, String degree,
			String university, List<String> skills, int yoe) {
		this.profileId = id;
		this.fullName = fullName;
		this.dob = dob;
		this.address = address;
		this.phone = phone;
		this.degree = degree;
		this.university = university;
		this.skills = skills;
		this.yoe = yoe;
	}

    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String college) {
        this.university = college;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skill) {
        this.skills = skill;
    }

    public int getYoe() {
        return yoe;
    }

    public void setYoe(int experienceYears) {
        this.yoe = experienceYears;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "invalid profile";
    }

}