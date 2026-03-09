package com.electronics_store.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Slf4j
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final RequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        /* ===============================
           1️⃣ ƯU TIÊN: SavedRequest (bị chặn bởi Security)
           =============================== */
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            String targetUrl = savedRequest.getRedirectUrl();
            requestCache.removeRequest(request, response);
            response.sendRedirect(targetUrl);
            return;
        }

        /* ===============================
          2️⃣ Redirect theo ROLE
           =============================== */
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        if (isAdmin) {
            response.sendRedirect("/admin");
            return;
        }

        /* ===============================
           3️⃣  Request public đã lưu thủ công
           =============================== */
        HttpSession session = request.getSession(false);
        if (session != null) {
            String lastPublicUrl = (String) session.getAttribute("LAST_PUBLIC_URL");
            if (lastPublicUrl != null) {
                session.removeAttribute("LAST_PUBLIC_URL");
                response.sendRedirect(lastPublicUrl);
                return;
            }
        }

        /* ===============================
           4️⃣ Fallback
           =============================== */
        response.sendRedirect("/");
    }
}
