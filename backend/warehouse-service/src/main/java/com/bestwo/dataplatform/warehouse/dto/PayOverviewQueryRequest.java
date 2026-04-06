package com.bestwo.dataplatform.warehouse.dto;

import jakarta.validation.constraints.AssertTrue;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class PayOverviewQueryRequest {

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @AssertTrue(message = "startDate and endDate must be provided together")
    public boolean isDateRangeComplete() {
        return (startDate == null && endDate == null) || (startDate != null && endDate != null);
    }

    @AssertTrue(message = "startDate must not be greater than endDate")
    public boolean isDateRangeValid() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return !startDate.isAfter(endDate);
    }

    public boolean hasDateRange() {
        return startDate != null && endDate != null;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
