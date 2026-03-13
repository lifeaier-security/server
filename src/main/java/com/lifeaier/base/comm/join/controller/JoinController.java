package com.lifeaier.base.comm.join.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.lifeaier.base.comm.join.dto.JoinDTO;
import com.lifeaier.base.comm.join.service.JoinService;

@RestController
public class JoinController {

    private final JoinService joinService;

    public JoinController(JoinService joinService) {

        this.joinService = joinService;
    }

    @PostMapping("/join")
    public ResponseEntity<?> join(@RequestBody JoinDTO dto) {
        try {
            joinService.join(dto);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
