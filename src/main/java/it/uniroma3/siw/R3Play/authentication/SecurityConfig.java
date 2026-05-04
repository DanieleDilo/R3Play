package it.uniroma3.siw.R3Play.authentication;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * CONFIGURAZIONE DELLA SICUREZZA
 *
 * Definisce:
 * - Le rotte pubbliche (accessibili senza login)
 * - Le rotte protette (richiedono autenticazione)
 * - Login classico (form con email)
 * - Login OAuth2 (Google)
 * - Logout
 * - Abilitazione @PreAuthorize per la protezione ADMIN a livello di metodo
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Abilita @PreAuthorize nei controller
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // === ROTTE PUBBLICHE ===
                // Homepage, vetrina, dettaglio articolo/utente: visibili a tutti
                .requestMatchers(
                    "/", "/vetrina", "/login", "/registrati", "/error",
                    "/css/**", "/images/**", "/uploads/**", "/favicon.ico",
                    "/articolo/**", "/utente/**",
                    // React e risorse statiche
                    "/react/**", "/js/**"
                ).permitAll()

                // === ROTTE PROTETTE (utenti autenticati) ===
                // Armadio, pubblicazione articoli, recensioni: richiedono login
                .requestMatchers(
                    "/armadio", "/articolo/nuovo", "/articolo/modifica/**",
                    "/articolo/elimina/**", "/utente/modifica",
                    "/recensione/**"
                ).authenticated()

                // === ROTTE ADMIN ===
                // L'analisi performance è protetta sia qui che con @PreAuthorize
                .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")

                // Tutto il resto richiede autenticazione
                .anyRequest().authenticated()
            )

            // --- LOGIN CLASSICO (form con email e password) ---
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")         // usiamo email come username
                .defaultSuccessUrl("/vetrina", true)
                .permitAll()
            )

            // --- LOGIN OAUTH2 (Google) ---
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/vetrina", true)
            )

            // --- LOGOUT ---
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
            );

        return http.build();
    }
}