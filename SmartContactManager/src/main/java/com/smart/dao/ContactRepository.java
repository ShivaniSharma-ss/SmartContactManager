package com.smart.dao;

import java.util.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contact;
import com.smart.entities.User;
//a page is a sublist of a list of objects.It allows gain information about the position of entire list.
public interface ContactRepository extends JpaRepository<Contact,Integer>{
//pagination
	@Query("from Contact as c where c.user.id=:userId")
	//currentpage-page
	//contact per page -5
	public Page<Contact> findContactsByUser(@Param("userId") int userId,Pageable pageable);

	//search
	public List<Contact>findByNameContainingAndUser(String name,User user);
}
