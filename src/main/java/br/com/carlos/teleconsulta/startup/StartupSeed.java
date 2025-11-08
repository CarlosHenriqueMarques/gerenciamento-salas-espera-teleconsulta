package br.com.carlos.teleconsulta.startup;

import br.com.carlos.teleconsulta.service.ContaService;
import br.com.carlos.teleconsulta.service.PasswordService;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

@Singleton
@Startup
public class StartupSeed {

    @Inject
    private ContaService contaService;

    @Inject
    private PasswordService passwordService;

    @PostConstruct
    public void init() {
        // Cria admin/admin caso não exista
        String hash = passwordService.hash("admin");
        contaService.garantirAdmin(hash);
    }
}
