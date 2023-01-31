package com.ebay.dap.epic.tdq.data.mapper.mybatis;


import com.baomidou.mybatisplus.test.autoconfigure.MybatisPlusTest;
import com.ebay.dap.epic.tdq.AbstractBaseTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;


@MybatisPlusTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class AbstractMapperTest extends AbstractBaseTest {
}
