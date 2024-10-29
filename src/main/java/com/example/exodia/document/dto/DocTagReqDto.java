package com.example.exodia.document.dto;

import javax.print.Doc;

import com.example.exodia.department.domain.Department;
import com.example.exodia.document.domain.Tag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocTagReqDto {
	private String tagName;
	private Long departmentId;

	public Tag toEntity(Department department){
		return Tag.builder()
			.tagName(this.getTagName())
			.department(department)
			.build();
	}
}
