
package com.wilkom.cronproject.repository;

import org.springframework.stereotype.Repository;

import com.wilkom.cronproject.model.Account;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
}
