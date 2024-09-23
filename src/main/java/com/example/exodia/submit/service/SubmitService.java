package com.example.exodia.submit.service;

import org.springframework.stereotype.Service;

import com.example.exodia.submit.repository.SubmitRepository;

@Service
public class SubmitService {

	private final SubmitRepository submitRepository;

	public SubmitService(SubmitRepository submitRepository) {
		this.submitRepository = submitRepository;
	}
}
