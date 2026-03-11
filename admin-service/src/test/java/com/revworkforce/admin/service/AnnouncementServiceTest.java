package com.revworkforce.admin.service;

import com.revworkforce.admin.dto.AnnouncementRequest;
import com.revworkforce.admin.dto.AnnouncementResponse;
import com.revworkforce.admin.entity.Announcement;
import com.revworkforce.admin.repository.AnnouncementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceTest {

    @Mock
    private AnnouncementRepository announcementRepository;

    @InjectMocks
    private AnnouncementService announcementService;

    @Test
    void createPersistsAnnouncementAndReturnsResponse() {
        AnnouncementRequest request = new AnnouncementRequest("Townhall", "Company-wide update");
        Announcement saved = Announcement.builder()
                .id(1L)
                .title("Townhall")
                .content("Company-wide update")
                .createdAt(LocalDateTime.now())
                .build();

        when(announcementRepository.save(any(Announcement.class))).thenReturn(saved);

        AnnouncementResponse response = announcementService.create(request);

        assertEquals(1L, response.getId());
        assertEquals("Townhall", response.getTitle());
        assertEquals("Company-wide update", response.getContent());
    }
}
