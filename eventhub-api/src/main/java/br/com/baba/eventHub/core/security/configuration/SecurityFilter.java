package br.com.baba.eventHub.core.security.configuration;

import br.com.baba.eventHub.core.model.User;
import br.com.baba.eventHub.core.repository.UserRepository;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.UUID;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            var tokenJWT = recoverToken(request);
            if (tokenJWT != null) {
                var subject = tokenService.getSubject(tokenJWT);
                User user = findUserById(subject);
                if (user == null) {
                    throw new UsernameNotFoundException("User not found");
                }
                var authenticationToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
            filterChain.doFilter(request, response);
        } catch (JWTVerificationException | UsernameNotFoundException e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }

    private User findUserById(String subject) {
        try {
            return userRepository.findById(UUID.fromString(subject)).orElse(null);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String recoverToken(HttpServletRequest request) {
        var authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }
        var token = authorizationHeader.substring(7).trim();
        return token.isEmpty() ? null : token;
    }
}
