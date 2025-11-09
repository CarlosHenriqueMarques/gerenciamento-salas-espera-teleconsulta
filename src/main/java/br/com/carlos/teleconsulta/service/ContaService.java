package br.com.carlos.teleconsulta.service;

import br.com.carlos.teleconsulta.domain.Conta;
import br.com.carlos.teleconsulta.domain.enums.Perfil;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

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

    public List<Conta> listarTodos() {
        return em.createQuery("select c from Conta c left join fetch c.usuario order by c.id desc", Conta.class)
                .getResultList();
    }

    public Conta encontrar(Long id) {
        return em.find(Conta.class, id);
    }

    public void excluir(Long id) {
        Conta ref = em.getReference(Conta.class, id);
        em.remove(ref);
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

    public boolean loginExiste(String login, Long ignorarId) {
        if (login == null) return false;
        Long qtd = em.createQuery(
                        "select count(c) from Conta c " +
                                "where lower(c.login) = :l " +
                                "and (:id is null or c.id <> :id)", Long.class)
                .setParameter("l", login.toLowerCase())
                .setParameter("id", ignorarId)
                .getSingleResult();
        return qtd != null && qtd > 0;
    }

    public List<Conta> buscarPorFiltro(String login, Perfil perfil) {
        String l = (login == null || login.isBlank()) ? null : "%" + login.toLowerCase() + "%";
        return em.createQuery(
                        "select c from Conta c left join fetch c.usuario " +
                                "where (:l is null or lower(c.login) like :l) " +
                                "and   (:p is null or c.perfil = :p) " +
                                "order by c.id desc", Conta.class)
                .setParameter("l", l)
                .setParameter("p", perfil)
                .getResultList();
    }
}
