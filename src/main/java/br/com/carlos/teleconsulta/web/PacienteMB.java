package br.com.carlos.teleconsulta.web;

import br.com.carlos.teleconsulta.domain.Paciente;
import br.com.carlos.teleconsulta.domain.enums.Sexo;
import br.com.carlos.teleconsulta.service.PacienteService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Named("pacienteMB")
@ViewScoped
public class PacienteMB implements Serializable {

    @Inject
    private PacienteService service;

    private List<Paciente> pacientes;
    private Paciente atual;

    private String termo;
    private LocalDate nascIni;
    private LocalDate nascFim;

    @PostConstruct
    public void init() {
        pacientes = new ArrayList<>();
        carregarLista();
        atual = new Paciente();
    }

    public void carregarLista() {
        pacientes = service.listarTodos();
    }

    public void novo() {
        atual = new Paciente();
    }

    public void editar(Paciente p) {
        this.atual = service.encontrar(p.getId());
        if (this.atual == null) {
            addMsg(FacesMessage.SEVERITY_WARN, "Registro não encontrado.");
        }
    }

    public void salvar() {
        try {
            service.salvar(atual);
            pacientes = service.listarTodos();
            addMsg(FacesMessage.SEVERITY_INFO, "Paciente salvo com sucesso.");
        } catch (IllegalArgumentException iae) {
            addMsg(FacesMessage.SEVERITY_ERROR, iae.getMessage());
        } catch (Exception e) {
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro ao salvar: " + e.getMessage());
        }
    }

    public void excluir(Paciente p) {
        try {
            service.excluir(p.getId());
            pacientes = service.listarTodos();
            addMsg(FacesMessage.SEVERITY_INFO, "Paciente excluído.");
        } catch (Exception e) {
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro ao excluir: " + e.getMessage());
        }
    }

    public void filtrar() {
        pacientes = service.buscar(termo, nascIni, nascFim);
    }

    public void limparFiltro() {
        termo = null;
        nascIni = null;
        nascFim = null;
        carregarLista();
    }

    private void addMsg(FacesMessage.Severity s, String m) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(s, m, null));
    }

    public Sexo[] getSexos() { return Sexo.values(); }
    public List<Paciente> getPacientes() { return pacientes; }
    public Paciente getAtual() { return atual; }
    public void setAtual(Paciente atual) { this.atual = atual; }

    public String getTermo() { return termo; }
    public void setTermo(String termo) { this.termo = termo; }

    public LocalDate getNascIni() { return nascIni; }
    public void setNascIni(LocalDate nascIni) { this.nascIni = nascIni; }

    public LocalDate getNascFim() { return nascFim; }
    public void setNascFim(LocalDate nascFim) { this.nascFim = nascFim; }
}
