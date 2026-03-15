package com.revworkforce.admin.service;

import com.revworkforce.admin.dto.HolidayRequest;
import com.revworkforce.admin.dto.HolidayResponse;
import com.revworkforce.admin.entity.Holiday;
import com.revworkforce.admin.exception.ResourceNotFoundException;
import com.revworkforce.admin.repository.HolidayRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HolidayService {

    private static final Logger log = LoggerFactory.getLogger(HolidayService.class);

    private final HolidayRepository holidayRepository;

    public HolidayService(HolidayRepository holidayRepository) {
        this.holidayRepository = holidayRepository;
    }

    public List<HolidayResponse> getAll() {
        log.debug("Get all holidays");
        return holidayRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<HolidayResponse> getByYear(int year) {
        log.debug("Get holidays year={}", year);
        return holidayRepository.findByYear(year).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public HolidayResponse create(HolidayRequest request) {
        log.info("Create holiday name={} date={}", request.getName(), request.getDate());
        Holiday holiday = Holiday.builder()
                .name(request.getName())
                .date(request.getDate())
                .build();

        Holiday saved = holidayRepository.save(holiday);
        log.info("Create holiday success id={}", saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Delete holiday id={}", id);
        if (!holidayRepository.existsById(id)) {
            throw new ResourceNotFoundException("Holiday not found with id: " + id);
        }
        holidayRepository.deleteById(id);
    }

    private HolidayResponse toResponse(Holiday holiday) {
        return HolidayResponse.builder()
                .id(holiday.getId())
                .name(holiday.getName())
                .date(holiday.getDate())
                .build();
    }
}
