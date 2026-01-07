package com.alura.hackatonAlura.churn;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Canonical request payload for single churn prediction using 20 raw variables
 * aligned with the Telco customer churn dataset. All String values are case-sensitive
 * and must match the reference exactly.
 */
public class ChurnRequest {

    // Categorical
    @JsonProperty("gender")
    @NotNull
    @Pattern(regexp = "Male|Female", message = "gender debe ser 'Male' o 'Female'")
    private String gender;

    @JsonProperty("SeniorCitizen")
    @NotNull @Min(0)
    private Integer seniorCitizen; // 0 o 1

    @JsonProperty("Partner")
    @NotNull
    @Pattern(regexp = "Yes|No", message = "Partner debe ser 'Yes' o 'No'")
    private String partner;

    @JsonProperty("Dependents")
    @NotNull
    @Pattern(regexp = "Yes|No", message = "Dependents debe ser 'Yes' o 'No'")
    private String dependents;

    // Numeric
    @JsonProperty("tenure")
    @NotNull @Min(0)
    private Integer tenure;

    @JsonProperty("PhoneService")
    @NotNull
    @Pattern(regexp = "Yes|No", message = "PhoneService debe ser 'Yes' o 'No'")
    private String phoneService;

    @JsonProperty("MultipleLines")
    @NotNull
    @Pattern(regexp = "No|Yes|No phone service", message = "MultipleLines valores: 'No','Yes','No phone service'")
    private String multipleLines;

    @JsonProperty("InternetService")
    @NotNull
    @Pattern(regexp = "DSL|Fiber optic|No", message = "InternetService valores: 'DSL','Fiber optic','No'")
    private String internetService;

    @JsonProperty("OnlineSecurity")
    @NotNull
    @Pattern(regexp = "Yes|No|No internet service", message = "OnlineSecurity valores: 'Yes','No','No internet service'")
    private String onlineSecurity;

    @JsonProperty("OnlineBackup")
    @NotNull
    @Pattern(regexp = "Yes|No|No internet service", message = "OnlineBackup valores: 'Yes','No','No internet service'")
    private String onlineBackup;

    @JsonProperty("DeviceProtection")
    @NotNull
    @Pattern(regexp = "Yes|No|No internet service", message = "DeviceProtection valores: 'Yes','No','No internet service'")
    private String deviceProtection;

    @JsonProperty("TechSupport")
    @NotNull
    @Pattern(regexp = "Yes|No|No internet service", message = "TechSupport valores: 'Yes','No','No internet service'")
    private String techSupport;

    @JsonProperty("StreamingTV")
    @NotNull
    @Pattern(regexp = "Yes|No|No internet service", message = "StreamingTV valores: 'Yes','No','No internet service'")
    private String streamingTV;

    @JsonProperty("StreamingMovies")
    @NotNull
    @Pattern(regexp = "Yes|No|No internet service", message = "StreamingMovies valores: 'Yes','No','No internet service'")
    private String streamingMovies;

    @JsonProperty("Contract")
    @NotNull
    @Pattern(regexp = "Month-to-month|One year|Two year", message = "Contract valores: 'Month-to-month','One year','Two year'")
    private String contract;

    @JsonProperty("PaperlessBilling")
    @NotNull
    @Pattern(regexp = "Yes|No", message = "PaperlessBilling debe ser 'Yes' o 'No'")
    private String paperlessBilling;

    @JsonProperty("PaymentMethod")
    @NotNull
    @Pattern(regexp = "Electronic check|Mailed check|Bank transfer \\(automatic\\)|Credit card \\(automatic\\)", message = "PaymentMethod valores exactos requeridos")
    private String paymentMethod;

    @JsonProperty("MonthlyCharges")
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true, message = "MonthlyCharges debe ser número ≥ 0 sin símbolos")
    private Double monthlyCharges;

    @JsonProperty("TotalCharges")
    @DecimalMin(value = "0.0", inclusive = true, message = "TotalCharges debe ser número ≥ 0 sin símbolos")
    private Double totalCharges; // null/blank se trata en servicio (opción A: 0.0)

    // Getters/Setters
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Integer getSeniorCitizen() { return seniorCitizen; }
    public void setSeniorCitizen(Integer seniorCitizen) { this.seniorCitizen = seniorCitizen; }
    public String getPartner() { return partner; }
    public void setPartner(String partner) { this.partner = partner; }
    public String getDependents() { return dependents; }
    public void setDependents(String dependents) { this.dependents = dependents; }
    public Integer getTenure() { return tenure; }
    public void setTenure(Integer tenure) { this.tenure = tenure; }
    public String getPhoneService() { return phoneService; }
    public void setPhoneService(String phoneService) { this.phoneService = phoneService; }
    public String getMultipleLines() { return multipleLines; }
    public void setMultipleLines(String multipleLines) { this.multipleLines = multipleLines; }
    public String getInternetService() { return internetService; }
    public void setInternetService(String internetService) { this.internetService = internetService; }
    public String getOnlineSecurity() { return onlineSecurity; }
    public void setOnlineSecurity(String onlineSecurity) { this.onlineSecurity = onlineSecurity; }
    public String getOnlineBackup() { return onlineBackup; }
    public void setOnlineBackup(String onlineBackup) { this.onlineBackup = onlineBackup; }
    public String getDeviceProtection() { return deviceProtection; }
    public void setDeviceProtection(String deviceProtection) { this.deviceProtection = deviceProtection; }
    public String getTechSupport() { return techSupport; }
    public void setTechSupport(String techSupport) { this.techSupport = techSupport; }
    public String getStreamingTV() { return streamingTV; }
    public void setStreamingTV(String streamingTV) { this.streamingTV = streamingTV; }
    public String getStreamingMovies() { return streamingMovies; }
    public void setStreamingMovies(String streamingMovies) { this.streamingMovies = streamingMovies; }
    public String getContract() { return contract; }
    public void setContract(String contract) { this.contract = contract; }
    public String getPaperlessBilling() { return paperlessBilling; }
    public void setPaperlessBilling(String paperlessBilling) { this.paperlessBilling = paperlessBilling; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public Double getMonthlyCharges() { return monthlyCharges; }
    public void setMonthlyCharges(Double monthlyCharges) { this.monthlyCharges = monthlyCharges; }
    public Double getTotalCharges() { return totalCharges; }
    public void setTotalCharges(Double totalCharges) { this.totalCharges = totalCharges; }
}
