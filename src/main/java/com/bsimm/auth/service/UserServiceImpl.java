package com.bsimm.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.bsimm.auth.model.User;
import com.bsimm.auth.repository.RoleRepository;
import com.bsimm.auth.repository.UserRepository;

import java.util.HashSet;

import javax.persistence.TypedQuery;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public void save(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setRoles(new HashSet<>(roleRepository.findAll()));
        userRepository.save(user);
    }

    @Override
    public User findByUsername(String username) {
    	
    	//https://github.com/eugenp/tutorials/blob/master/software-security/sql-injection-samples/src/main/java/com/baeldung/examples/security/sql/AccountDAO.java  
        return userRepository.findByUsername(username);
    }
}
