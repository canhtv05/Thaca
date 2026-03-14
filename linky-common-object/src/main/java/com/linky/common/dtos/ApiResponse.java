package com.linky.common.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> {

    private ApiHeader header;
    private ApiBody<T> body;
}
