package tn.esprit.autoloc.web.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.autoloc.dto.PaiementResponse;
import tn.esprit.autoloc.service.IPaiementService;

@RestController
@RequestMapping("/paiements")
@RequiredArgsConstructor
public class PaiementController {

    private final IPaiementService paiementService;

    @GetMapping("/{id}")
    public PaiementResponse findById(@PathVariable Long id) {
        return paiementService.findById(id);
    }

    @GetMapping
    public List<PaiementResponse> findAll() {
        return paiementService.findAll();
    }
}