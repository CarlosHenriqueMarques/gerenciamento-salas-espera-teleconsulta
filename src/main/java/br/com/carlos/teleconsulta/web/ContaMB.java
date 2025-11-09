package br.com.carlos.teleconsulta.web;

import br.com.carlos.teleconsulta.domain.Conta;
import br.com.carlos.teleconsulta.domain.enums.Perfil;
import br.com.carlos.teleconsulta.service.ContaService;
import br.com.carlos.teleconsulta.service.PasswordService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Named("contaMB")
@ViewScoped
public class ContaMB implements Serializable {

    @Inject
    private ContaService contaService;

    @Inject
    private PasswordService passwordService;

    private List<Conta> contas = new ArrayList<>();

    private Conta atual = new Conta();

    private String senhaPlano;

    private String termo;
    private Perfil perfilFiltro;
    private Boolean ativoFiltro;

    @PostConstruct
    public void init() {
        carregarLista();
        novo();
    }

    public void carregarLista() {
        contas = contaService.listarTodos();
    }

    public void novo() {
        atual = new Conta();
        atual.setAtivo(true);
        senhaPlano = null;
    }

    public void editar(Conta c) {
        Conta carregada = contaService.encontrar(c.getId());
        if (carregada == null) {
            addMsg(FacesMessage.SEVERITY_WARN, "Registro não encontrado.");
            return;
        }
        atual = carregada;
        senhaPlano = null;
    }

    public void salvar() {
        try {
            if (atual.getId() == null) {
                if (senhaPlano == null || senhaPlano.isBlank()) {
                    addMsg(FacesMessage.SEVERITY_ERROR, "Informe uma senha.");
                    return;
                }
                atual.setSenhaHash(passwordService.hash(senhaPlano));
            } else if (senhaPlano != null && !senhaPlano.isBlank()) {
                atual.setSenhaHash(passwordService.hash(senhaPlano));
            }

            contaService.salvar(atual);
            carregarLista();
            addMsg(FacesMessage.SEVERITY_INFO, "Conta salva com sucesso.");
        } catch (Exception e) {
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro ao salvar: " + e.getMessage());
        }
    }

    public void excluir(Conta c) {
        try {
            contaService.excluir(c.getId());
            carregarLista();
            addMsg(FacesMessage.SEVERITY_INFO, "Conta excluída.");
        } catch (Exception e) {
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro ao excluir: " + e.getMessage());
        }
    }

    public void filtrar() {
        List<Conta> base = contaService.listarTodos();
        final String t = termo == null ? "" : termo.trim().toLowerCase(Locale.ROOT);

        contas = base.stream()
                .filter(c -> {
                    boolean ok = true;

                    if (!t.isEmpty()) {
                        String loginVal = c.getLogin() == null ? "" : c.getLogin().toLowerCase(Locale.ROOT);
                        ok &= loginVal.contains(t);
                    }
                    if (perfilFiltro != null) {
                        ok &= c.getPerfil() == perfilFiltro;
                    }
                    if (ativoFiltro != null) {
                        ok &= (c.isAtivo() == ativoFiltro.booleanValue());
                    }
                    return ok;
                })
                .collect(Collectors.toList());
    }


    public void limparFiltro() {
        termo = null;
        perfilFiltro = null;
        ativoFiltro = null;
        carregarLista();
    }

    private void addMsg(FacesMessage.Severity s, String m) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(s, m, null));
    }

    public List<Conta> getContas() { return contas; }

    public Conta getAtual() { return atual; }
    public void setAtual(Conta atual) { this.atual = atual; }

    public String getSenhaPlano() { return senhaPlano; }
    public void setSenhaPlano(String senhaPlano) { this.senhaPlano = senhaPlano; }

    public String getTermo() { return termo; }
    public void setTermo(String termo) { this.termo = termo; }

    public Perfil getPerfilFiltro() { return perfilFiltro; }
    public void setPerfilFiltro(Perfil perfilFiltro) { this.perfilFiltro = perfilFiltro; }

    public Boolean getAtivoFiltro() { return ativoFiltro; }
    public void setAtivoFiltro(Boolean ativoFiltro) { this.ativoFiltro = ativoFiltro; }

    public Perfil[] getPerfis() { return Perfil.values(); }
}
