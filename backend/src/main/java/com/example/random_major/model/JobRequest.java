package com.example.random_major.model;

public class JobRequest {
    private String title;
    private String description;
    private String requirements;
    private String company_profile;
    private String industry;
    private String function;
    private String location;
    private String employment_type;
    private String required_experience;
    private String required_education;
    private int telecommuting;
    private int has_company_logo;
    private int has_questions;

    // Default constructor
    public JobRequest() {}

    // Getters and setters for all fields

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequirements() {
        return requirements;
    }
    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getCompany_profile() {
        return company_profile;
    }
    public void setCompany_profile(String company_profile) {
        this.company_profile = company_profile;
    }

    public String getIndustry() {
        return industry;
    }
    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getFunction() {
        return function;
    }
    public void setFunction(String function) {
        this.function = function;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public String getEmployment_type() {
        return employment_type;
    }
    public void setEmployment_type(String employment_type) {
        this.employment_type = employment_type;
    }

    public String getRequired_experience() {
        return required_experience;
    }
    public void setRequired_experience(String required_experience) {
        this.required_experience = required_experience;
    }

    public String getRequired_education() {
        return required_education;
    }
    public void setRequired_education(String required_education) {
        this.required_education = required_education;
    }

    public int getTelecommuting() {
        return telecommuting;
    }
    public void setTelecommuting(int telecommuting) {
        this.telecommuting = telecommuting;
    }

    public int getHas_company_logo() {
        return has_company_logo;
    }
    public void setHas_company_logo(int has_company_logo) {
        this.has_company_logo = has_company_logo;
    }

    public int getHas_questions() {
        return has_questions;
    }
    public void setHas_questions(int has_questions) {
        this.has_questions = has_questions;
    }
}
