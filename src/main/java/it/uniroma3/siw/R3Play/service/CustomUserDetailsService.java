package it.uniroma3.siw.R3Play.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import it.uniroma3.siw.R3Play.model.Utente;
import it.uniroma3.siw.R3Play.repository.UserRepository;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Cerca l'utente nel DB tramite email
        Utente utente = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato con email: " + email));

        // 2. Se l'utente si era registrato con Google, non ha una password nel DB!
        if (utente.getPassword() == null) {
            throw new UsernameNotFoundException("Questo utente si è registrato con Google. Usa il login social.");
        }

       
       // 3. Traduce il nostro "Utente" nel formato "User" assegnandogli il ruolo base "ROLE_USER"
        return new User(
                utente.getEmail(),
                utente.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}