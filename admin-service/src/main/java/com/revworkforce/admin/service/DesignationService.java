package com.revworkforce.admin.service;

import com.revworkforce.admin.dto.DesignationRequest;
import com.revworkforce.admin.dto.DesignationResponse;
import com.revworkforce.admin.entity.Designation;
import com.revworkforce.admin.exception.DuplicateResourceException;
import com.revworkforce.admin.exception.ResourceNotFoundException;
import com.revworkforce.admin.repository.DesignationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DesignationService {

    private static final Logger log = LoggerFactory.getLogger(DesignationService.class);

    private final DesignationRepository designationRepository;

    public DesignationService(DesignationRepository designationRepository) {
        this.designationRepository = designationRepository;
    }

    public List<DesignationResponse> getAll() {
        log.debug("Get all designations");
        return designationRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public DesignationResponse getById(Long id) {
        log.debug("Get designation id={}", id);
        Designation designation = designationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Designation not found with id: " + id));
        return toResponse(designation);
    }

    @Transactional
    public DesignationResponse create(DesignationRequest request) {
        log.info("Create designation name={}", request.getName());
        if (designationRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Designation with name '" + request.getName() + "' already exists");
        }

        Designation designation = Designation.builder()
                .name(request.getName())
                .build();

        Designation saved = designationRepository.save(designation);
        log.info("Create designation success id={}", saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Delete designation id={}", id);
        if (!designationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Designation not found with id: " + id);
        }
        designationRepository.deleteById(id);
    }

    private DesignationResponse toResponse(Designation designation) {
        return DesignationResponse.builder()
                .id(designation.getId())
                .name(designation.getName())
                .build();
    }
}
