package br.com.carlos.teleconsulta.service;

import br.com.carlos.teleconsulta.domain.Paciente;
import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Stateless
public class PacienteService {

    @PersistenceContext(unitName = "teleconsultaPU")
    private EntityManager em;

    public Paciente encontrar(Long id) {
        return em.find(Paciente.class, id);
    }

    public List<Paciente> listarTodos() {
        return em.createQuery("select p from Paciente p order by p.id desc", Paciente.class)
                .getResultList();
    }

    public List<Paciente> buscar(String termo, LocalDate nascIni, LocalDate nascFim) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Paciente> cq = cb.createQuery(Paciente.class);
        Root<Paciente> root = cq.from(Paciente.class);

        List<Predicate> ands = new ArrayList<>();

        if (termo != null) {
            String t = termo.trim().toLowerCase(Locale.ROOT);
            if (!t.isEmpty()) {
                String like = "%" + t + "%";
                Predicate porNome       = cb.like(cb.lower(root.get("nome")), like);
                Predicate porNomeSocial = cb.like(cb.lower(root.get("nomeSocial")), like);
                Predicate porEmail      = cb.like(cb.lower(root.get("email")), like);
                ands.add(cb.or(porNome, porNomeSocial, porEmail));
            }
        }

        if (nascIni != null) {
            ands.add(cb.greaterThanOrEqualTo(root.get("dataNascimento"), nascIni));
        }
        if (nascFim != null) {
            ands.add(cb.lessThanOrEqualTo(root.get("dataNascimento"), nascFim));
        }

        cq.select(root)
                .where(ands.toArray(new Predicate[0]))
                .orderBy(cb.desc(root.get("id")));

        return em.createQuery(cq).getResultList();
    }

    public Paciente salvar(Paciente p) {
        if (p.getCpf() != null && !p.getCpf().isBlank() && cpfExiste(p.getCpf(), p.getId())) {
            throw new IllegalArgumentException("CPF já cadastrado.");
        }
        if (p.getCns() != null && !p.getCns().isBlank() && cnsExiste(p.getCns(), p.getId())) {
            throw new IllegalArgumentException("CNS já cadastrado.");
        }

        if (p.getId() == null) {
            em.persist(p);
            return p;
        }
        return em.merge(p);
    }

    public void excluir(Long id) {
        Paciente ref = em.getReference(Paciente.class, id);
        em.remove(ref);
    }

    public boolean cpfExiste(String cpf, Long ignorarId) {
        String jpql = "select count(p) from Paciente p where p.cpf = :cpf"
                + (ignorarId != null ? " and p.id <> :id" : "");
        TypedQuery<Long> q = em.createQuery(jpql, Long.class)
                .setParameter("cpf", cpf);
        if (ignorarId != null) q.setParameter("id", ignorarId);
        Long c = q.getSingleResult();
        return c != null && c > 0;
    }

    public boolean cnsExiste(String cns, Long ignorarId) {
        String jpql = "select count(p) from Paciente p where p.cns = :cns"
                + (ignorarId != null ? " and p.id <> :id" : "");
        TypedQuery<Long> q = em.createQuery(jpql, Long.class)
                .setParameter("cns", cns);
        if (ignorarId != null) q.setParameter("id", ignorarId);
        Long c = q.getSingleResult();
        return c != null && c > 0;
    }
}
