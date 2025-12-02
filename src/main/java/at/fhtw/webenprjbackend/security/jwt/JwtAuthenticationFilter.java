package at.fhtw.webenprjbackend.security.jwt;

import java.io.IOException;

import at.fhtw.webenprjbackend.security.CustomUserDetailsService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtDecoder jwtDecoder,
                                   CustomUserDetailsService userDetailsService) {
        this.jwtDecoder = jwtDecoder;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String token = resolveToken(request);

            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Claims claims = jwtDecoder.decode(token);
                String loginIdentifier = claims.getSubject(); // email oder username

                UserDetails userDetails = userDetailsService.loadUserByUsername(loginIdentifier);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (JwtException | org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            logger.error("Cannot set user authentication: " + e.getMessage(), e);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
