package com.ebay.dap.epic.tdq.data.handler;

import com.ebay.dap.epic.tdq.data.enums.Role;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

@Slf4j
public class RoleListTypeHandler extends BaseTypeHandler<List<Role>> {

  @Override
  public void setNonNullParameter(PreparedStatement preparedStatement, int i, List<Role> roles, JdbcType jdbcType) throws SQLException {
    String str = roles.stream().map(Role::getCode).collect(Collectors.joining(","));
    preparedStatement.setString(i, str);
  }

  @Override
  public List<Role> getNullableResult(ResultSet resultSet, String s) throws SQLException {
    List<Role> roles = new ArrayList<>();
    for (String str : resultSet.getString(s).split(",")) {
      roles.add(Role.fromCode(Integer.parseInt(str)));
    }
    return roles;
  }

  @Override
  public List<Role> getNullableResult(ResultSet resultSet, int i) throws SQLException {
    List<Role> roles = new ArrayList<>();
    for (String str : resultSet.getString(i).split(",")) {
      roles.add(Role.fromCode(Integer.parseInt(str)));
    }
    return roles;
  }

  @Override
  public List<Role> getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
    List<Role> roles = new ArrayList<>();
    for (String str : callableStatement.getString(i).split(",")) {
      roles.add(Role.fromCode(Integer.parseInt(str)));
    }
    return roles;
  }
}
