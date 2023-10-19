package ru.aston.bankapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(name = "transactions")
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String accNumFrom;

    private String accNumTo;

    @Temporal(TemporalType.TIME)
    private LocalTime time;

    @Column(columnDefinition = "numeric")
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private Operation operation;

    public Transaction(String accNumFrom, String accNumTo, LocalTime time, BigDecimal amount, Operation operation) {
        this.accNumFrom = accNumFrom;
        this.accNumTo = accNumTo;
        this.time = time;
        this.amount = amount;
        this.operation = operation;
    }
}
