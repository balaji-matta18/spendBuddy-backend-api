package com.spendbuddy.response.dto;

/**
 * Simple DTO for chart-friendly responses.
 */
public class ReportResponse {
    private String label;
    private Double value;

    public ReportResponse() {}

    public ReportResponse(String label, Double value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}

