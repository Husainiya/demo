package com.example.demo.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Topic {
    private String name;
    private boolean completed;
}
