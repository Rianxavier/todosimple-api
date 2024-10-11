package com.rianxavier.todosimple.services;

import com.rianxavier.todosimple.models.User;
import com.rianxavier.todosimple.repositories.UserRepository;
import com.rianxavier.todosimple.security.UserSpringSecuriry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.userRepository.findByUsername(username);
        if (Objects.isNull(user))
            throw new UsernameNotFoundException("Usuário não encontrado: " + username);

        return new UserSpringSecuriry(user.getId(), user.getUsername(), user.getPassword(), user.getProfile());
    }
}
