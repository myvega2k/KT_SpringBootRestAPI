package com.kt.myrestapi.security.filter;

import com.kt.myrestapi.security.jwt.JwtService;
import com.kt.myrestapi.security.userinfo.UserInfo;
import com.kt.myrestapi.security.userinfo.UserInfoRepository;
import com.kt.myrestapi.security.userinfo.UserInfoUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserInfoUserDetailsService userDetailsService;

    @Autowired
    private UserInfoRepository repository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            //username = jwtService.extractUsername(token);
            String userId = jwtService.extractUsername(token);
            UserInfo userInfo = repository.findByUserId(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("user not found "));
            username = userInfo.getEmail();
            System.out.println(">>>> username = " + username);

        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            //UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            //if (jwtService.validateToken(token, userDetails)) {
            if (jwtService.validateToken(token)) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails,
                                null, userDetails.getAuthorities());
                //Authentication 객체생성
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); 
                //Authentication 객체를 SecurityContext에 저장함
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}