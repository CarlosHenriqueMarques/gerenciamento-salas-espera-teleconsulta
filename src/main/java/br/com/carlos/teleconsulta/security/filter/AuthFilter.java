package br.com.carlos.teleconsulta.security.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
@WebFilter(filterName = "AuthFilter", urlPatterns = {"*.xhtml"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        var req  = (HttpServletRequest) request;
        var resp = (HttpServletResponse) response;

        String ctx = req.getContextPath();
        String uri = req.getRequestURI();

        boolean isLoginPage  = uri.endsWith("/login.xhtml");
        boolean isJsfRes     = uri.contains("/javax.faces.resource/");
        boolean isPublic     = isLoginPage || isJsfRes;

        HttpSession session = req.getSession(false);
        boolean logged = (session != null && session.getAttribute("usuarioLogado") != null);

        if (isLoginPage) {
            resp.setHeader("Cache-Control","no-store, no-cache, must-revalidate, max-age=0");
            resp.setHeader("Pragma","no-cache");
            resp.setDateHeader("Expires", 0);
        }

        if (logged || isPublic) {
            chain.doFilter(request, response);
        } else {
            resp.sendRedirect(ctx + "/login.xhtml");
        }
    }
}

