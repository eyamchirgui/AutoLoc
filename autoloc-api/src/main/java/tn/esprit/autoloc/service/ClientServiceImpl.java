package tn.esprit.autoloc.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Client;
import tn.esprit.autoloc.dto.ClientRequest;
import tn.esprit.autoloc.dto.ClientResponse;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.mapper.ClientMapper;
import tn.esprit.autoloc.repository.IClientRepository;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements IClientService {

    private final IClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @Override
    @Transactional
    public ClientResponse create(ClientRequest request) {
        Client client = clientMapper.toEntity(request);
        valider(client);
        client.setDateInscription(LocalDate.now());
        return clientMapper.toResponse(clientRepository.save(client));
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse findById(Long id) {
        return clientMapper.toResponse(getOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientResponse> findAll() {
        return clientRepository.findAll().stream()
                .map(clientMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ClientResponse update(Long id, ClientRequest request) {
        Client existant = getOrThrow(id);
        Client donnees = clientMapper.toEntity(request);
        valider(donnees);
        existant.setNom(donnees.getNom());
        existant.setPrenom(donnees.getPrenom());
        existant.setEmail(donnees.getEmail());
        existant.setTelephone(donnees.getTelephone());
        existant.setNumPermis(donnees.getNumPermis());
        return clientMapper.toResponse(clientRepository.save(existant));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Client", id);
        }
        clientRepository.deleteById(id);
    }

    private Client getOrThrow(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));
    }

    private void valider(Client client) {
        if (client.getNom() == null || client.getNom().isBlank()
                || client.getPrenom() == null || client.getPrenom().isBlank()) {
            throw new IllegalArgumentException("Le nom et le prénom du client sont obligatoires");
        }
        if (client.getEmail() == null || client.getEmail().isBlank()) {
            throw new IllegalArgumentException("L'e-mail du client est obligatoire");
        }
    }
}