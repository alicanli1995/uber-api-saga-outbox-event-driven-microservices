package com.uber.api.customer.service.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.uber.api.common.api.constants.CustomerStatus;
import com.uber.api.common.api.entity.Location;
import com.uber.api.common.api.entity.PendingRequest;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    private UUID id;

    private String name;

    private String email;

    @JsonProperty("customer_status")
    @Enumerated(EnumType.STRING)
    private CustomerStatus customerStatus;

    @JsonIgnore
    private String ipAddress;

    @JsonIgnore
    @JsonBackReference
    @OneToOne(cascade = CascadeType.ALL)
    private PendingRequest pendingRequest;

    @JsonProperty("customer_location")
    @OneToOne(cascade = CascadeType.ALL)
    private Location locations;


}
