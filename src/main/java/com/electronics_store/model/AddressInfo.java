package com.electronics_store.model;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressInfo {
    private String firstName;
    private String lastName;
    private String companyName;
    private String country;
    private String streetAddress;
    private String apartment; // Optional
    private String city;
    private String state;
    private String zipCode;
    private String email;
    private String phone;
}
