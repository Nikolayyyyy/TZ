package ru.aston.bankapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.ToString;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(name = "accounts")
@AllArgsConstructor
public class Account {

    @Id
    private String accNum;

    private String name;

    @Column(length = 4)
    private String pinCode;

    @Column(columnDefinition = "numeric")
    private BigDecimal amount;

    public Account(String name, String pinCode) {
        this.accNum = UUID.randomUUID().toString();
        this.name = name;
        this.pinCode = pinCode;
        this.amount = BigDecimal.valueOf(0);
    }

}
