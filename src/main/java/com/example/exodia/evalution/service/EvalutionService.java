package com.example.exodia.evalution.service;

import com.example.exodia.evalution.repository.EvalutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EvalutionService {
    @Autowired
    private final EvalutionRepository evalutionRepository;

    public EvalutionService(EvalutionRepository evalutionRepository) {
        this.evalutionRepository = evalutionRepository;
    }


}
