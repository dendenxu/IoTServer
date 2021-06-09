package com.neoncubes.iotserver;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class Greeting {
    private final long id;
    private final String content;
}
