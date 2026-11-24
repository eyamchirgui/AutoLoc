package tn.esprit.autoloc.service;

import java.util.List;
import tn.esprit.autoloc.dto.AgenceRequest;
import tn.esprit.autoloc.dto.AgenceResponse;

public interface IAgenceService {

    AgenceResponse create(AgenceRequest request);

    AgenceResponse findById(Long id);

    List<AgenceResponse> findAll();

    AgenceResponse update(Long id, AgenceRequest request);

    void deleteById(Long id);
}