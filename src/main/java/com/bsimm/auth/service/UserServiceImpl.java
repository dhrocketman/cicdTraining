package com.bsimm.auth.service;

import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.bsimm.auth.model.User;
import com.bsimm.auth.model.UserRowMapper;
import com.bsimm.auth.repository.RoleRepository;
import com.bsimm.auth.repository.UserRepository;

import java.util.Base64;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Override
    public void save(User user) {

    	/**
    	 * Instance of Base64 encoding for password. Uncomment while testing with SAST tools
    	 */
    	user.setPassword(Base64.getEncoder().encodeToString(user.getPassword().getBytes())); 
        //user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setRoles(new HashSet<>(roleRepository.findAll()));
        userRepository.save(user);
    }

    @Override
    public User findByUsername(String username) {
    	
    	/**
    	 * Instance of SQL Injection using the jdbcTemplate and a vulnerable query string. 
    	 * Uncomment when testing with SAST tools.
    	 */
    	String sql = "SELECT username, password FROM user where username='" + username + "'";
    	RowMapper<User> rowMapper = new UserRowMapper();
    	
    	return jdbcTemplate.query(sql, rowMapper).get(0);
    	//return userRepository.findByUsername(username);
    }
}
