package br.com.carlos.teleconsulta.service;

import br.com.carlos.teleconsulta.domain.Conta;
import br.com.carlos.teleconsulta.domain.enums.Perfil;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

@Stateless
public class ContaService {

    @PersistenceContext(unitName = "teleconsultaPU")
    private EntityManager em;

    public Conta buscarPorLogin(String login) {
        TypedQuery<Conta> q = em.createQuery(
                "select c from Conta c where lower(c.login) = :l", Conta.class);
        q.setParameter("l", login == null ? null : login.toLowerCase());
        return q.getResultStream().findFirst().orElse(null);
    }

    public boolean existeLogin(String login) {
        return buscarPorLogin(login) != null;
    }

    public Conta salvar(Conta c) {
        if (c.getId() == null) {
            em.persist(c);
            return c;
        }
        return em.merge(c);
    }

    public Conta validarLogin(String login, String senhaPlain, PasswordService pass) {
        Conta c = buscarPorLogin(login);
        if (c == null || !c.isAtivo()) return null;
        if (!pass.verify(senhaPlain, c.getSenhaHash())) return null;
        return c;
    }

    // Seed (admin/admin)
    public void garantirAdmin(String senhaHashBCrypt) {
        if (!existeLogin("admin")) {
            Conta admin = new Conta();
            admin.setLogin("admin");
            admin.setSenhaHash(senhaHashBCrypt);
            admin.setPerfil(Perfil.ADMIN);
            admin.setAtivo(true);
            em.persist(admin);
        }
    }
}
