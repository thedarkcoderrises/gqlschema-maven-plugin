package com.tdcr.pojo;


import com.tdcr.annotation.NotNull;

public class Address {

    @NotNull
    String addLine1;
    String addLine2;
    @NotNull
    String city;
    @NotNull
    String state;
    @NotNull
    Country country;
}
