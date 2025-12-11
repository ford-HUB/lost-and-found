package com.backend.server.services;

import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.backend.server.models.Account;
import com.backend.server.models.Role;
import com.backend.server.repository.AccountRepository;
import com.backend.server.repository.RoleRepository;
import com.backend.server.security.UserAccountInfoDetails;

@Service
public class UserAccountInfoService implements UserDetailsService {
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public UserAccountInfoService(AccountRepository accountRepository, RoleRepository roleRepository) {
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Account> accountInfoDetails = accountRepository.findByEmail(username);

        if(accountInfoDetails.isEmpty()) {
            throw new UsernameNotFoundException("Account email is not found" + username);
        }

        Account account = accountInfoDetails.get();

        Role role = roleRepository.findByAccount_AccountId(account.getAccountId())
        .orElseThrow(() -> new UsernameNotFoundException("Account role not found for " + account.getEmail()));

        return new UserAccountInfoDetails(account, role);
    }
}
