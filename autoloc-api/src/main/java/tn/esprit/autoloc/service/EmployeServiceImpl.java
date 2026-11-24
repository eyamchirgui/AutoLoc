package tn.esprit.autoloc.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Employe;
import tn.esprit.autoloc.dto.EmployeRequest;
import tn.esprit.autoloc.dto.EmployeResponse;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.mapper.EmployeMapper;
import tn.esprit.autoloc.repository.IEmployeRepository;

@Service
@RequiredArgsConstructor
public class EmployeServiceImpl implements IEmployeService {

    private final IEmployeRepository employeRepository;
    private final EmployeMapper employeMapper;

    @Override
    @Transactional
    public EmployeResponse create(EmployeRequest request) {
        Employe employe = employeMapper.toEntity(request);
        valider(employe);
        return employeMapper.toResponse(employeRepository.save(employe));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeResponse findById(Long id) {
        return employeMapper.toResponse(getOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeResponse> findAll() {
        return employeRepository.findAll().stream()
                .map(employeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public EmployeResponse update(Long id, EmployeRequest request) {
        Employe existant = getOrThrow(id);
        Employe donnees = employeMapper.toEntity(request);
        valider(donnees);
        existant.setNom(donnees.getNom());
        existant.setPrenom(donnees.getPrenom());
        existant.setRole(donnees.getRole());
        existant.setAgence(donnees.getAgence());
        return employeMapper.toResponse(employeRepository.save(existant));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!employeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employe", id);
        }
        employeRepository.deleteById(id);
    }

    private Employe getOrThrow(Long id) {
        return employeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employe", id));
    }

    private void valider(Employe employe) {
        if (employe.getNom() == null || employe.getNom().isBlank()
                || employe.getPrenom() == null || employe.getPrenom().isBlank()) {
            throw new IllegalArgumentException("Le nom et le prénom de l'employé sont obligatoires");
        }
        if (employe.getRole() == null) {
            throw new IllegalArgumentException("Le rôle de l'employé est obligatoire");
        }
    }
}