package com.name.match.controller;

import com.name.match.service.NameMatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class NameMatchController {

    private final NameMatchService nameMatchService;

    @Autowired
    public NameMatchController(NameMatchService nameMatchService) {
        this.nameMatchService = nameMatchService;
    }

    @GetMapping("/nameMatchScore")
    public ResponseEntity<Map<String, Object>> nameMatchScore(
            @RequestParam(required = false) String name1,
            @RequestParam(required = false) String name2) {
        
        Map<String, Object> result = nameMatchService.mainFunction(name1, name2);
        return ResponseEntity.ok(result);
    }
} 