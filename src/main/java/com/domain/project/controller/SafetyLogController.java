package com.domain.project.controller;

import com.domain.project.dto.SafetyLogDto;
import com.domain.project.service.SafetyLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/safety-log")
public class SafetyLogController {

    private final SafetyLogService safetyLogService;

    public SafetyLogController(SafetyLogService safetyLogService) {
        this.safetyLogService = safetyLogService;
    }

    @PostMapping
    public ResponseEntity<SafetyLogDto> createLog(@RequestBody SafetyLogDto dto, Principal principal) {
        String username = principal.getName();
        SafetyLogDto createdDto = safetyLogService.createLog(dto, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDto);
    }

    @GetMapping
    public ResponseEntity<List<SafetyLogDto>> getAllLogs(Principal principal) {
        String username = principal.getName();
        List<SafetyLogDto> dtos = safetyLogService.findAllMyLogs(username);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SafetyLogDto> getLog(@PathVariable Long id) {
        return ResponseEntity.ok(safetyLogService.getLogById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SafetyLogDto> updateLog(@PathVariable Long id, @RequestBody SafetyLogDto dto) {
        return ResponseEntity.ok(safetyLogService.updateLog(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLog(@PathVariable Long id) {
        safetyLogService.deleteLog(id);
        return ResponseEntity.noContent().build();
    }
}