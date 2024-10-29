package com.example.exodia.qna.controller;


import com.example.exodia.qna.dto.ManagerListDto;
import com.example.exodia.qna.dto.ManagerSaveDto;
import com.example.exodia.qna.service.ManagerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/manager")
public class ManagerController {

    private ManagerService managerService;

    public ManagerController(ManagerService managerService) {
        this.managerService = managerService;
    }

    // 매니저 추가
    @PostMapping("/save")
    public ResponseEntity<ManagerListDto> saveManager(@RequestBody ManagerSaveDto managerSaveDto) {
        ManagerListDto managerDto = managerService.saveManager(managerSaveDto);
        return ResponseEntity.ok(managerDto);
    }


    // 매니저 삭제
    @DeleteMapping("/delete/{userNum}")
    public ResponseEntity<Void> deleteManager(@PathVariable String userNum) {
        managerService.deleteManager(userNum);
        return ResponseEntity.noContent().build();
    }


    // 매니저 목록 조회
    @GetMapping("/list")
    public ResponseEntity<List<ManagerListDto>> getAllManagers() {
        List<ManagerListDto> managers = managerService.getAllManagers();
        return ResponseEntity.ok(managers);
    }

    @GetMapping("/is-manager/{userNum}")
    public ResponseEntity<Boolean> isManager(@PathVariable String userNum) {
        boolean isManager = managerService.isManager(userNum);
        return ResponseEntity.ok(isManager);
    }
}
