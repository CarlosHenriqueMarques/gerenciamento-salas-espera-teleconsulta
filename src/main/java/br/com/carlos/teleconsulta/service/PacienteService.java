package br.com.carlos.teleconsulta.service;

import br.com.carlos.teleconsulta.domain.Paciente;
import jakarta.ejb.Stateless;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

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
        StringBuilder jpql = new StringBuilder("select p from Paciente p where 1=1");

        boolean hasTermo = termo != null && !termo.isBlank();
        if (hasTermo) {
            jpql.append("""
                and (
                       lower(p.nome) like :kw
                    or lower(p.nomeSocial) like :kw
                    or lower(p.email) like :kw
                    or lower(p.rg) like :kw
                    or p.cpf like :kwNum
                    or p.cns like :kwNum
                    or p.telefone like :kwNum
                )
            """);
        }
        if (nascIni != null) jpql.append(" and p.dataNascimento >= :nascIni");
        if (nascFim != null) jpql.append(" and p.dataNascimento <= :nascFim");

        jpql.append(" order by p.id desc");

        TypedQuery<Paciente> q = em.createQuery(jpql.toString(), Paciente.class);

        if (hasTermo) {
            String kw = "%" + termo.toLowerCase().trim() + "%";
            String kwNum = "%" + termo.replaceAll("\\D", "") + "%";
            q.setParameter("kw", kw);
            q.setParameter("kwNum", kwNum);
        }
        if (nascIni != null) q.setParameter("nascIni", nascIni);
        if (nascFim != null) q.setParameter("nascFim", nascFim);

        return q.getResultList();
    }

    public Paciente salvar(Paciente p) {
        // checa duplicidade de documentos (apenas se informados)
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
