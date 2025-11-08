package br.com.carlos.teleconsulta.web;

import br.com.carlos.teleconsulta.domain.Sala;
import br.com.carlos.teleconsulta.domain.UnidadeSaude;
import br.com.carlos.teleconsulta.service.SalaService;
import br.com.carlos.teleconsulta.service.UnidadeSaudeService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("salaController")
@ViewScoped
public class SalaController implements Serializable {

    private static final Logger LOG = Logger.getLogger(SalaController.class.getName());

    @Inject
    private SalaService salaService;

    @Inject
    private UnidadeSaudeService unidadeService;

    private List<Sala> salas;
    private List<UnidadeSaude> unidades;
    private Sala atual;


    private Long unidadeIdFiltro;
    private String termo;

    private Long unidadeSelId;

    @PostConstruct
    public void init() {
        salas = new ArrayList<>();
        unidades = new ArrayList<>();
        atual = new Sala();

        try {
            if (unidadeService != null) {
                unidades = unidadeService.listarTodos();
            } else {
                LOG.warning("unidadeService está null no init() de SalaController");
            }
            if (salaService != null) {
                salas = salaService.listarTodos();
            } else {
                LOG.warning("salaService está null no init() de SalaController");
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Falha ao inicializar SalaController", e);
            addMsg(FacesMessage.SEVERITY_ERROR, "Falha ao carregar dados de Salas: " + e.getMessage());
        }
    }

    public void carregarLista() {
        try {
            if (salaService == null) {
                addMsg(FacesMessage.SEVERITY_ERROR, "Serviço de Sala indisponível.");
                return;
            }
            salas = salaService.listarTodos();
        } catch (Exception e) {
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro ao listar salas: " + e.getMessage());
        }
    }

    public void novo() {
        atual = new Sala();
        unidadeSelId = null;
    }

    public void editar(Sala s) {
        try {
            if (salaService == null) {
                addMsg(FacesMessage.SEVERITY_ERROR, "Serviço de Sala indisponível.");
                return;
            }
            this.atual = salaService.encontrar(s.getId());
            if (this.atual == null) {
                addMsg(FacesMessage.SEVERITY_WARN, "Registro não encontrado.");
                return;
            }
            this.unidadeSelId = (this.atual.getUnidadeSaude() != null) ? this.atual.getUnidadeSaude().getId() : null;
        } catch (Exception e) {
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro ao carregar sala: " + e.getMessage());
        }
    }

    public void salvar() {
        try {
            if (salaService == null) {
                addMsg(FacesMessage.SEVERITY_ERROR, "Serviço de Sala indisponível.");
                return;
            }
            if (unidadeSelId == null) {
                addMsg(FacesMessage.SEVERITY_ERROR, "Selecione a Unidade de Saúde.");
                return;
            }
            UnidadeSaude un = unidadeService.encontrar(unidadeSelId);
            if (un == null) {
                addMsg(FacesMessage.SEVERITY_ERROR, "Unidade informada não existe.");
                return;
            }
            atual.setUnidadeSaude(un);

            salaService.salvar(atual);
            carregarLista();
            addMsg(FacesMessage.SEVERITY_INFO, "Sala salva com sucesso.");
        } catch (Exception e) {
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro ao salvar: " + e.getMessage());
        }
    }

    public void excluir(Sala s) {
        try {
            if (salaService == null) {
                addMsg(FacesMessage.SEVERITY_ERROR, "Serviço de Sala indisponível.");
                return;
            }
            salaService.excluir(s.getId());
            carregarLista();
            addMsg(FacesMessage.SEVERITY_INFO, "Sala excluída.");
        } catch (Exception e) {
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro ao excluir: " + e.getMessage());
        }
    }
    public void filtrar() {
        try {
            List<Sala> base = salaService.listarTodos();
            String t = termo != null ? termo.trim().toLowerCase() : "";
            Long uid = unidadeIdFiltro;

            salas = base.stream()
                    .filter(s -> t.isEmpty() || (s.getNome() != null && s.getNome().toLowerCase().contains(t)))
                    .filter(s -> uid == null || (s.getUnidadeSaude() != null && uid.equals(s.getUnidadeSaude().getId())))
                    .toList();
        } catch (Exception e) {
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro ao filtrar: " + e.getMessage());
        }
    }

    public void limparFiltro() {
        termo = null;
        unidadeIdFiltro = null;
        carregarLista();
    }

    private void addMsg(FacesMessage.Severity s, String m) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(s, m, null));
    }

    public List<Sala> getSalas() { return salas; }
    public List<UnidadeSaude> getUnidades() { return unidades; }
    public Sala getAtual() { return atual; }
    public void setAtual(Sala atual) { this.atual = atual; }

    public Long getUnidadeFiltroId() { return unidadeIdFiltro; }
    public void setUnidadeFiltroId(Long unidadeFiltroId) { this.unidadeIdFiltro = unidadeFiltroId; }

    public Long getUnidadeIdFiltro() { return unidadeIdFiltro; }
    public void setUnidadeIdFiltro(Long unidadeIdFiltro) { this.unidadeIdFiltro = unidadeIdFiltro; }

    public String getTermo() { return termo; }
    public void setTermo(String termo) { this.termo = termo; }

    public Long getUnidadeSelId() { return unidadeSelId; }
    public void setUnidadeSelId(Long unidadeSelId) { this.unidadeSelId = unidadeSelId; }
}
