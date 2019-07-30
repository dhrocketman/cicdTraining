package com.bsimm.auth.service;

import com.bsimm.auth.model.User;

public interface UserService {
    void save(User user);

    User findByUsername(String username);
}
