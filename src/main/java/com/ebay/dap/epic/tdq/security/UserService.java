package com.ebay.dap.epic.tdq.security;

import com.ebay.dap.epic.tdq.data.enums.Role;
import com.ebay.dap.epic.tdq.security.model.User;

public interface UserService {

    User getByUsername(String username);

    /**
     * If user haven't logged in before, add user to db and give user a default user role.
     * If user have logged in before, update user's last login time.
     *
     * @param username
     * @return
     */
    User login(String username);

    User createUser(String username, Role... roles);

}
