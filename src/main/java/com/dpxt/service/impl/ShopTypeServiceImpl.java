package com.dpxt.service.impl;

import cn.hutool.json.JSONUtil;
import com.dpxt.dto.Result;
import com.dpxt.entity.ShopType;
import com.dpxt.mapper.ShopTypeMapper;
import com.dpxt.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dpxt.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public Result queryTypeList() {
        // 从redis查询缓存
        List<String> shopTypes  = stringRedisTemplate.opsForList().range(RedisConstants.CACHE_SHOP_TYPE_KEY, 0 ,-1);
        // 判断是否存在
        if (!shopTypes .isEmpty()) {
            // 如果存在，则返回
            List<ShopType> shopTypeList = new ArrayList<>();
            for (String types : shopTypes ) {
                ShopType shopType = JSONUtil.toBean(types, ShopType.class);
                shopTypeList.add(shopType);
            }
            return Result.ok(shopTypeList);
        }
        // 不存在，根据id查询数据库
        List<ShopType> tmp = query().orderByAsc("sort").list();
        if (tmp == null) {
            return Result.fail("店铺不存在");
        }
        //查到转为json字符串，存入redis
        for (ShopType shopType : tmp) {
            String s = JSONUtil.toJsonStr(shopType);
            shopTypes.add(s);
        }
        // 存入redis中
        stringRedisTemplate.opsForList().leftPushAll(RedisConstants.CACHE_SHOP_TYPE_KEY, shopTypes);
        return Result.ok(tmp);
    }
}
