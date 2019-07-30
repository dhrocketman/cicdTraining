package com.bsimm.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bsimm.auth.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long>{
}
