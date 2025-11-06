package br.com.carlos.teleconsulta.service;

import br.com.carlos.teleconsulta.domain.Sala;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;

@Stateless
public class SalaService {

    @PersistenceContext(unitName = "teleconsultaPU")
    private EntityManager em;

    public Sala encontrar(Long id) {
        return em.find(Sala.class, id);
    }

    public List<Sala> listarTodos() {
        // fetch join SEM alias (válido em JPA)
        return em.createQuery(
                "select s from Sala s " +
                        "join fetch s.unidadeSaude " +
                        "order by s.id desc",
                Sala.class
        ).getResultList();
    }

    public List<Sala> buscarPorFiltro(String termo, Long unidadeId) {
        String jpql =
                "select s from Sala s " +
                        "join fetch s.unidadeSaude " +
                        "where (:termo is null or lower(s.nome) like :like) " +
                        "and (:uid is null or s.unidadeSaude.id = :uid) " +
                        "order by s.id desc";

        var q = em.createQuery(jpql, Sala.class)
                .setParameter("termo", (termo == null || termo.isBlank()) ? null : termo)
                .setParameter("like", (termo == null || termo.isBlank()) ? "%" : "%" + termo.toLowerCase() + "%")
                .setParameter("uid", unidadeId);

        return q.getResultList();
    }

    public Sala salvar(Sala s) {
        if (s.getId() == null) {
            em.persist(s);
            return s;
        }
        return em.merge(s);
    }

    public void excluir(Long id) {
        Sala ref = em.getReference(Sala.class, id);
        em.remove(ref);
    }

    public List<Sala> listarPorUnidade(Long unidadeId) {
        return em.createQuery(
                        "select s from Sala s " +
                                "where s.unidadeSaude.id = :uid " +
                                "order by s.nome asc", Sala.class)
                .setParameter("uid", unidadeId)
                .getResultList();
    }
    public boolean jaExisteNomeNaUnidade(String nome, Long unidadeId, Long ignorarId) {
        StringBuilder jpql = new StringBuilder("""
                select count(s) from Sala s
                where lower(s.nome) = :nome
                and s.unidadeSaude.id = :unidadeId
                """);

        if (ignorarId != null) {
            jpql.append(" and s.id <> :ignorarId ");
        }

        TypedQuery<Long> q = em.createQuery(jpql.toString(), Long.class)
                .setParameter("nome", nome.trim().toLowerCase())
                .setParameter("unidadeId", unidadeId);

        if (ignorarId != null) {
            q.setParameter("ignorarId", ignorarId);
        }

        Long count = q.getSingleResult();
        return count != null && count > 0L;
    }
}
