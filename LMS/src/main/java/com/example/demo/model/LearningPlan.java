package com.example.demo.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.w3c.dom.Text;

import java.time.LocalDate;
import java.util.List;

@Document(collection = "learningPlans")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class LearningPlan {

    @Id
    private String id;

    private String userId; // Hardcoded for now

    private String title;
    private List<Topic> topics;
    private List<Resource> resources;
    private List<String> topicsCovered;
    private double progress;
    private PlanStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
}
