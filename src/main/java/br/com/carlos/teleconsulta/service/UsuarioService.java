package br.com.carlos.teleconsulta.service;

import br.com.carlos.teleconsulta.domain.Usuario;
import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class UsuarioService {

    @PersistenceContext(unitName = "teleconsultaPU")
    private EntityManager em;

    public List<Usuario> listarTodos() {
        return em.createQuery("select u from Usuario u order by u.id desc", Usuario.class)
                .getResultList();
    }

    public Usuario encontrar(Long id) {
        return em.find(Usuario.class, id);
    }

    public Usuario salvar(Usuario u) {
        if (u.getId() == null) {
            em.persist(u);
            return u;
        }
        return em.merge(u);
    }

    public void excluir(Long id) {
        Usuario ref = em.getReference(Usuario.class, id);
        em.remove(ref);
    }

    public boolean cpfExiste(String cpf) {
        Long qtd = em.createQuery("select count(u) from Usuario u where u.cpf = :cpf", Long.class)
                .setParameter("cpf", cpf)
                .getSingleResult();
        return qtd != null && qtd > 0L;
    }

    public List<Usuario> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Usuario> cq = cb.createQuery(Usuario.class);
        Root<Usuario> root = cq.from(Usuario.class);

        List<Predicate> preds = new ArrayList<>();
        if (inicio != null) preds.add(cb.greaterThanOrEqualTo(root.get("dataCadastro"), inicio));
        if (fim != null)    preds.add(cb.lessThanOrEqualTo(root.get("dataCadastro"), fim));

        cq.select(root)
                .where(preds.toArray(new Predicate[0]))
                .orderBy(cb.desc(root.get("id")));
        return em.createQuery(cq).getResultList();
    }
}
