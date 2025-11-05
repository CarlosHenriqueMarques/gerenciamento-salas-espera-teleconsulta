package br.com.carlos.teleconsulta.service;

import br.com.carlos.teleconsulta.domain.UnidadeSaude;
import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class UnidadeSaudeService {

    @PersistenceContext(unitName = "teleconsultaPU")
    private EntityManager em;

    public List<UnidadeSaude> listarTodos() {
        return em.createQuery("select u from UnidadeSaude u order by u.id desc", UnidadeSaude.class)
                .getResultList();
    }

    public UnidadeSaude encontrar(Long id) {
        return em.find(UnidadeSaude.class, id);
    }

    public UnidadeSaude salvar(UnidadeSaude u) {
        if (u.getId() == null) {
            em.persist(u);
            return u;
        }
        return em.merge(u);
    }

    public void excluir(Long id) {
        UnidadeSaude ref = em.getReference(UnidadeSaude.class, id);
        em.remove(ref);
    }

    public boolean cnpjExiste(String cnpj, Long ignorarId) {
        String jpql = "select count(u) from UnidadeSaude u where u.cnpj = :cnpj";
        if (ignorarId != null) {
            jpql += " and u.id <> :id";
        }
        TypedQuery<Long> q = em.createQuery(jpql, Long.class)
                .setParameter("cnpj", cnpj);
        if (ignorarId != null) {
            q.setParameter("id", ignorarId);
        }
        Long qtd = q.getSingleResult();
        return qtd != null && qtd > 0L;
    }

    public boolean cnesExiste(String cnes, Long ignorarId) {
        String jpql = "select count(u) from UnidadeSaude u where u.cnes = :cnes";
        if (ignorarId != null) {
            jpql += " and u.id <> :id";
        }
        TypedQuery<Long> q = em.createQuery(jpql, Long.class)
                .setParameter("cnes", cnes);
        if (ignorarId != null) {
            q.setParameter("id", ignorarId);
        }
        Long qtd = q.getSingleResult();
        return qtd != null && qtd > 0L;
    }

    public List<UnidadeSaude> buscarPorFiltro(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            return listarTodos();
        }
        String q = "%" + filtro.trim().toLowerCase() + "%";

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<UnidadeSaude> cq = cb.createQuery(UnidadeSaude.class);
        Root<UnidadeSaude> root = cq.from(UnidadeSaude.class);

        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.like(cb.lower(root.get("nome")), q));
        preds.add(cb.like(cb.lower(root.get("razaoSocial")), q));
        preds.add(cb.like(cb.lower(root.get("sigla")), q));

        cq.select(root)
                .where(cb.or(preds.toArray(new Predicate[0])))
                .orderBy(cb.desc(root.get("id")));

        return em.createQuery(cq).getResultList();
    }
}
