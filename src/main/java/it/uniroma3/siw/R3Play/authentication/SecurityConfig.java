package it.uniroma3.siw.R3Play.authentication;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // Pagine libere a tutti (Vetrina e Dettaglio)
                .requestMatchers("/", "/articolo/{id}", "/css/**", "/images/**", "/uploads/**").permitAll()
                // Tutto il resto (aggiungi articolo, recensione, elimina) richiede il login
                .anyRequest().authenticated()
            )
            // Abilitiamo il login tramite OAuth2
            .oauth2Login(withDefaults())
            // Abilitiamo il logout e torniamo in vetrina
            .logout(logout -> logout.logoutSuccessUrl("/"));

        return http.build();
    }
}