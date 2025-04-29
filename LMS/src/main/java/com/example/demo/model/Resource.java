package com.example.demo.model;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Resource {
    private String fileName;
    private String fileUrl;
    private ResourceType type;
    private String description;
    private LocalDateTime uploadedAt;
}
