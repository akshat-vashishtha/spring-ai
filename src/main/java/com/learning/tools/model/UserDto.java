package com.learning.tools.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserDto(
        Long id,
        String name,
        String username,
        String email,
        String phone,
        String website,
        AddressDto address,
        CompanyDto company
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AddressDto(
            String street,
            String suite,
            String city,
            String zipcode,
            GeoDto geo
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeoDto(
            String lat,
            String lng
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CompanyDto(
            String name,
            String catchPhrase,
            String bs
    ) {}
}
