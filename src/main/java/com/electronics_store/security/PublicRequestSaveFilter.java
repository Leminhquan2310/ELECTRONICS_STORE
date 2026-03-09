package com.electronics_store.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class PublicRequestSaveFilter extends OncePerRequestFilter {

    private static final List<String> IGNORED_PATHS = List.of(
            "/auth/",
            "/logout",
            "/error",

            // admin static
            "/admin/css/",
            "/admin/js/",
            "/admin/images/",
            "/admin/fonts/",
            "/admin/icons/",
            "/admin/plugins/",

            // client-user static
            "/client-user/css/",
            "/client-user/js/",
            "/client-user/images/",
            "/client-user/fonts/",

            "/favicon.ico",
            "/api/product-details/",

            // well-know
            "/.well-known/"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        boolean isGet = "GET".equals(request.getMethod());
        boolean isIgnored = IGNORED_PATHS.stream()
                .anyMatch(path -> request.getRequestURI().startsWith(path));

        boolean notLoggedIn = request.getUserPrincipal() == null;

        if (isGet && !isIgnored && notLoggedIn) {
            request.getSession()
                    .setAttribute("LAST_PUBLIC_URL",
                            request.getRequestURI() +
                                    (request.getQueryString() != null
                                            ? "?" + request.getQueryString()
                                            : ""));
        }

        filterChain.doFilter(request, response);
    }
}
