package com.uber.api.common.api.entity;

import com.uber.api.common.api.constants.CallStatus;
import com.uber.api.common.api.constants.PaymentStatus;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.util.UUID;


@Entity
@Getter
@Setter
@Builder
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pending_requests", schema = "public")
public class PendingRequest {
    @Id
    private UUID requestId;

    @Column(nullable = false)
    private String ipAddress;
    @Email
    private String driverEmail;

    @Email
    private String customerEmail;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    @Enumerated(EnumType.STRING)
    private CallStatus callStatus;
    @OneToOne(cascade = CascadeType.ALL)
    private Location customerLocation;
    @OneToOne(cascade = CascadeType.ALL)
    private Location customerDestination;
    private Double offer;
    private Boolean isSpecialOffer;
}