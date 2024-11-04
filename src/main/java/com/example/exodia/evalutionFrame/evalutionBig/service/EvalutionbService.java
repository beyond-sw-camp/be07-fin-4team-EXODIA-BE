package com.example.exodia.evalutionFrame.evalutionBig.service;

import com.example.exodia.evalutionFrame.evalutionBig.domain.Evalutionb;
import com.example.exodia.evalutionFrame.evalutionBig.dto.EvalutionbDto;
import com.example.exodia.evalutionFrame.evalutionBig.repository.EvalutionbRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EvalutionbService {
    @Autowired
    private final EvalutionbRepository evalutionbRepository;

    public EvalutionbService(EvalutionbRepository evalutionbRepository) {
        this.evalutionbRepository = evalutionbRepository;
    }

    /*대분류 생성*/
    @Transactional
    public Evalutionb createEvalutionb(EvalutionbDto evalutionbDto) {

        Evalutionb evalutionb = new Evalutionb();
        evalutionb.setBName(evalutionbDto.getBName());

        return evalutionbRepository.save(evalutionb);
    }

    /*대분류 리스트*/
    public List<Evalutionb> getAllEvalutionbs() {
        return evalutionbRepository.findAll();
    }
}