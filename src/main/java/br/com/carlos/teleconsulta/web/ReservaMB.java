package br.com.carlos.teleconsulta.web;

import br.com.carlos.teleconsulta.domain.*;
import br.com.carlos.teleconsulta.service.*;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Named("reservaMB")
@ViewScoped
public class ReservaMB implements Serializable {

    @Inject private ReservaService reservaService;
    @Inject private SalaService salaService;
    @Inject private UnidadeSaudeService unidadeService;
    @Inject private UsuarioService usuarioService;

    // Listagens
    private List<Reserva> reservas = new ArrayList<>();
    private List<UnidadeSaude> unidades = new ArrayList<>();
    private List<Sala> salasDaUnidade = new ArrayList<>();      // usado no filtro (topo da página)
    private List<Sala> salasDaUnidadeSel = new ArrayList<>();   // usado no diálogo (seleção)
    private List<Usuario> usuarios = new ArrayList<>();

    // Entidade em edição
    private Reserva atual = new Reserva();

    // Filtros (topo)
    private LocalDateTime filtroIni;
    private LocalDateTime filtroFim;
    private Long unidadeFiltroId;
    private Long salaFiltroId;
    private Long usuarioFiltroId;

    // Seleção no diálogo
    private Long unidadeSelId;
    private Long salaSelId;
    private List<Long> usuarioSelIds = new ArrayList<>();

    @PostConstruct
    public void init() {
        try { unidades = unidadeService.listarTodos(); } catch (Exception ignored) {}
        try { usuarios = usuarioService.listarTodos(); } catch (Exception ignored) {}
        try { reservas = reservaService.listarTodos(); } catch (Exception ignored) {}
        salasDaUnidade = new ArrayList<>();
        salasDaUnidadeSel = new ArrayList<>();
    }
    public void novo() {
        atual = new Reserva();
        unidadeSelId = null;
        salaSelId = null;
        usuarioSelIds = new ArrayList<>();
        salasDaUnidadeSel = new ArrayList<>();
    }

    public void editar(Reserva r) {
        Reserva carregada = reservaService.encontrarComUsuarios(r.getId());
        if (carregada == null) {
            addMsg(FacesMessage.SEVERITY_WARN, "Registro não encontrado.");
            return;
        }
        this.atual = carregada;

        Sala s = atual.getSala();
        if (s != null && s.getUnidadeSaude() != null) {
            unidadeSelId = s.getUnidadeSaude().getId();
            onTrocaUnidadeSel();
            salaSelId = s.getId();
        } else {
            unidadeSelId = null;
            salaSelId = null;
            salasDaUnidadeSel = new ArrayList<>();
        }

        usuarioSelIds = atual.getUsuarios() == null ? new ArrayList<>() :
                atual.getUsuarios().stream().map(Usuario::getId).collect(Collectors.toList());
    }

    public void salvar() {
        try {
            if (atual.getInicio() == null || atual.getFim() == null || !atual.getFim().isAfter(atual.getInicio())) {
                addMsg(FacesMessage.SEVERITY_ERROR, "Período inválido (fim deve ser após o início).");
                return;
            }
            if (unidadeSelId == null || salaSelId == null) {
                addMsg(FacesMessage.SEVERITY_ERROR, "Selecione a unidade e a sala.");
                return;
            }
            if (usuarioSelIds == null || usuarioSelIds.isEmpty()) {
                addMsg(FacesMessage.SEVERITY_ERROR, "Selecione pelo menos um usuário.");
                return;
            }

            Sala sala = salaService.encontrar(salaSelId);
            if (sala == null) {
                addMsg(FacesMessage.SEVERITY_ERROR, "Sala inválida.");
                return;
            }
            List<Usuario> selecionados = usuarioService.buscarPorIds(usuarioSelIds);

            boolean conflito = reservaService.existeConflito(sala.getId() , atual.getInicio(), atual.getFim(),atual.getId());
            if (conflito) {
                addMsg(FacesMessage.SEVERITY_ERROR, "Conflito: já existe reserva para esta sala no período.");
                return;
            }

            atual.setSala(sala);
            atual.setUsuarios(selecionados);

            reservaService.salvar(atual);
            filtrar();
            addMsg(FacesMessage.SEVERITY_INFO, "Reserva salva com sucesso.");
        } catch (Exception e) {
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro ao salvar: " + e.getMessage());
        }
    }

    public void excluir(Reserva r) {
        try {
            reservaService.excluir(r.getId());
            filtrar();
            addMsg(FacesMessage.SEVERITY_INFO, "Reserva excluída.");
        } catch (Exception e) {
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro ao excluir: " + e.getMessage());
        }
    }

    public void filtrar() {
        reservas = reservaService.buscar(filtroIni, filtroFim, unidadeFiltroId, salaFiltroId, usuarioFiltroId);
    }

    public void limparFiltro() {
        filtroIni = null;
        filtroFim = null;
        unidadeFiltroId = null;
        salaFiltroId = null;
        usuarioFiltroId = null;
        salasDaUnidade = new ArrayList<>();
        reservas = reservaService.listarTodos();
    }

    public void onTrocaUnidadeFiltro() {
        if (unidadeFiltroId != null) {
            salasDaUnidade = salaService.listarPorUnidade(unidadeFiltroId);
        } else {
            salasDaUnidade = new ArrayList<>();
        }
        salaFiltroId = null;
    }

    public void onTrocaUnidadeSel() {
        if (unidadeSelId != null) {
            salasDaUnidadeSel = salaService.listarPorUnidade(unidadeSelId);
        } else {
            salasDaUnidadeSel = new ArrayList<>();
        }
        salaSelId = null;
    }

    /* =========================
       Util
       ========================= */
    private void addMsg(FacesMessage.Severity s, String m) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(s, m, null));
    }

    /* =========================
       Getters / Setters (EL)
       ========================= */
    public List<Reserva> getReservas() { return reservas; }
    public Reserva getAtual() { return atual; }
    public void setAtual(Reserva atual) { this.atual = atual; }

    public List<UnidadeSaude> getUnidades() { return unidades; }
    public List<Sala> getSalasDaUnidade() { return salasDaUnidade; }
    public List<Sala> getSalasDaUnidadeSel() { return salasDaUnidadeSel; }
    public List<Usuario> getUsuarios() { return usuarios; }

    public LocalDateTime getFiltroIni() { return filtroIni; }
    public void setFiltroIni(LocalDateTime filtroIni) { this.filtroIni = filtroIni; }
    public LocalDateTime getFiltroFim() { return filtroFim; }
    public void setFiltroFim(LocalDateTime filtroFim) { this.filtroFim = filtroFim; }

    public Long getUnidadeFiltroId() { return unidadeFiltroId; }
    public void setUnidadeFiltroId(Long unidadeFiltroId) { this.unidadeFiltroId = unidadeFiltroId; }
    public Long getSalaFiltroId() { return salaFiltroId; }
    public void setSalaFiltroId(Long salaFiltroId) { this.salaFiltroId = salaFiltroId; }
    public Long getUsuarioFiltroId() { return usuarioFiltroId; }
    public void setUsuarioFiltroId(Long usuarioFiltroId) { this.usuarioFiltroId = usuarioFiltroId; }

    public Long getUnidadeSelId() { return unidadeSelId; }
    public void setUnidadeSelId(Long unidadeSelId) { this.unidadeSelId = unidadeSelId; }
    public Long getSalaSelId() { return salaSelId; }
    public void setSalaSelId(Long salaSelId) { this.salaSelId = salaSelId; }
    public List<Long> getUsuarioSelIds() { return usuarioSelIds; }
    public void setUsuarioSelIds(List<Long> usuarioSelIds) { this.usuarioSelIds = usuarioSelIds; }
}
