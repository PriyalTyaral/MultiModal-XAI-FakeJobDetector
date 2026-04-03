package com.example.random_major.model;

/**
 * Request object for company verification
 */
public class CompanyVerificationRequest {
    private String companyName;
    private String jobPostingUrl;
    private String contactEmail;

    public CompanyVerificationRequest() {}

    public CompanyVerificationRequest(String companyName, String jobPostingUrl, String contactEmail) {
        this.companyName = companyName;
        this.jobPostingUrl = jobPostingUrl;
        this.contactEmail = contactEmail;
    }

    // Getters and Setters
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobPostingUrl() {
        return jobPostingUrl;
    }

    public void setJobPostingUrl(String jobPostingUrl) {
        this.jobPostingUrl = jobPostingUrl;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }
}
