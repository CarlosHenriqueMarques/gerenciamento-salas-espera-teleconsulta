package br.com.carlos.teleconsulta.web;

import br.com.carlos.teleconsulta.domain.Usuario;
import br.com.carlos.teleconsulta.service.UsuarioService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Named("usuarioMB")
@ViewScoped
public class UsuarioMB implements Serializable {

    @Inject
    private UsuarioService service;

    private List<Usuario> usuarios;
    private Usuario atual = new Usuario();

    // filtros por período (datas simples na tela)
    private LocalDate inicio;
    private LocalDate fim;

    @PostConstruct
    public void init() {
        usuarios = new ArrayList<>();
        carregarLista();
        atual = new Usuario();
    }

    public void carregarLista() {
        usuarios = service.listarTodos();
    }
    public void novo() {
        atual = new Usuario();
    }

    public void editar(Usuario u) {
        // carrega do banco para evitar stale state
        this.atual = service.encontrar(u.getId());
        if (this.atual == null) {
            addMsg(FacesMessage.SEVERITY_WARN, "Registro não encontrado.");
        }
    }

    public void salvar() {
        try {
            if (atual.getId() == null && service.cpfExiste(atual.getCpf())) {
                addMsg(FacesMessage.SEVERITY_ERROR, "CPF já cadastrado.");
                return;
            }
            service.salvar(atual);
            usuarios = service.listarTodos();
            addMsg(FacesMessage.SEVERITY_INFO, "Usuário salvo com sucesso.");
        } catch (Exception e) {
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro ao salvar: " + e.getMessage());
        }
    }

    public void excluir(Usuario u) {
        try {
            service.excluir(u.getId());
            usuarios = service.listarTodos();
            addMsg(FacesMessage.SEVERITY_INFO, "Usuário excluído.");
        } catch (Exception e) {
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro ao excluir: " + e.getMessage());
        }
    }

    public void filtrarPeriodo() {
        LocalDateTime i = (inicio != null) ? inicio.atStartOfDay() : null;
        LocalDateTime f = (fim != null) ? fim.atTime(LocalTime.MAX) : null;
        usuarios = service.buscarPorPeriodo(i, f);
    }

    private void addMsg(FacesMessage.Severity s, String m) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(s, m, null));
    }

    // getters/setters
    public List<Usuario> getUsuarios() { return usuarios; }
    public Usuario getAtual() { return atual; }
    public void setAtual(Usuario atual) { this.atual = atual; }
    public LocalDate getInicio() { return inicio; }
    public void setInicio(LocalDate inicio) { this.inicio = inicio; }
    public LocalDate getFim() { return fim; }
    public void setFim(LocalDate fim) { this.fim = fim; }
}
