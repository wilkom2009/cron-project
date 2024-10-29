package com.wilkom.cronproject.model;

import java.math.BigDecimal;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    private Integer id;
    private String holder;
    private BigDecimal balance;

    public Account() {
    }

    public Account(Integer id, String holder, BigDecimal balance) {
        this.id = id;
        this.holder = holder;
        this.balance = balance;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getHolder() {
        return this.holder;
    }

    public void setHolder(String holder) {
        this.holder = holder;
    }

    public BigDecimal getBalance() {
        return this.balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Account id(Integer id) {
        setId(id);
        return this;
    }

    public Account holder(String holder) {
        setHolder(holder);
        return this;
    }

    public Account balance(BigDecimal balance) {
        setBalance(balance);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Account)) {
            return false;
        }
        Account account = (Account) o;
        return Objects.equals(id, account.id) && Objects.equals(holder, account.holder)
                && Objects.equals(balance, account.balance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, holder, balance);
    }

    @Override
    public String toString() {
        return "{" +
                " id='" + getId() + "'" +
                ", holder='" + getHolder() + "'" +
                ", balance='" + getBalance() + "'" +
                "}";
    }

}
