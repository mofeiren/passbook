package com.imooc.passbook.service.impl;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: kkc
 * Email: wochiyouzhi@gmail.com
 * Date: 2018-11-11
 * Time: 上午11:47
 */

import com.imooc.passbook.constant.Constants;
import com.imooc.passbook.service.IUserService;
import com.imooc.passbook.vo.Response;
import com.imooc.passbook.vo.User;
import com.spring4all.spring.boot.starter.hbase.api.HbaseTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建用户服务实现
 */
@Slf4j
@Service
public class UserServiceImpl implements IUserService {

    //HBase 客户端
    private final HbaseTemplate hbaseTemplate;

    //redis 客户端
    private final StringRedisTemplate redisTemplate;

    @Autowired
    public UserServiceImpl(HbaseTemplate hbaseTemplate, StringRedisTemplate redisTemplate) {
        this.hbaseTemplate = hbaseTemplate;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 创建用户
     * @param user {@link User}
     * @return {@link Response}
     * @throws Exception
     */
    @Override
    public Response createUser(User user) throws Exception {

        byte[] FAMILY_B = Constants.UserTable.FAMILY_B.getBytes();
        byte[] NAME = Constants.UserTable.NAME.getBytes();
        byte[] AGE = Constants.UserTable.AGE.getBytes();
        byte[] SEX = Constants.UserTable.SEX.getBytes();

        byte[] FANILY_O = Constants.UserTable.FAMILY_O.getBytes();
        byte[] PHONE = Constants.UserTable.PHONE.getBytes();
        byte[] ADDRESS = Constants.UserTable.ADDRESS.getBytes();

        Long curCount = redisTemplate.opsForValue().increment(Constants.USE_COUNT_REDIS_KEY,1);
        Long userId = genUserId(curCount);

        List<Mutation> datas = new ArrayList<Mutation>();
        Put put = new Put(Bytes.toBytes(userId));

        put.addColumn(FAMILY_B,NAME,Bytes.toBytes(user.getBaseInfo().getName()));
        put.addColumn(FAMILY_B,AGE,Bytes.toBytes(user.getBaseInfo().getAge()));
        put.addColumn(FAMILY_B,SEX,Bytes.toBytes(user.getBaseInfo().getAge()));

        put.addColumn(FANILY_O,PHONE,Bytes.toBytes(user.getOtherInfo().getPhone()));
        put.addColumn(FANILY_O,ADDRESS,Bytes.toBytes(user.getOtherInfo().getAddress()));

        datas.add(put);

        hbaseTemplate.saveOrUpdates(Constants.UserTable.TABLE_NAME, datas);

        user.setId(userId);
        return new Response(user);
    }

    /**
     * 生成UserId
     * @param prefix 当前用户数
     * @return
     */
    private Long genUserId(Long prefix){
        String suffix = RandomStringUtils.randomNumeric(5);
        return Long.valueOf(prefix+suffix);
    }
}
