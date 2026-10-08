package tn.esprit.autoloc;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.domain.CategorieVehicule;
import tn.esprit.autoloc.domain.Client;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.service.IAgenceService;
import tn.esprit.autoloc.service.IClientService;
import tn.esprit.autoloc.service.IVehiculeService;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DemoRunner implements CommandLineRunner {

    private final IClientService clientService;
    private final IVehiculeService vehiculeService;
    private final IAgenceService agenceService;

    @Override
    public void run(String... args) {

        System.out.println("========== CRUD CLIENT ==========");

        Client c = new Client();
        c.setNom("Ben Ali");
        c.setPrenom("Sami");
        c.setEmail("sami" + System.currentTimeMillis() + "@mail.com");
        c.setTelephone("20123456");
        c.setNumPermis("P" + System.currentTimeMillis());

        Client saved = clientService.create(c);
        System.out.println("CREATE : id = " + saved.getIdClient());

        Client lu = clientService.getById(saved.getIdClient());
        System.out.println("READ   : " + lu.getNom() + " " + lu.getPrenom());

        lu.setTelephone("99999999");
        Client modifie = clientService.update(lu.getIdClient(), lu);
        System.out.println("UPDATE : telephone = " + modifie.getTelephone());

        System.out.println("READ ALL : " + clientService.getAll().size() + " client(s)");

        clientService.delete(saved.getIdClient());
        System.out.println("DELETE : client " + saved.getIdClient() + " supprimé");

        System.out.println("========== CRUD VEHICULE ==========");

        Agence agence = new Agence();
        agence.setNom("Agence Tunis");
        agence.setVille("Tunis");
        agence.setAdresse("Avenue Habib Bourguiba");
        Agence agenceSaved = agenceService.create(agence);

        Vehicule v = new Vehicule();
        v.setImmatriculation("TUN-" + System.currentTimeMillis());
        v.setMarque("Peugeot");
        v.setModele("208");
        v.setCategorie(CategorieVehicule.CITADINE);
        v.setTarifJournalier(new BigDecimal("80.00"));

        Vehicule vs = vehiculeService.create(v, agenceSaved.getIdAgence());
        System.out.println("CREATE : id = " + vs.getIdVehicule());

        Vehicule vl = vehiculeService.getById(vs.getIdVehicule());
        System.out.println("READ   : " + vl.getMarque() + " " + vl.getModele());

        vl.setTarifJournalier(new BigDecimal("95.00"));
        Vehicule vm = vehiculeService.update(vl.getIdVehicule(), vl);
        System.out.println("UPDATE : tarif = " + vm.getTarifJournalier());

        System.out.println("READ ALL : " + vehiculeService.getAll().size() + " véhicule(s)");

        vehiculeService.delete(vs.getIdVehicule());
        System.out.println("DELETE : véhicule " + vs.getIdVehicule() + " supprimé");

        agenceService.delete(agenceSaved.getIdAgence());
        System.out.println("Agence de test supprimée");
    }
}