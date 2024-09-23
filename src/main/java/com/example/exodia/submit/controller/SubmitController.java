package com.example.exodia.submit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.exodia.submit.service.SubmitService;

@RestController
@RequestMapping("/submit")
public class SubmitController {

	private final SubmitService submitService;

	@Autowired
	public SubmitController(SubmitService submitService) {
		this.submitService = submitService;
	}
}
