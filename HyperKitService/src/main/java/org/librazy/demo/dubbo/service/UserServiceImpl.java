package org.librazy.demo.dubbo.service;

import org.librazy.demo.dubbo.domain.SrpAccountEntity;
import org.librazy.demo.dubbo.domain.UserEntity;
import org.librazy.demo.dubbo.domain.repo.UserRepository;
import org.librazy.demo.dubbo.model.SrpSignupForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String id) {
        log.debug("Loading user {}", id);
        Optional<UserEntity> user = userRepository.findById(Long.valueOf(id));
        if (!user.isPresent()) {
            log.info("user {} not found when loading", id);
            throw new UsernameNotFoundException("");
        }
        UserEntity userEntity = user.get();
        return new User(String.valueOf(userEntity.getId()), "",
                createAuthority(userEntity));
    }

    @Override
    @Transactional
    public UserEntity registerUser(SrpSignupForm signupForm) {
        log.info("Registering new srp user {}", signupForm.getEmail());
        UserEntity user = new UserEntity(signupForm.getEmail(), signupForm.getNick());
        new SrpAccountEntity(user, signupForm.getSalt(), signupForm.getVerifier());
        user = userRepository.save(user);
        return user;
    }

    private Collection<GrantedAuthority> createAuthority(UserEntity user) {
        return user.getRole().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public SrpAccountEntity getSrpAccount(String email) {
        return userRepository.getAccount(email).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}
