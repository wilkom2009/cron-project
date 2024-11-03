package com.wilkom.cronproject.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RawData {

    private String name;

    private BigDecimal amount;

    // toString method for easy printing
    @Override
    public String toString() {
        return "RawData{" +
                ", name='" + name + '\'' +
                ", amount=" + amount +
                '}';
    }
}
