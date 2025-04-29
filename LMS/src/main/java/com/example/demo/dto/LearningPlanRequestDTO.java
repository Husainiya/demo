package com.example.demo.dto;

import com.example.demo.model.PlanStatus;
import jakarta.validation.constraints.*;
import lombok.*;
import org.w3c.dom.Text;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class LearningPlanRequestDTO {

    private String userId = "default-user";

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Topics list is required")
    private List<String> topics;

    private List<String> topicsCovered;

    private double progress=0;

    private String description;


    @NotNull
    private PlanStatus status;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;
}
