package com.fourtune.common.global.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.common.global.error.ErrorCode;
import com.fourtune.common.global.error.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        try {
            if (token != null && jwtTokenProvider.validateToken(token)) {
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Security Context에 '{}' 인증 정보를 저장했습니다", authentication.getName());
            }
        }
        catch(ExpiredJwtException e){
            reissueResponse(response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void reissueResponse(HttpServletResponse response) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.EXPIRED_ACCESS_TOKEN);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(errorResponse);

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(errorResponse.getStatus());

        response.getWriter().write(json);
    }

}
