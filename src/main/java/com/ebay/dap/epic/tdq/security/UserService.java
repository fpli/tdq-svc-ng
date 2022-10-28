package com.ebay.dap.epic.tdq.security;

import com.ebay.dap.epic.tdq.security.model.Role;
import com.ebay.dap.epic.tdq.security.model.User;

import java.util.List;

public interface UserService {

  User getByUsername(String username);

  User updateUserLastLoginDate(String username);

  User setUserRoles(List<Role> roles);
}
