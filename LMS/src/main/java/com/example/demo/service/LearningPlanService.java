package com.example.demo.service;

import com.example.demo.model.LearningPlan;
import com.example.demo.model.Resource;
import com.example.demo.repository.LearningPlanRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LearningPlanService {

    private final LearningPlanRepository planRepository;

    public LearningPlanService(LearningPlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public LearningPlan createPlan(LearningPlan plan) {
        return planRepository.save(plan);
    }

    public List<LearningPlan> getAllPlans() {
        return planRepository.findAll();
    }

    public Optional<LearningPlan> getPlanById(String id) {
        return planRepository.findById(id);
    }

    public Optional<LearningPlan> updatePlan(String id, LearningPlan updatedPlan) {
        return planRepository.findById(id).map(existing -> {
            existing.setTitle(updatedPlan.getTitle());
            existing.setTopics(updatedPlan.getTopics());
            existing.setTopicsCovered(updatedPlan.getTopicsCovered());
            existing.setProgress(updatedPlan.getProgress());
            existing.setStatus(updatedPlan.getStatus());
            existing.setStartDate(updatedPlan.getStartDate());
            existing.setEndDate(updatedPlan.getEndDate());
            existing.setDescription(updatedPlan.getDescription());
            return planRepository.save(existing);
        });
    }

    public boolean deletePlan(String id) {
        if (planRepository.existsById(id)) {
            planRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Optional<LearningPlan> addResourceToPlan(String id, Resource resource) {
        return planRepository.findById(id).map(plan -> {
            plan.getResources().add(resource);
            return planRepository.save(plan);
        });
    }
    public Optional<LearningPlan> removeResourceFromPlan(String planId, String fileName) {
        return planRepository.findById(planId).map(plan -> {
            boolean removed = plan.getResources().removeIf(res -> res.getFileName().equalsIgnoreCase(fileName));
            if (removed) {
                return planRepository.save(plan); // returns LearningPlan
            }
            return plan; // still returns LearningPlan
        });
    }


}
