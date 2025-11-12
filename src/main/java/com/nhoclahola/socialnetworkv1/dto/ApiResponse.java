package com.nhoclahola.socialnetworkv1.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)  //Null field will not be included in json
public class ApiResponse<T>
{
    private int responseCode = 1000;
    private String message;
    private T result;
}

