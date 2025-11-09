package br.com.carlos.teleconsulta.security.controller;

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

@Named("authController")
@SessionScoped
public class AuthController implements Serializable {

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
            FacesContext.getCurrentInstance().addMessage("frm:senha",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Login ou senha inválidos.", "Verifique suas credenciais."));
            return;
        }

        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();

        try {
            var req = (jakarta.servlet.http.HttpServletRequest) ec.getRequest();
            var resp = (jakarta.servlet.http.HttpServletResponse) ec.getResponse();
            req.changeSessionId();
            var session = req.getSession(true);
            session.setAttribute("usuarioLogado", c);
        } catch (Exception ignored) {
            var old = ec.getSession(false);
            if (old != null) {
                try { ((jakarta.servlet.http.HttpSession)old).invalidate(); } catch (Exception ignore) {}
            }
            ec.getSessionMap().put("usuarioLogado", c);
        }
        this.contaLogada = c;
        this.senha = null;

        try {
            ec.redirect(ec.getRequestContextPath() + "/index.xhtml");
        } catch (IOException ignored) { }
        fc.responseComplete();
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

        String target = page.startsWith("/") ? ec.getRequestContextPath() + page : ec.getRequestContextPath() + "/" + page;

        if (fc.getPartialViewContext().isAjaxRequest()) {
            PrimeFaces.current().executeScript("window.location='" + target + "';");
        } else {
            try {
                ec.redirect(target);
            } catch (IOException ignored) { }
        }
    }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public Conta getContaLogada() { return contaLogada; }
}
