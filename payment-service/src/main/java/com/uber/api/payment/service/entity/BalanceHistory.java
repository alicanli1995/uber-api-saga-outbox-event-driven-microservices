package com.uber.api.payment.service.entity;

import com.uber.api.common.api.constants.TransactionStatus;
import com.uber.api.common.api.constants.TransactionType;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "balance_history")
public class BalanceHistory {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    private UUID id;
    private String email;
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    @CreatedDate
    private ZonedDateTime transactionDate;
    private BigDecimal transactionAmount;
    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "balance_id")
    private Balance balance;

}
