package br.com.carlos.teleconsulta.security.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("*.xhtml")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest  request  = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String uri = request.getRequestURI();
        boolean recursoEstatico = uri.contains("/javax.faces.resource/");
        boolean paginaLogin     = uri.endsWith("/login.xhtml");

        Object usuario = request.getSession(false) == null ? null
                : request.getSession(false).getAttribute("usuarioLogado");

        if (usuario != null || paginaLogin || recursoEstatico) {
            chain.doFilter(req, res);
        } else {
            response.sendRedirect(request.getContextPath() + "/login.xhtml");
        }
    }
}
