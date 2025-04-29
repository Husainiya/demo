package com.example.demo.controller;

import com.example.demo.dto.LearningPlanRequestDTO;
import com.example.demo.model.*;
import com.example.demo.service.LearningPlanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/learning-plans")
@CrossOrigin
public class LearningPlanController {

    private final LearningPlanService planService;

    @Value("${upload.path:uploads/}")
    private String uploadPath;

    public LearningPlanController(LearningPlanService planService) {
        this.planService = planService;
    }

    @PostMapping
    public ResponseEntity<LearningPlan> createPlan(@Valid @RequestBody LearningPlanRequestDTO dto) {
        LearningPlan plan = new LearningPlan();
        plan.setUserId(dto.getUserId());
        plan.setTitle(dto.getTitle());
        plan.setTopics(dto.getTopics().stream().map(t -> new Topic(t, false)).toList());
        plan.setTopicsCovered(dto.getTopicsCovered());
        plan.setResources(new ArrayList<>());
        plan.setProgress(dto.getProgress());
        plan.setDescription(dto.getDescription());
        plan.setStatus(dto.getStatus());
        plan.setStartDate(dto.getStartDate());
        plan.setEndDate(dto.getEndDate());

        return new ResponseEntity<>(planService.createPlan(plan), HttpStatus.CREATED);
    }

    @GetMapping
    public List<LearningPlan> getAllPlans() {
        return planService.getAllPlans();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LearningPlan> getById(@PathVariable String id) {
        return planService.getPlanById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<LearningPlan> updatePlan(@PathVariable String id,
                                                   @Valid @RequestBody LearningPlanRequestDTO dto) {
        LearningPlan updated = new LearningPlan();
        updated.setUserId(dto.getUserId());
        updated.setTitle(dto.getTitle());
        updated.setTopics(dto.getTopics().stream().map(t -> new Topic(t, false)).toList());
        updated.setTopicsCovered(dto.getTopicsCovered());
        updated.setProgress(dto.getProgress());
        updated.setDescription(dto.getDescription());
        updated.setResources(new ArrayList<>());
        updated.setStatus(dto.getStatus());
        updated.setStartDate(dto.getStartDate());
        updated.setEndDate(dto.getEndDate());

        return planService.updatePlan(id, updated)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable String id) {
        return planService.deletePlan(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/upload")
    public ResponseEntity<?> uploadResource(@PathVariable String id,
                                            @RequestParam("file") MultipartFile file,
                                            @RequestParam("type") ResourceType type,
                                            @RequestParam(value = "description", required = false) String desc) {
        try {
            String originalName = file.getOriginalFilename();
            String extension = originalName.substring(originalName.lastIndexOf("."));
            String storedName = UUID.randomUUID() + extension;

            Path path = Paths.get(uploadPath);
            Files.createDirectories(path);
            Files.copy(file.getInputStream(), path.resolve(storedName), StandardCopyOption.REPLACE_EXISTING);

            Resource resource = new Resource();
            resource.setFileName(originalName);
            resource.setFileUrl("/files/" + storedName);
            resource.setType(type);
            resource.setDescription(desc);
            resource.setUploadedAt(LocalDateTime.now());

            Optional<LearningPlan> updated = planService.addResourceToPlan(id, resource);
            if (updated.isPresent()) {
                return ResponseEntity.ok(updated.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Plan not found");
            }

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload error");
        }
    }
    @DeleteMapping("/{planId}/resources/{fileName}")
    public ResponseEntity<?> deleteResource(@PathVariable String planId, @PathVariable String fileName) {
        try {
            // Decode file name (in case it has spaces or special characters)
            String decodedFileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);

            Optional<LearningPlan> updated = planService.removeResourceFromPlan(planId, decodedFileName);

            if (updated.isPresent()) {
                // Delete the file from the filesystem
                Path filePath = Paths.get(uploadPath).resolve(decodedFileName).normalize();
                Files.deleteIfExists(filePath);
                return ResponseEntity.ok(updated.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Plan or resource not found");
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete file");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid file name");
        }
    }




}
