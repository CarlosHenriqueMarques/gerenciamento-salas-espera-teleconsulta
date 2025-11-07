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

    private static final long serialVersionUID = 1L;

    @Inject
    private UsuarioService service;

    private List<Usuario> usuarios = new ArrayList<>();
    private Usuario atual = new Usuario();

    private LocalDate inicio;
    private LocalDate fim;

    @PostConstruct
    public void init() {
        carregarLista();
        atual = new Usuario();
    }

    public void carregarLista() {
        try {
            usuarios = service.listarTodos();
        } catch (Exception e) {
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro ao carregar lista: " + e.getMessage());
        }
    }

    public void novo() {
        atual = new Usuario();
    }

    public void editar(Usuario u) {
        try {
            this.atual = service.encontrar(u.getId());
            if (this.atual == null) {
                addMsg(FacesMessage.SEVERITY_WARN, "Registro não encontrado.");
            }
        } catch (Exception e) {
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro ao carregar registro: " + e.getMessage());
        }
    }

    public void salvar() {
        try {
            if (atual.getNome() != null) atual.setNome(atual.getNome().trim());
            if (atual.getEmail() != null) atual.setEmail(atual.getEmail().trim());
            if (atual.getCpf() != null) atual.setCpf(atual.getCpf().trim());

            if (atual.getId() == null && service.cpfExiste(atual.getCpf())) {
                addMsg(FacesMessage.SEVERITY_ERROR, "CPF já cadastrado.");
                return;
            }

            service.salvar(atual);
            carregarLista();
            addMsg(FacesMessage.SEVERITY_INFO, "Usuário salvo com sucesso.");
        } catch (Exception e) {
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro ao salvar: " + e.getMessage());
        }
    }

    public void excluir(Usuario u) {
        try {
            service.excluir(u.getId());
            carregarLista();
            addMsg(FacesMessage.SEVERITY_INFO, "Usuário excluído.");
        } catch (Exception e) {
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro ao excluir: " + e.getMessage());
        }
    }

    public void limparFiltro() {
        inicio = null;
        fim = null;
        carregarLista();
    }

    public void filtrarPeriodo() {
        if (inicio == null && fim == null) {
            carregarLista();
            return;
        }

        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            addMsg(FacesMessage.SEVERITY_WARN, "Período inválido: início após o fim.");
            return;
        }

        LocalDateTime i = (inicio != null) ? inicio.atStartOfDay() : null;
        LocalDateTime f = (fim != null) ? fim.atTime(LocalTime.MAX) : null;

        try {
            usuarios = service.buscarPorPeriodo(i, f);
        } catch (Exception e) {
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro ao filtrar: " + e.getMessage());
        }
    }

    private void addMsg(FacesMessage.Severity s, String m) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(s, m, null));
    }
    public List<Usuario> getUsuarios() { return usuarios; }
    public Usuario getAtual() { return atual; }
    public void setAtual(Usuario atual) { this.atual = atual; }
    public LocalDate getInicio() { return inicio; }
    public void setInicio(LocalDate inicio) { this.inicio = inicio; }
    public LocalDate getFim() { return fim; }
    public void setFim(LocalDate fim) { this.fim = fim; }
}
