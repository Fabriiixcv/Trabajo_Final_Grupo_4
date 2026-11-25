package com.upc.comparasalud.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.io.IOException;
import java.time.LocalDateTime;

//HU06: acceso por rol. Mismas reglas que el modo "mock" del frontend (js/mock.js):
//  publico  -> los 3 login y los registros de cliente y farmacia (HU01, HU09)
//  CLIENTE  -> solo sus datos (/api/clientes/{id}/...), calificar farmacias
//  FARMACIA -> sus datos, medicamentos y ofertas propias
//  ADMINISTRADOR -> categorias, bajas y totales
//  cualquier usuario autenticado -> consultas (farmacias, medicamentos, ofertas, categorias)
//Los controladores no cambian: las rutas y los cuerpos son los mismos de antes.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtRequestFilter jwtRequestFilter) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable) //API REST sin cookies de sesion
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        //publico: login y registro
                        .requestMatchers(HttpMethod.POST, "/api/clientes/login", "/api/farmacias/login",
                                "/api/administradores/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/clientes", "/api/farmacias").permitAll()
                        //cliente: solo sobre sus propios datos y favoritos
                        .requestMatchers("/api/clientes/{id}", "/api/clientes/{id}/**")
                        .access(propio("CLIENTE", "/api/clientes/"))
                        .requestMatchers(HttpMethod.POST, "/api/farmacias/{id}/calificaciones").hasRole("CLIENTE")
                        //farmacia: solo sobre su propia cuenta y ofertas
                        .requestMatchers(HttpMethod.PUT, "/api/farmacias/{id}", "/api/farmacias/{id}/perfil-comercial")
                        .access(propio("FARMACIA", "/api/farmacias/"))
                        .requestMatchers(HttpMethod.GET, "/api/farmacias/{id}/ofertas")
                        .access(propio("FARMACIA", "/api/farmacias/"))
                        .requestMatchers(HttpMethod.DELETE, "/api/farmacias/{id}").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/medicamentos").hasRole("FARMACIA")
                        .requestMatchers(HttpMethod.PUT, "/api/medicamentos/{id}").hasRole("FARMACIA")
                        .requestMatchers(HttpMethod.DELETE, "/api/medicamentos/{id}").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/ofertas").hasRole("FARMACIA")
                        .requestMatchers(HttpMethod.PUT, "/api/ofertas/{id}", "/api/ofertas/{id}/precio-stock").hasRole("FARMACIA")
                        .requestMatchers(HttpMethod.DELETE, "/api/ofertas/{id}").hasRole("FARMACIA")
                        //administrador
                        .requestMatchers(HttpMethod.POST, "/api/categorias").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/categorias/{id}").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/categorias/{id}").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/administradores/**").hasRole("ADMINISTRADOR")
                        //el resto (consultas): cualquier usuario autenticado
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> error(res, 401, "No autenticado"))
                        .accessDeniedHandler((req, res, ex) -> error(res, 403, SesionActual.DENEGADO)));
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    //Exige el rol y que el {id} de la ruta (el segmento que sigue a "prefijo") sea el de quien hizo la peticion
    private static AuthorizationManager<RequestAuthorizationContext> propio(String rol, String prefijo) {
        return (authentication, contexto) -> {
            Authentication auth = authentication.get();
            if (auth == null || !(auth.getPrincipal() instanceof UsuarioSesion))
                return new AuthorizationDecision(false);
            UsuarioSesion u = (UsuarioSesion) auth.getPrincipal();
            String uri = contexto.getRequest().getRequestURI();
            int desde = uri.indexOf(prefijo);
            if (desde < 0 || !rol.equals(u.getRol())) return new AuthorizationDecision(false);
            String resto = uri.substring(desde + prefijo.length());
            int fin = resto.indexOf('/');
            String id = fin < 0 ? resto : resto.substring(0, fin);
            return new AuthorizationDecision(id.equals(String.valueOf(u.getId())));
        };
    }

    //Mismo formato de error que GlobalExceptionHandler: fecha, estado, mensaje
    private static void error(jakarta.servlet.http.HttpServletResponse res, int estado, String mensaje) throws IOException {
        res.setStatus(estado);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write("{\"fecha\":\"" + LocalDateTime.now() + "\",\"estado\":" + estado
                + ",\"mensaje\":\"" + mensaje + "\"}");
    }

    //CORS: el navegador debe poder llamar a la API desde el frontend y enviar el header Authorization
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsFilter corsFilter(@Value("${ip.frontend}") String[] origenes) {
        CorsConfiguration config = new CorsConfiguration();
        for (String origen : origenes) config.addAllowedOrigin(origen.trim());
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.addExposedHeader("Authorization");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
