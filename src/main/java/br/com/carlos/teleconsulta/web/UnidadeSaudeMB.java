package br.com.carlos.teleconsulta.web;

import br.com.carlos.teleconsulta.domain.UnidadeSaude;
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

@Named("unidadeMB")
@ViewScoped
public class UnidadeSaudeMB implements Serializable {

    @Inject
    private UnidadeSaudeService service;

    private List<UnidadeSaude> unidades;
    private UnidadeSaude atual = new UnidadeSaude();

    private String filtro; // nome/razão/sigla

    @PostConstruct
    public void init() {
        unidades = new ArrayList<>();
        carregarLista();
        atual = new UnidadeSaude();
    }

    public void carregarLista() {
        unidades = service.listarTodos();
    }

    public void novo() {
        atual = new UnidadeSaude();
    }

    public void editar(UnidadeSaude u) {
        this.atual = service.encontrar(u.getId());
        if (this.atual == null) {
            addMsg(FacesMessage.SEVERITY_WARN, "Registro não encontrado.");
        }
    }

    public void salvar() {
        try {
            // checagens únicas
            if (service.cnpjExiste(atual.getCnpj(), atual.getId())) {
                addMsg(FacesMessage.SEVERITY_ERROR, "CNPJ já cadastrado.");
                return;
            }
            if (service.cnesExiste(atual.getCnes(), atual.getId())) {
                addMsg(FacesMessage.SEVERITY_ERROR, "CNES já cadastrado.");
                return;
            }

            service.salvar(atual);
            filtrar(); // mantém o contexto do filtro após salvar
            addMsg(FacesMessage.SEVERITY_INFO, "Unidade salva com sucesso.");
        } catch (Exception e) {
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro ao salvar: " + e.getMessage());
        }
    }

    public void excluir(UnidadeSaude u) {
        try {
            service.excluir(u.getId());
            filtrar();
            addMsg(FacesMessage.SEVERITY_INFO, "Unidade excluída.");
        } catch (Exception e) {
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro ao excluir: " + e.getMessage());
        }
    }

    public void filtrar() {
        unidades = service.buscarPorFiltro(filtro);
    }

    public void limparFiltro() {
        filtro = null;
        carregarLista();
    }

    private void addMsg(FacesMessage.Severity s, String m) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(s, m, null));
    }

    // getters/setters
    public List<UnidadeSaude> getUnidades() { return unidades; }
    public UnidadeSaude getAtual() { return atual; }
    public void setAtual(UnidadeSaude atual) { this.atual = atual; }
    public String getFiltro() { return filtro; }
    public void setFiltro(String filtro) { this.filtro = filtro; }
}
