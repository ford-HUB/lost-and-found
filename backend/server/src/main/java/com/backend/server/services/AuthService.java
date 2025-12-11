package com.backend.server.services;

import java.util.Optional;

import org.elasticsearch.ResourceAlreadyExistsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.backend.server.models.Account;
import com.backend.server.models.Role;
import com.backend.server.models.User;
import com.backend.server.repository.AccountRepository;
import com.backend.server.repository.RoleRepository;
import com.backend.server.repository.UserRepository;

import jakarta.transaction.Transactional;


@Service
public class AuthService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    // @imported helper
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    // constructor
    public AuthService(AccountRepository accountRepository, UserRepository userRepository, RoleRepository roleRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Transactional
    public Account registerAccount(Account payload) {

        if(accountRepository.existsByEmail(payload.getEmail())) {
            throw new ResourceAlreadyExistsException("Email is already exist");
        }

        Account account = new Account();
        account.setEmail(payload.getEmail());
        account.setPassword(bCryptPasswordEncoder.encode(payload.getPassword()));
        return accountRepository.save(account);
        
    }

    @Transactional
    public Role registerRole(Role payload) {
        Role role = new Role();
        role.setRoleName(payload.getRoleName());
        role.setDescription(payload.getDescription());

        Account account = accountRepository.findById(payload.getAccount().getAccountId())
        .orElseThrow(() -> new RuntimeException("Account not found"));
        
        role.setAccount(account);
        return roleRepository.save(role);
    }

    @Transactional
    public User registerUserInfo(User payload) {
        User user = new User();
        user.setFullname(payload.getFullname());
        user.setContactNumber(payload.getContactNumber());
        user.setAddress(payload.getAddress());

        Account account = accountRepository.findById(payload.getAccount().getAccountId())
        .orElseThrow(() -> new RuntimeException("Account not found"));
        user.setAccount(account);

        return userRepository.save(user);
    }

}
