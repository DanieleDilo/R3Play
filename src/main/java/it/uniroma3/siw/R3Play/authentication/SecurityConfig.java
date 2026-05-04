package it.uniroma3.siw.R3Play.authentication;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Rotte protette - prima delle pubbliche
                .requestMatchers(
                    "/armadio", "/articolo/nuovo", "/articolo/modifica/**",
                    "/articolo/elimina/**", "/utente/modifica",
                    "/utente/*/recensione", "/recensione/**"
                ).authenticated()
                
                // Rotte pubbliche
                .requestMatchers(
                    "/", "/vetrina", "/login", "/registrati", "/error",
                    "/css/**", "/images/**", "/uploads/**", "/favicon.ico",
                    "/js/**", "/react/**"
                ).permitAll()
                
                // Rotte ADMIN
                .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                
                // Tutto il resto
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .defaultSuccessUrl("/vetrina", true)
                .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
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