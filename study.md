## 短链系统

## 功能

- [x] 账号系统
- [x] 短链分组
- [x] 生成短链
- [ ] 流量包系统

## 配置搭建

```
docker run -d \
-e NACOS_AUTH_ENABLE=true \
-e MODE=standalone \
-e JVM_XMS=128m \
-e JVM_XMX=128m \
-e JVM_XMN=128m \
-p 8848:8848 \
-p 9848:9848 \
-p 9849:9849 \
-e SPRING_DATASOURCE_PLATFORM=mysql \
-e MYSQL_SERVICE_HOST=rm-wz983qrr9nc08t030to.mysql.rds.aliyuncs.com \
-e MYSQL_SERVICE_PORT=3306 \
-e MYSQL_SERVICE_USER=root \
-e MYSQL_SERVICE_PASSWORD=Novohit \
-e MYSQL_SERVICE_DB_NAME=nacos_config \
-e MYSQL_SERVICE_DB_PARAM='characterEncoding=utf8&connectTimeout=10000&socketTimeout=30000&autoReconnect=true&useSSL=false' \
--restart=always \
--privileged=true \
-v /home/data/nacos/logs:/home/nacos/logs \
--name nacos_auth \
nacos/nacos-server:v2.0.4
```

```
docker run -d --hostname my-rabbit --name plato_rabbitmq -p 15672:15672 -p 5672:5672 -e RABBITMQ_DEFAULT_USER=admin -e RABBITMQ_DEFAULT_PASS=admin rabbitmq:3-management
```



- 不支持直接挂载文件，只能挂载文件夹
- 想要挂载文件，必须宿主机也要有对应的同名文件

```
sudo docker run --privileged --name nginx -d -p 8088:80 \
-v /data/nginx/html:/usr/share/nginx/html \
-v /data/nginx/conf/nginx.conf:/etc/nginx/nginx.conf \
-v /data/nginx/conf.d/default.conf:/etc/nginx/conf.d/default.conf \
-v /data/nginx/logs:/var/log/nginx nginx
```



```
docker run -d --name zookeeper -p 2181:2181 -t wurstmeister/zookeeper
```

```
docker run -d --name plato_kafka \
-p 9092:9092 \
--link zookeeper \
-e KAFKA_BROKER_ID=0 \
-e KAFKA_HEAP_OPTS=-Xmx256M \
-e KAFKA_HEAP_OPTS=-Xms128M \
-e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
-e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://公网ip:9092 \
-e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092 \
wurstmeister/kafka:2.13-2.7.0
```



```
docker run -d --name kafka -p 9092:9092 --link zookeeper --env KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 --env KAFKA_ADVERTISED_HOST_NAME=localhost --env KAFKA_ADVERTISED_PORT=9092 wurstmeister/kafka:2.13-2.7.0
```



```
docker run -d --name kafka-map -p 8049:8080 -e DEFAULT_USERNAME=admin -e DEFAULT_PASSWORD=admin  dushixiang/kafka-map:latest
```



```
```



## 账户模块

- 索引规范



```
CREATE TABLE `account` (
	`id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
	`account_no` BIGINT DEFAULT NULL,
	`avatar` VARCHAR ( 255 ) CHARACTER 
	SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '头像',
	`phone` VARCHAR ( 128 ) CHARACTER 
	SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '手机号',
	`password` VARCHAR ( 128 ) CHARACTER 
	SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '密码',
	`secret` VARCHAR ( 64 ) CHARACTER 
	SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '盐，用于个人敏感信息处理',
	`mail` VARCHAR ( 128 ) CHARACTER 
	SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '邮箱',
	`username` VARCHAR ( 255 ) CHARACTER 
	SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '用户名',
	`auth` VARCHAR ( 32 ) CHARACTER 
	SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '认证级别，DEFAULT，REALNAME，ENTERPRISE，访问次数不一样',
	`create_time` datetime DEFAULT CURRENT_TIMESTAMP,
	`update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`delete_time` datetime DEFAULT NULL,
	PRIMARY KEY ( `id` ),
	UNIQUE KEY `uk_phone` ( `phone` ) USING BTREE,
UNIQUE KEY `uk_account` ( `account_no` ) USING BTREE 
) ENGINE = INNODB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_bin;
```



```
CREATE TABLE `traffic` (
	`id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
	`day_limit` INT DEFAULT NULL COMMENT '每天限制多少条短链',
	`day_used` INT DEFAULT NULL COMMENT '当天用了多少条短链',
	`total_limit` INT DEFAULT NULL COMMENT '总次数，活码才用',
	`account_no` BIGINT DEFAULT NULL COMMENT '账户',
	`out_trade_no` VARCHAR ( 64 ) CHARACTER 
	SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '订单号',
	`level` VARCHAR ( 64 ) CHARACTER 
	SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '产品层级：FIRST青铜 SECOND黄金THIRD砖石',
	`expired_date` date DEFAULT NULL COMMENT '过期时间',
	`plugin_type` VARCHAR ( 64 ) CHARACTER 
	SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '插件类型',
	`product_id` BIGINT DEFAULT NULL COMMENT '商品主键',
	`create_time` datetime DEFAULT CURRENT_TIMESTAMP,
	`update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`delete_time` datetime DEFAULT NULL,
	PRIMARY KEY ( `id` ),
	UNIQUE KEY `uk_trade_no` ( `out_trade_no`, `account_no` ) USING BTREE,
KEY `idx_account_no` ( `account_no` ) USING BTREE 
) ENGINE = INNODB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_bin;
```



### 短信验证码

短信验证码防刷：

- 前端防抖

- 添加图像验证码

- 基于滑动窗口算法对调用方法进行全局限制

- 在方法中进行两次Redis存储，一次存储验证码code并设置过期时间10min，一次存储额外的key并设置过期时间60s用于判断是否重复发送

  ```
  缺点：
  两次redis操作为非原子操作，存在不一致性
  增加的额外的key-value存储，浪费空间
  ```

- 将两次Redis存储压缩成一次

  ```
  code拼装时间戳存储
  key:phone/captchaId value:code_timestamp
  只需要将value中的timestamp相减即可知道两次调用时间间隔
  优点:
  满足了当前节点内的原子性，也满足业务需求
  ```




## 分库分表



分库分表后的查询问题

C端用户可根据短链码的库表位路由到对应的库表

B端用户如何查看自己创建的所有短链？

多维度查询解决方案：

- 额外表字段解析配置
- NOSQL冗余
- 冗余双写





## 流量包模块



## 短链模块

```
CREATE TABLE `link_group` (
	`id` BIGINT UNSIGNED NOT NULL,
	`title` VARCHAR ( 255 ) CHARACTER 
	SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '短链分组名',
	`account_no` BIGINT DEFAULT NULL COMMENT '账户唯一标识',
	`create_time` datetime DEFAULT CURRENT_TIMESTAMP,
	`update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`delete_time` datetime DEFAULT NULL,
PRIMARY KEY ( `id` ) 
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_bin;
```



```
CREATE TABLE `short_link` (
	`id` BIGINT UNSIGNED NOT NULL,
	`group_id` BIGINT DEFAULT NULL COMMENT '分组id',
	`title` VARCHAR ( 128 ) CHARACTER 
	SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '短链标题',
	`original_url` VARCHAR ( 1024 ) CHARACTER 
	SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '原url地址',
	`domain` VARCHAR ( 128 ) CHARACTER 
	SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '短链域名',
	`code` VARCHAR ( 16 ) CHARACTER 
	SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '短链码',
	`long_hash` VARCHAR ( 64 ) CHARACTER 
	SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '长链的hash码 方便查找',
	`expired` datetime DEFAULT NULL COMMENT '过期时间 永久为-1',
	`account_no` BIGINT DEFAULT NULL COMMENT '账户唯一标识',
	`state` VARCHAR ( 16 ) CHARACTER 
	SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '短链状态 lock：锁定 active：可用',
	`link_level` VARCHAR ( 16 ) CHARACTER 
	SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '产品level FIRST青铜SECOND黄金THIRD钻石',
	`create_time` datetime DEFAULT CURRENT_TIMESTAMP,
	`update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`delete_time` datetime DEFAULT NULL,
	PRIMARY KEY ( `id` ),
UNIQUE KEY `uk_code` ( `code` ) 
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_bin;
```



```
CREATE TABLE `domain` (
	`id` BIGINT UNSIGNED NOT NULL,
	`account_no` BIGINT DEFAULT NULL COMMENT '用户自己绑定的域名',
	`domain_type` VARCHAR ( 11 ) CHARACTER 
	SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '域名类型，自建custom, 官方offical',
	`value` VARCHAR ( 255 ) CHARACTER 
	SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
	`create_time` datetime DEFAULT CURRENT_TIMESTAMP,
	`update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`delete_time` datetime DEFAULT NULL,
PRIMARY KEY ( `id` ) 
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_bin;
```



## 分布式锁

#### 单用户超领优惠券问题

**简介：讲解单用户优惠券超领业务问题和效果演示**

* 什么单用户超领优惠券
  * 优惠券限制1人限制1张，有些人却领了2张
  * 优惠券限制1人限制2张，有些人却领了3或者4张

* 案例举例和问题来源

```
前面解决了，优惠券超发的问题，但是这个个人领取的时候，有张数限制，

有个生发洗发水100元，有个10元优惠券，每人限制领劵1张

小滴课堂-老王，使用时间暂停思维来发现问题，并发领劵

A线程原先查询出来没有领劵，要再插入领劵记录前暂停
然后B线程原先查询出来也没有领劵，则插入领劵记录，然后A线程也插入领劵记录
老王就有了两个优惠券

问题来源核心：对资源的修改没有加锁，导致多个线程可以同时操作，从而导致数据不正确

解决问题：分布式锁 或者 细粒度分布式锁
```







#### 分布式核心技术-关于高并发下分布式锁你知道多少？

**简介：分布式锁核心知识介绍和注意事项**

* 避免单人超领劵
  * 加锁
    * 本地锁：synchronize、lock等，锁在当前进程内，集群部署下依旧存在问题
    * 分布式锁：Redis、Zookeeper等实现，虽然还是锁，但是多个进程共用的锁标记，可以用Redis、Zookeeper、MySQL等都可以

![image-20210208224458995](https://zwx-images-1305338888.cos.ap-guangzhou.myqcloud.com/img/2023/03/23/image-20210208224458995.png)

* 设计分布式锁应该考虑的东西
  * 排他性
    *  在分布式应用集群中，同一个方法在同一时间只能被一台机器上的一个线程执行
  * 容错性
    * 分布式锁一定能得到释放，比如客户端奔溃或者网络中断
  * 满足可重入、高性能、高可用
  * 注意分布式锁的开销、锁粒度





#### 基于Redis实现分布式锁的几种坑《上》

**简介：基于Redis实现分布式锁的几种坑**

* 实现分布式锁 可以用 Redis、Zookeeper、Mysql数据库这几种 ,   性能最好的是Redis且是最容易理解

  * 分布式锁离不开 key - value 设置

```
    key 是锁的唯一标识，一般按业务来决定命名，比如想要给一种商品的秒杀活动加锁，key 命名为 “seckill_商品ID” 。value就可以使用固定值，比如设置成1
```


​    

* 基于redis实现分布式锁，文档：http://www.redis.cn/commands.html#string

  * 加锁 SETNX key value

  ```
  setnx 的含义就是 SET if Not Exists，有两个参数 setnx(key, value)，该方法是原子性操作
  
  如果 key 不存在，则设置当前 key 成功，返回 1；
  
  如果当前 key 已经存在，则设置当前 key 失败，返回 0
  ```

  * 解锁 del (key)

  ```
  得到锁的线程执行完任务，需要释放锁，以便其他线程可以进入,调用 del(key)
  ```

  * 配置锁超时 expire (key，30s）

  ```
  客户端奔溃或者网络中断，资源将会永远被锁住,即死锁，因此需要给key配置过期时间，以保证即使没有被显式释放，这把锁也要在一定时间后自动释放
  
  ```

  * 综合伪代码

  ```
  methodA(){
    String key = "coupon_66"
  
    if（setnx（key，1） == 1）{
        expire(key,30,TimeUnit.MILLISECONDS)
        try {
            //做对应的业务逻辑
            //查询用户是否已经领券
            //如果没有则扣减库存
            //新增领劵记录
        } finally {
            del（key）
        }
    }else{
  
      //睡眠100毫秒，然后自旋调用本方法
  		methodA()
    }
  }
  ```

  * 存在哪些问题，大家自行思考下

  

  

#### 基于Redis实现分布式锁的几种坑《下》

**简介：手把手教你彻底掌握分布式锁+原生代码编写**

* * 存在什么问题？

    * 多个命令之间不是原子性操作，如`setnx`和`expire`之间，如果`setnx`成功，但是`expire`失败，且宕机了，则这个资源就是死锁

    ```
    使用原子命令：设置和配置过期时间  setnx / setex
    如: set key 1 ex 30 nx
    java里面 redisTemplate.opsForValue().setIfAbsent("seckill_1",1,30,TimeUnit.MILLISECONDS)
    ```

    ![分布式锁1.drawio](https://zwx-images-1305338888.cos.ap-guangzhou.myqcloud.com/img/2023/03/24/分布式锁1.drawio.png)

    

    * 业务超时，存在其他线程勿删，key 30秒过期，假如线程A执行很慢超过30秒，则key就被释放了，其他线程B就得到了锁，这个时候线程A执行完成，而B还没执行完成，结果就是线程A删除了线程B加的锁

    ```
    可以在 del 释放锁之前做一个判断，验证当前的锁是不是自己加的锁, 那 value 应该是存当前线程的标识或者uuid
    
    String key = "coupon_66"
    String value = Thread.currentThread().getId()
    
    if（setnx（key，value） == 1）{
        expire(key,30,TimeUnit.MILLISECONDS)
        try {
            //做对应的业务逻辑
        } finally {
        	//删除锁，判断是否是当前线程加的
        	if(get(key).equals(value)){
    					//还存在时间间隔
    					del（key）
            }
        }
    }else{
    	
    	//睡眠100毫秒，然后自旋调用本方法
    
    }
    ```

    * 进一步细化误删
      * 当线程A获取到正常值时，返回带代码中判断期间锁过期了，线程B刚好重新设置了新值，线程A那边有判断value是自己的标识，然后调用del方法，结果就是删除了新设置的线程B的值
      * 核心还是判断和删除命令 不是原子性操作导致

    

    * 那如何解决呢？下集讲解







#### 分布式锁lua脚本+redis原生代码编写

**简介：手把手教你彻底掌握分布式锁+原生代码编写**

* 前面说了redis做分布式锁存在的问题

  * 核心是保证多个指令原子性，加锁使用setnx setex 可以保证原子性，那解锁使用 判断和删除怎么保证原子性
  * 文档：http://www.redis.cn/commands/set.html
  * 多个命令的原子性：采用 lua脚本+redis, 由于【判断和删除】是lua脚本执行，所以要么全成功，要么全失败

  ```
  //获取lock的值和传递的值一样，调用删除操作返回1，否则返回0
  
  String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
  
  //Arrays.asList(lockKey)是key列表，uuid是参数
  Integer result = redisTemplate.execute(new DefaultRedisScript<>(script, Integer.class), Arrays.asList(lockKey), uuid);
  ```

  * 全部代码

  ```
  /**
  * 原生分布式锁 开始
  * 1、原子加锁 设置过期时间，防止宕机死锁
  * 2、原子解锁：需要判断是不是自己的锁
  */
  String uuid = CommonUtil.generateUUID();
  String lockKey = "lock:coupon:"+couponId;
  Boolean nativeLock=redisTemplate.opsForValue().setIfAbsent(lockKey,uuid,Duration.ofSeconds(30));
      if(nativeLock){
        //加锁成功
        log.info("加锁：{}",nativeLock);
        try {
             //执行业务  TODO
          }finally {
             String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
  
                  Integer result = redisTemplate.execute(new DefaultRedisScript<>(script, Integer.class), Arrays.asList(lockKey), uuid);
                  log.info("解锁：{}",result);
              }
  
          }else {
              //加锁失败，睡眠100毫秒，自旋重试
              try {
                  TimeUnit.MILLISECONDS.sleep(100L);
              } catch (InterruptedException e) { }
              return addCoupon( couponId, couponCategory);
          }
          //原生分布式锁 结束
  ```

  * 遗留一个问题，锁的过期时间，如何实现锁的自动续期 或者 避免业务执行时间过长，锁过期了？
    * 原生方式的话，一般把锁的过期时间设置久一点，比如10分钟时间



#### 基于Redis官方推荐-分布式锁最佳实践介绍

**简介：redis官方推荐-分布式锁最佳实践**

* 原生代码+redis实现分布式锁使用比较复杂，且有些锁续期问题更难处理

  * 官方推荐方式：https://redis.io/topics/distlock
  * 多种实现客户端框架

  ![image-20230324131604928](https://zwx-images-1305338888.cos.ap-guangzhou.myqcloud.com/img/2023/03/24/image-20230324131604928.png)

  * Redisson官方文档：https://github.com/redisson/redisson/wiki

* 聚合工程锁定版本，common项目添加依赖（多个服务都会用到分布式锁）

```
<!--分布式锁-->
<dependency>
      <groupId>org.redisson</groupId>
      <artifactId>redisson</artifactId>
      <version>3.10.1</version>
</dependency>

```

* 创建redisson客户端

```
    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private String redisPort;

    @Value("${spring.redis.password}")
    private String redisPwd;
    
		/**
     * 配置分布式锁
     * @return
     */
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        //单机模式
        //config.useSingleServer().setPassword("123456").setAddress("redis://8.129.113.233:3308");
        config.useSingleServer().setPassword(redisPwd).setAddress("redis://"+redisHost+":"+redisPort);

        //集群模式
        //config.useClusterServers()
        //.setScanInterval(2000)
        //.addNodeAddress("redis://10.0.29.30:6379", "redis://10.0.29.95:6379")
        // .addNodeAddress("redis://127.0.0.1:6379");

        RedissonClient redisson = Redisson.create(config);

        return redisson;
    }

```

* 模拟controller接口测试







#### 实战Redisson实现优惠券微服务领劵接口的分布式锁

**简介：redisson实现优惠券微服务领劵接口的分布式锁**

* 优惠券微服务，分布式锁实现方式

```
Lock lock = redisson.getLock("lock:coupon:"+couponId);
//阻塞式等待，一个线程获取锁后，其他线程只能等待，和原生的方式循环调用不一样
lock.lock();
        try {
            CouponDO couponDO = couponMapper.selectOne(new QueryWrapper<CouponDO>().eq("id", couponId)
                    .eq("category", couponCategory)
                    .eq("publish", CouponPublishEnum.PUBLISH));

            this.couponCheck(couponDO,loginUser.getId());

            CouponRecordDO couponRecordDO = new CouponRecordDO();
            BeanUtils.copyProperties(couponDO,couponRecordDO);
            couponRecordDO.setCreateTime(new Date());
            couponRecordDO.setUseState(CouponStateEnum.NEW.name());
            couponRecordDO.setUserId(loginUser.getId());
            couponRecordDO.setUserName(loginUser.getName());
            couponRecordDO.setCouponId(couponId);
            couponRecordDO.setId(null);
            //高并发下扣减劵库存，采用乐观锁,当前stock做版本号,一次只能领取1张
            int rows = couponMapper.reduceStock(couponId);

            if(rows == 1){
                //库存扣减成功才保存
                couponRecordMapper.insert(couponRecordDO);
            }else {
                log.warn("发放优惠券失败:{},用户:{}",couponDO,loginUser);
                throw new BizException(BizCodeEnum.COUPON_NO_STOCK);
            }

        }finally {
            lock.unlock();
        }
```





#### Redisson是怎样解决分布式锁的里面的坑

**简介：redisson解决分布式锁里面的坑**

* 问题 :  Redis锁的过期时间小于业务的执行时间该如何续期？  

  * watch dog看门狗机制

  ```
  负责储存这个分布式锁的Redisson节点宕机以后，而且这个锁正好处于锁住的状态时，这个锁会出现锁死的状态。或者业务执行时间过长导致锁过期，
  
  为了避免这种情况的发生，Redisson内部提供了一个监控锁的看门狗，它的作用是在Redisson实例被关闭前，不断的延长锁的有效期。
  
  Redisson中客户端一旦加锁成功，就会启动一个watch dog看门狗。watch dog是一个后台线程，会每隔10秒检查一下，如果客户端还持有锁key，那么就会不断的延长锁key的生存时间
  
  
  默认情况下，看门狗的检查锁的超时时间是30秒钟，也可以通过修改Config.lockWatchdogTimeout来另行指定
  ```

  * 指定加锁时间

  ```
  // 加锁以后10秒钟自动解锁
  // 无需调用unlock方法手动解锁
  lock.lock(10, TimeUnit.SECONDS);
  
  // 尝试加锁，最多等待100秒，上锁以后10秒自动解锁
  boolean res = lock.tryLock(100, 10, TimeUnit.SECONDS);
  
  if (res) {
     try {
       ...
     } finally {
         lock.unlock();
     }
  }
  ```

  





```
{"bizId":"04ulWBP0","dnu":0,"eventType":"LINK","ip":"14.117.64.1","timestamp":1680675798852,"udid":"4B685B1B57FA7E16EF12E849C418FD96","userAgent":"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36"}
{"bizId":"04ulWBP0","dnu":0,"eventType":"LINK","ip":"5.62.152.1","timestamp":1680675798852,"udid":"4B685B1B57FA7E16EF12E849C418FD96","userAgent":"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36"}
{"bizId":"04ulWBP0","dnu":0,"eventType":"LINK","ip":"192.168.0.1","timestamp":1680675798852,"udid":"6B685B1B57FA7E16EF12E849C418FD96","userAgent":"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36"}
```



```
CREATE TABLE default.access_stats
(
`id` String,
`code` String,
`referer` String,
`nu` UInt64,
`account_no` UInt64,
`country` String,
`province` String,
`city` String,
`ip` String,
`isp` String,
`browser_type` String,
`os` String,
`device_type` String,
`device_manufacturer` String,
`pv` UInt64,
`uv` UInt64,
`start` DateTime64,
`end` DateTime64,
`timestamp` UInt64
)
ENGINE = ReplacingMergeTree(`timestamp`)
PARTITION BY toYYYYMMDD(`start`)
PRIMARY KEY id
ORDER BY (
`id`,
`start`,
`end`,
code,
country,
province,
city,
referer,
nu,
ip,
browser_type,
os,
device_type);
```



## 压测

简介：目前用的常用测试工具对比

- LoadRunner

    - 性能稳定，压测结果及细粒度大，可以自定义脚本进行压测，但是太过于重大，功能比较繁多

- Apache AB(单接口压测最方便)
    - 模拟多线程并发请求,ab命令对发出负载的计算机要求很低，既不会占用很多CPU，也不会占用太多的内存，但却会给目标服务器造成巨大的负载, 简单DDOS攻击等

- Webbench
    - webbench首先fork出多个子进程，每个子进程都循环做web访问测试。子进程把访问的结果通过pipe告诉父进程，父进程做最终的统计结果。
- Jmeter (GUI )
    - 开源免费，功能强大，在互联网公司普遍使用
    - 压测不同的协议和应用
        - Web - HTTP, HTTPS (Java, NodeJS, PHP, ASP.NET, ...)
        - SOAP / REST Webservices
        - FTP
        - Database via JDBC
        - LDAP 轻量目录访问协议
        - Message-oriented middleware (MOM) via JMS
        - Mail - SMTP(S), POP3(S) and IMAP(S)
        - TCP等等
    - 使用场景及优点
        - 功能测试
        - 压力测试
        - 分布式压力测试
        - 纯java开发
        - 上手容易，高性能
        - 提供测试数据分析
        - 各种报表数据图形展示



压测工具本地快速安装Jmeter5.x





1. 图像数据的网络爬取

本实验选择了一个公开的动物分类数据集作为实验样本，数据集包含了10种不同类别的动物图像。在进行数据爬取时，我们采用了Python中的requests和beautifulsoup4库来实现对图片URL的抓取，并将其保存到本地文件夹中。由于网络爬取的过程中可能会遇到访问限制、网络波动等问题，因此在实际操作中需要具备一定的技巧和经验。

1. 图像数据的整理与批量标注

在数据爬取完成后，我们需要对其进行整理和批量标注。具体来说，我们首先需要将图片按照类别分别存储到不同的文件夹中。然后，我们需要手动为每一张图片打上标签，标签可以采用数字、英文或中文等形式，具体取决于实验需要和个人喜好。



## 部署

### Jenkins

```
docker run -d \
-u root \
--name plato_jenkins \
-p 9302:8080 \
-v /home/admin/plato/docker/jenkins:/var/jenkins_home \
-v /var/run/docker.sock:/var/run/docker.sock \
-v /usr/bin/docker:/usr/bin/docker \
jenkins/jenkins:2.319.3-lts-jdk11
```



```
echo "登录阿里云镜像仓库"
docker login --username=novohit registry.cn-hangzhou.aliyuncs.com --password=zb001222
echo "构建plato-account"
cd plato-account
mvn install
ls -alh


ls -alh
cd plato-account
ls -alh
echo "开始构建plato-account"
mvn install -Dmaven.test.skip=true dockerfile:build
docker tag plato/plato-account:latest registry.cn-hangzhou.aliyuncs.com/plato-link/plato-account:v1.0
docker push registry.cn-hangzhou.aliyuncs.com/plato-link/plato-account:v1.0
mvn clean
echo "账号服务构建推送成功"
echo "======构建脚本执行完毕======"
```



```
        SELECT DISTINCT t.group_id, IFNULL(link_sum,0)
				FROM short_link_mapping_1 as t
				LEFT JOIN(
				SELECT sub.group_id, count(*) as link_sum
        FROM short_link_mapping_1 as sub
        WHERE account_no = 841437389090979840
        AND delete_time IS NULL
        AND sub.group_id IN (1634921945596243970, 1634922018128343042, 1639527665725603841, 1640718778440388610)
        GROUP BY
        sub.group_id
				) sub ON sub.group_id = t.group_id;
```



### Rancher

创建Rancher挂载目录

```
mkdir -p /data/rancher_home/rancher
mkdir -p /data/rancher_home/auditlog
```



```
docker run -d --privileged --restart=unless-stopped \
-p 80:80 -p 443:443 \
-v /data/rancher_home/rancher:/var/lib/rancher \
-v /data/rancher_home/auditlog:/var/log/auditlog \
--name plato_rancher1 rancher/rancher:v2.5.7
```



## 开发遇到的问题

### Gson.toJson()导致OOM

原因：序列化的对象ResponseFacade/ResquestFacade中出现了循环引用





Q:在Java中为什么ResponseFacade类中有Response属性，而Response类中又还有ResponseFacade属性

A:
在Java Servlet API中，ResponseFacade是一个包装器类，用于封装Servlet响应(Response)对象，以便于在Servlet容器内部使用。它实现了ServletResponse接口，并持有一个ServletResponse类型的成员变量response。

而在ServletResponse接口的实现类Response中，也有一个ResponseFacade类型的成员变量facade，它用于存储ResponseFacade对象。这是因为在Servlet容器内部，实际处理请求和响应的是Servlet容器，而不是Servlet本身。当Servlet调用ServletResponse的方法时，容器会将这些方法调用传递给ResponseFacade对象，然后再由ResponseFacade对象转发给真正的ServletResponse对象。

这种设计模式称为外观模式(Facade Pattern)，它提供了一种简单的方式来访问复杂子系统的一部分，而无需了解其内部实现。在Servlet API中，ResponseFacade对象充当了Servlet容器与Servlet响应之间的中间层，封装了Servlet响应对象的实现细节，使得Servlet可以更方便地进行响应处理。

Q:这不是循环引用了吗

A:
确实，从代码结构上看，ResponseFacade类中持有了Response对象，而Response类中又持有了ResponseFacade对象，因此存在循环引用的情况。

但是，这种循环引用并不会导致程序出错或内存泄漏，因为它们所持有的对象并不是相互依赖的。在Servlet容器内部，实际处理请求和响应的是Servlet容器，而不是Servlet本身。当Servlet调用ServletResponse的方法时，容器会将这些方法调用传递给ResponseFacade对象，然后再由ResponseFacade对象转发给真正的ServletResponse对象。因此，ResponseFacade对象只是作为一个中间层进行了一些简单的封装，而不会对Response对象产生任何影响。

在Java语言中，循环引用不一定会导致内存泄漏，因为Java的垃圾回收器会自动识别和处理这种情况。当对象之间存在循环引用时，只要这些对象不再被程序所引用，它们就会被垃圾回收器识别并回收。因此，从内存管理的角度来看，循环引用并不是一个严重的问题。


