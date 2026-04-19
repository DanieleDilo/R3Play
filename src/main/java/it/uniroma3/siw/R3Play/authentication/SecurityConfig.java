package it.uniroma3.siw.R3Play.authentication;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. Diciamo a Spring di usare BCrypt per criptare/decriptare le password
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

   @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                
                // 1. Risorse di base e pagine di benvenuto permesse a tutti
                .requestMatchers("/", "/vetrina", "/css/**", "/images/**", "/uploads/**", "/error", "/favicon.ico", "/login", "/registrati").permitAll()
                
                // 2. Dettaglio articolo pubblico, ma SOLO se "id" è un numero da 0 a 9!
                .requestMatchers("/articolo/{id:[0-9]+}").permitAll()
                
                // 3. Qualsiasi altra richiesta (incluso /articolo/nuovo, /armadio, ecc.) viene bloccata se non sei loggato
                .anyRequest().authenticated()
            )
            // ... (il resto della configurazione di formLogin e oauth2Login rimane identico)
            // 2. CONFIGURAZIONE LOGIN CLASSICO (Form)
            .formLogin(form -> form
                .loginPage("/login") // Diremo a Spring dove trovare la nostra pagina HTML personalizzata
                .usernameParameter("email") // Usiamo l'email invece dello "username" standard
                .defaultSuccessUrl("/vetrina", true)
                .permitAll()
            )
            // 3. CONFIGURAZIONE OAUTH2 (Google)
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login") // Rimandiamo alla stessa pagina HTML per scegliere!
                .defaultSuccessUrl("/vetrina", true) 
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
            );

        return http.build();
    }
}