package com.example.restauth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class InputErrorResponse {

    private String status;
    private String msg;
}