package com.uber.api.customer.service.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_extra")
public class UserExtra {

    @Id
    private String username;
    private String avatar;

    public UserExtra(String name) {
        this.username = name;
    }
}
