package com.wilkom.cronproject.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "accounts")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Account {

    @Id
    private Integer id;
    private String holder;
    private BigDecimal balance;
    @Column(name = "balance_time")
    private LocalDateTime balanceTime;

    @Override
    public String toString() {
        return "{" +
                " id='" + getId() + "'" +
                ", holder='" + getHolder() + "'" +
                ", balance='" + getBalance() + "'" +
                "}";
    }

}
