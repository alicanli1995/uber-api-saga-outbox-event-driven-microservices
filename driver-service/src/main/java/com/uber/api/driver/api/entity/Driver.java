package com.uber.api.driver.api.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.uber.api.common.api.constants.DriverStatus;
import com.uber.api.common.api.entity.Location;
import com.uber.api.common.api.entity.PendingRequest;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "drivers")
public class Driver {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    private String id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;

    @JsonProperty("driver_status")
    @Enumerated(EnumType.STRING)
    private DriverStatus driverStatus;

    @JsonIgnore
    private String ipAddress;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonBackReference
    @JoinColumn(name = "pending_request_request_id")
    private PendingRequest pendingRequest;

    @JsonProperty("driver_location")
    @JoinColumn(nullable = false)
    @OneToOne(cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    private Location locations;

}
