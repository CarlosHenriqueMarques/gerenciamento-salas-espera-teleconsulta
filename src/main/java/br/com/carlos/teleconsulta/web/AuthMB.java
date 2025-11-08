package br.com.carlos.teleconsulta.web;

import br.com.carlos.teleconsulta.domain.Conta;
import br.com.carlos.teleconsulta.domain.enums.Perfil;
import br.com.carlos.teleconsulta.service.ContaService;
import br.com.carlos.teleconsulta.service.PasswordService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.PrimeFaces;

import java.io.IOException;
import java.io.Serializable;

@Named("authMB")
@SessionScoped
public class AuthMB implements Serializable {

    private static final long serialVersionUID = 1L;

    private String login;
    private String senha;

    private Conta contaLogada;

    @Inject
    private ContaService contaService;

    @Inject
    private PasswordService passwordService;

    public void logar() {
        Conta c = contaService.validarLogin(login, senha, passwordService);
        if (c == null) {
            FacesMessage msg = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Login ou senha inválidos.",
                    "Verifique suas credenciais."
            );
            // amarra a mensagem ao campo de senha (mostra no p:message for="senha")
            FacesContext.getCurrentInstance().addMessage("frm:senha", msg);
            FacesContext.getCurrentInstance().validationFailed(); // sinaliza falha pro AJAX
            return;
        }

        this.contaLogada = c;
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.getSessionMap().put("usuarioLogado", c);

        // redireciona (com ou sem AJAX)
        redirect("/index.xhtml");
        this.senha = null;
    }


    public void logout() {
        var ec = FacesContext.getCurrentInstance().getExternalContext();
        try { ec.invalidateSession(); } catch (IllegalStateException ignored) {}
        this.contaLogada = null;
        this.login = null;
        this.senha = null;
        try { ec.redirect(ec.getRequestContextPath() + "/login.xhtml"); } catch (IOException ignored) {}
    }

    public boolean isLogado() {
        return contaLogada != null;
    }

    public boolean hasRole(Perfil p) {
        return isLogado() && contaLogada.getPerfil() == p;
    }

    private void redirect(String page) {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();

        // Garante caminho com contextPath
        String target = page.startsWith("/") ? ec.getRequestContextPath() + page : ec.getRequestContextPath() + "/" + page;

        // Se for AJAX, usa JS para navegar; senão, redirect normal
        if (fc.getPartialViewContext().isAjaxRequest()) {
            PrimeFaces.current().executeScript("window.location='" + target + "';");
        } else {
            try {
                ec.redirect(target);
            } catch (IOException ignored) { }
        }
    }

    // Getters/Setters
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public Conta getContaLogada() { return contaLogada; }
}
