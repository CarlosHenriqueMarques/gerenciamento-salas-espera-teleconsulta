package br.com.carlos.teleconsulta.service;

import br.com.carlos.teleconsulta.domain.Reserva;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class ReservaService {

    @PersistenceContext(unitName = "teleconsultaPU")
    private EntityManager em;

    /** Lista para a grid: já traz Sala, Unidade e Usuarios para evitar LazyInitializationException */
    public List<Reserva> listarTodos() {
        String jpql =
                "select distinct r " +
                        "from Reserva r " +
                        "join fetch r.sala " +
                        "join fetch r.sala.unidadeSaude " +
                        "left join fetch r.usuarios " +
                        "order by r.inicio desc";
        return em.createQuery(jpql, Reserva.class).getResultList();
    }

    /** Busca com filtros (todos opcionais), com fetch dos relacionamentos usados na tela */
    public List<Reserva> buscar(LocalDateTime ini, LocalDateTime fim,
                                Long unidadeId, Long salaId, Long usuarioId) {

        StringBuilder jpql = new StringBuilder(
                "select distinct r " +
                        "from Reserva r " +
                        "join fetch r.sala " +
                        "join fetch r.sala.unidadeSaude " +
                        "left join fetch r.usuarios " +
                        "where 1=1 ");

        if (ini != null)       jpql.append("and r.inicio >= :ini ");
        if (fim != null)       jpql.append("and r.fim <= :fim ");
        if (unidadeId != null) jpql.append("and r.sala.unidadeSaude.id = :uid ");
        if (salaId != null)    jpql.append("and r.sala.id = :sid ");
        if (usuarioId != null) {
            // Filtra reservas que contenham o usuário
            jpql.append("and exists (select 1 from Reserva r2 join r2.usuarios u where r2 = r and u.id = :usid) ");
        }
        jpql.append("order by r.inicio desc");

        TypedQuery<Reserva> q = em.createQuery(jpql.toString(), Reserva.class);
        if (ini != null)       q.setParameter("ini", ini);
        if (fim != null)       q.setParameter("fim", fim);
        if (unidadeId != null) q.setParameter("uid", unidadeId);
        if (salaId != null)    q.setParameter("sid", salaId);
        if (usuarioId != null) q.setParameter("usid", usuarioId);

        return q.getResultList();
    }

    /** Busca uma reserva completa por id (inclui sala, unidade e usuários) para edição/visualização */
    public Reserva encontrarComUsuarios(Long id) {
        String jpql =
                "select r " +
                        "from Reserva r " +
                        "join fetch r.sala " +
                        "join fetch r.sala.unidadeSaude " +
                        "left join fetch r.usuarios " +
                        "where r.id = :id";
        List<Reserva> lista = em.createQuery(jpql, Reserva.class)
                .setParameter("id", id)
                .getResultList();
        return lista.isEmpty() ? null : lista.get(0);
    }

    /** Versão simples (sem fetch) — use se precisar em cenários específicos */
    public Reserva encontrar(Long id) {
        return em.find(Reserva.class, id);
    }

    /** Verifica conflito de horário na mesma sala.
     *  Regra de sobreposição: (inicio < fimExistente) e (fim > inicioExistente).
     *  ignorarId: use ao editar para desconsiderar a própria reserva.
     */
    public boolean existeConflito(Long salaId, LocalDateTime inicio, LocalDateTime fim, Long ignorarId) {
        StringBuilder jpql = new StringBuilder(
                "select count(r) " +
                        "from Reserva r " +
                        "where r.sala.id = :salaId " +
                        "and r.inicio < :fim " +   // começa antes do fim novo
                        "and r.fim > :inicio ");   // termina depois do início novo

        if (ignorarId != null) {
            jpql.append("and r.id <> :ignorarId ");
        }

        var q = em.createQuery(jpql.toString(), Long.class)
                .setParameter("salaId", salaId)
                .setParameter("inicio", inicio)
                .setParameter("fim", fim);

        if (ignorarId != null) {
            q.setParameter("ignorarId", ignorarId);
        }

        Long count = q.getSingleResult();
        return count != null && count > 0L;
    }

    public Reserva salvar(Reserva r) {
        if (r.getId() == null) {
            em.persist(r);
            return r;
        }
        return em.merge(r);
    }

    public void excluir(Long id) {
        Reserva ref = em.getReference(Reserva.class, id);
        em.remove(ref);
    }
}
