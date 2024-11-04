package com.example.exodia.evalutionFrame.subevalution.controller;

import com.example.exodia.common.dto.CommonResDto;
import com.example.exodia.evalutionFrame.subevalution.dto.SubEvalutionDto;
import com.example.exodia.evalutionFrame.subevalution.dto.SubEvalutionResponseDto;
import com.example.exodia.evalutionFrame.subevalution.dto.SubEvalutionWithUserDetailsDto;
import com.example.exodia.evalutionFrame.subevalution.service.SubEvalutionService;
import com.example.exodia.evalutionFrame.subevalution.domain.SubEvalution;
import com.example.exodia.evalutionFrame.subevalution.dto.SubEvalutionUpdateDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/sub-evalution")
public class SubEvalutionController {
    private final SubEvalutionService subEvalutionService;

    public SubEvalutionController(SubEvalutionService subEvalutionService) {
        this.subEvalutionService = subEvalutionService;
    }

    @PostMapping("/create")
    public ResponseEntity<SubEvalution> createSubEvalution(@RequestBody SubEvalutionDto subEvalutionDto) {
        SubEvalution createdSubEvalution = subEvalutionService.createSubEvalution(subEvalutionDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSubEvalution);
    }

    // 소분류 조회 (로그인한 사용자가 작성한 모든 소분류)
    @GetMapping("/list")
    public ResponseEntity<List<SubEvalution>> getAllSubEvalutionsForUser() {
        List<SubEvalution> subEvalutions = subEvalutionService.getAllSubEvalutionsForUser();
        return ResponseEntity.ok(subEvalutions);
    }

    /* 소분류 수정 */
    @PutMapping("/update/{id}")
    public ResponseEntity<SubEvalution> updateSubEvalution(@PathVariable Long id, @RequestBody SubEvalutionUpdateDto subEvalutionUpdateDto) {
        subEvalutionUpdateDto.setId(id);
        SubEvalution updatedSubEvalution = subEvalutionService.updateSubEvalution(subEvalutionUpdateDto);
        return ResponseEntity.ok(updatedSubEvalution);
    }

    /* 소분류 삭제 */
    //  소분류의 경우 일정 기간이 지나면 사라지는 휘발성 데이터를 지향, 따로 소프트 del 적용x
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteSubEvalution(@PathVariable Long id) {
        subEvalutionService.deleteSubEvalution(id);
        return ResponseEntity.noContent().build();
    }

    // 소분류 조회 (대분류명 및 중분류명 포함)
    @GetMapping("/list-with-categories")
    public ResponseEntity<List<SubEvalutionResponseDto>> getAllSubEvalutionsWithCategoriesForUser() {
        List<SubEvalutionResponseDto> subEvalutions = subEvalutionService.getAllSubEvalutionsWithCategoriesForUser();
        return ResponseEntity.ok(subEvalutions);
    }

    @GetMapping("/team-evaluations/{userNum}")
    public ResponseEntity<List<SubEvalutionWithUserDetailsDto>> getTeamMembersSubEvalutions(@PathVariable String userNum) {
        List<SubEvalutionWithUserDetailsDto> evalutions = subEvalutionService.getTeamMembersSubEvalutions(userNum);
        return ResponseEntity.ok(evalutions);
    }

    // 일괄 저장 기능
    @PostMapping("/create-multiple")
    public ResponseEntity<List<SubEvalution>> createMultipleSubEvalutions(@RequestBody List<SubEvalutionDto> subEvalutionDtoList) {
        List<SubEvalution> createdSubEvalutions = subEvalutionService.createMultipleSubEvalutions(subEvalutionDtoList);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSubEvalutions);
    }

    // 인사 평가를 위한 하위 부서 직원들 조회
    @GetMapping("/user/list")
    public ResponseEntity<?> getTeamMembersSubEvalutions() throws IOException {
        List<?> evalutions = subEvalutionService.getDepartmentChilUsers();
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "하위 부서 직원들 조회 성공", evalutions));
    }

}