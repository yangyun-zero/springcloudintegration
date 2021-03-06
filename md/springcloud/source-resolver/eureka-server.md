## springcloud-eureka

###Describle: 

#### 关于 ZK 和 Eureka

ZK 的设计原则是 CP, 强一致性和分区容错性. 保证了数据强一致性, 但舍弃了可用性, 如果**出现网络问题可能会影响ZK 的选举, 到之后 ZK 注册中心不可用**

Eureka 的设计原则是 AP, 可用性和分区容错性. 保证了注册中心的可用性, 舍**弃了数据一致性, 各节点间的数据有可能是不一致的(会最终一致)**

#### Eureka：

#####Eureka Instance: 

当前 Eureka 实例信息

```java
@ConfigurationProperties("eureka.instance")
public class EurekaInstanceConfigBean implements CloudEurekaInstanceConfig, EnvironmentAware {
    // 实例心跳检查间隔时间
    private int leaseRenewalIntervalInSeconds = 30;
    
    // 超时时间, 当服务超过 leaseExpirationDurationInSeconds 时间, Server 就认为它不可用, 然后剔除
    private int leaseExpirationDurationInSeconds = 90;
    
    // 实例元数据, 提供实例上下文和端口
    private Map<String, String> metadataMap = new HashMap<>();
    
    // 实例部署的数据中心. 如: AWS, MyOwn	
	private DataCenterInfo dataCenterInfo = new MyDataCenterInfo(
			DataCenterInfo.Name.MyOwn);
}
```

#####Eureka Server：

注册中心服务端，用于维护和管理注册服务列表

######  EurekaServerConfigBean 

对注册中心的特性配置

```java
@ConfigurationProperties(EurekaServerConfigBean.PREFIX)
public class EurekaServerConfigBean implements EurekaServerConfig {
    // 自我保护续约百分比阀值因子. 如果实际续约数小于续约数阀值, 则开启自我保护
    private double renewalPercentThreshold = 0.85;
    
    // 续约数阀值更新频率
    private int renewalThresholdUpdateIntervalMs = 15 * MINUTES;

    // Eureka Server 节点更新频率
	private int peerEurekaNodesUpdateIntervalMs = 10 * MINUTES;
    
    // 当从其他节点同步实例信息为空时等待时间
    private int waitTimeInMsWhenSyncEmpty = 5 * MINUTES;
    
    // 节点间连接的超时时间
    private int peerNodeConnectTimeoutMs = 200;
    
    // 节点间读取信息的超时时间
    private int peerNodeReadTimeoutMs = 200;
    
    // 节点连接的总数
    private int peerNodeTotalConnections = 1000;
    
    // 单个节点间连接数
    private int peerNodeTotalConnectionsPerHost = 500;
    
    // 节点间连接诶空闲超时时间
    private int peerNodeConnectionIdleTimeoutSeconds = 30;
    
    // 增量队列缓存时间
	private long retentionTimeInMSInDeltaQueue = 3 * MINUTES;
    
    // 清理增量队列过期的频率
    private long deltaRetentionTimerIntervalInMs = 30 * 1000;

    // 剔除任务频率
	private long evictionIntervalTimerInMs = 60 * 1000;
    
    // 注册列表缓存超时时间(当注册列表没有变化时)
    private long responseCacheAutoExpirationInSeconds = 180;
    
    // 注册列表缓存更新频率
    private long responseCacheUpdateIntervalMs = 30 * 1000;
    
    // 是否开启注册列表二级缓存
    private boolean useReadOnlyResponseCache = true;
    
    // 状态同步最大线程数
    private int maxThreadsForStatusReplication = 1;
    
    // 注册信息同步重试次数
    private int registrySyncRetries = 0;
    
    // 注册信息同步重试期间的时间间隔
    private long registrySyncRetryWaitMs = 30 * 1000;
    
    // 节点间同步时间的最大容量
    private int maxElementsInPeerReplicationPool = 10000;
    
    // 节点间同步的最大线程数
    private int maxThreadsForPeerReplication = 20;
    
    // 节点同步的最大时间, 毫秒
    private int maxTimeForReplication = 30000;
}
```

###Eureka 总体架构

![](F:\git\springcloudintegration\md\springcloud\images\eureka_01.png)

#### 服务提供者

1. 启动后, 向注册中心发起 registry 请求, 注册服务
2. 在运行过程中, 定时向注册中心发送 renew 请求, 证明我还活着
3. 停止服务, 向注册中心发送 cancel 请求, 清空当前服务注册信息

####服务消费者

1. 启动后, 从注册中心拉取服务注册信息

2. 在运行过程中, 定时更新服务注册信息

3. 服务消费者发起远程调用

   a. 服务消费者(北京) 会从服务注册中心选择同机房的服务提供者(北京)发起远程调用. 只有同机房的服务提供者挂了才会选择其他机房的服务提供者(天津)

   b. 服务消费者(天津) 因为同机房内没有服务提供者, 则会按负载均衡算法选择北京或青岛的服务提供者发起远程调用

#### 注册中心

1. 启动后, 从其他节点拉取服务注册信息
2. 运行过程中, 会定时运行 evict 任务, 剔除没有按时 renew 的任务(包括非正常停止和网络故障的服务)
3. 运行过程中, 接受到 registry, renew, cancel 请求, 都会同步至其他注册中心节点



#####Eureka Client：

注册中心客户端，向注册中心注册服务的应用都可以叫做Eureka Client（**包括Eureka Server本身**, 因为 Eureka Server 本身也是可以注册到注册中心的）

**在实际使用中我们通常会在 yml 配置文件中添加一下信息**

```yml
eureka:
  client:
    register-with-eureka: false   #当前eureka-server 自己不注册进服务列表中
```

######  EurekaClientConfigBean 

Eureka client 特性配置类

```java
@ConfigurationProperties(EurekaClientConfigBean.PREFIX)
public class EurekaClientConfigBean implements EurekaClientConfig {
    // 定时从 Eureka Server 拉取服务注册信息的时间间隔
	private int registryFetchIntervalSeconds = 30;
    
    // 定时将实例信息(如果变化了)复制到 Eureka Server 的时间间隔 InstanceInfoReplicator 线程
    private int instanceInfoReplicationIntervalSeconds = 30;
    
    // 首次将实例信息复制到 Eureka Server 的延迟时间 InstanceInfoReplicator 线程
    private int initialInstanceInfoReplicationIntervalSeconds = 40;
    
    // 从 Eureka Server 读取信息的超时时间
    private int eurekaServerReadTimeoutSeconds = 8;
    
    // Eureka Client 第一次启动时获取服务注册信息的调用的回溯实现. 首次启动会检查有没有 BackupRegistry 的实现类, 有, 则优先从这个实现类中获取服务注册信息
    private String backupRegistryImpl;
    
    // 允许从 Eureka Client 连接到所有 Eureka Server 的连接总数
    private int eurekaServerTotalConnections = 200;
    
    // Eureka Client 连接到单台 Eureka Server 的连接总数
    private int eurekaServerTotalConnectionsPerHost = 50;
    
    // Eureka Client 和 Eureka Server 之间的 Http 连接的空闲超时时间, 防火墙会在几分钟后清除空闲连接, 导致这些连接处于不确定状态
    private int eurekaConnectionIdleTimeoutSeconds = 30;
    
    // 心跳检测线程池大小
    private int heartbeatExecutorThreadPoolSize = 2;
    
    // 在检测心跳过程中超时再次执行检测的最大延迟时间
    // 10 * eureka.instance.leaseRenewalIntervalInSeconds 
    private int heartbeatExecutorExponentialBackOffBound = 10;
    
    // 获取注册信息 cacheRefreshExecutorThreadPoolSize 大小
    private int cacheRefreshExecutorThreadPoolSize = 2;
    
    // cacheRefreshExecutor 再次执行的最大延迟倍数
    // 10 *eureka.client.registryFetchIntervalSeconds
    private int cacheRefreshExecutorExponentialBackOffBound = 10;
}
```

Eureka 注册中心启动, 会加载 EurekaServerConfig 和 EurekaClientConfig 配置的相关连接信息(有点类似 JDBC 连接数据库那部分配置)

ApplicationInfoManager(Sinleton): 初始化注册到Eureka服务器所需信息和其他组件

EurekaClient: 负责管理注册到注册中心的客户端, 数据的存储, 缓存, 服务的剔除和更新

InstanceRegistry: 处理所有来至 eureka 客户端注册表请求

![../images/1568968777(1).jpg](../images/1568968777(1).jpg)

####version

```pom
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-dependencies</artifactId>
  <version>Finchley.RELEASE</version>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-dependencies</artifactId>
  <version>2.0.5.RELEASE</version>
</dependency>
```

![../images/eurekaserver1.png](../images/eurekaserver1.png)

###Eureka 注册中心启动源码

####EurekaServerAutoConfiguration

```java
/**
 * @author Gunnar Hillert
 * @author Biju Kunjummen
 * @author Fahim Farook
 * eureka-server 自动配置类
 * xxx.yml 配置文件里面的信息在程序启动就已经被加载, 这部分属于 boot 启动时加载
 */
@Configuration
@Import(EurekaServerInitializerConfiguration.class)
@ConditionalOnBean(EurekaServerMarkerConfiguration.Marker.class)
@EnableConfigurationProperties({ EurekaDashboardProperties.class,
		InstanceRegistryProperties.class })
@PropertySource("classpath:/eureka/server.properties")
public class EurekaServerAutoConfiguration extends WebMvcConfigurerAdapter {
	/**
	 * List of packages containing Jersey resources required by the Eureka server
	 */
	private static final String[] EUREKA_PACKAGES = new String[] { "com.netflix.discovery",
			"com.netflix.eureka" };

	@Autowired
	private ApplicationInfoManager applicationInfoManager;

	@Autowired
	private EurekaServerConfig eurekaServerConfig;

	@Autowired
	private EurekaClientConfig eurekaClientConfig;

	@Autowired
	private EurekaClient eurekaClient;

	@Autowired
	private InstanceRegistryProperties instanceRegistryProperties;
	
   	// 1 开始准备工作
  	// 说明: 关于类加载属性方法静态属性静态方法加载顺序, 这个是 jvm 类加载相关资料, 可以自行了解, 其实从这里也可以看出来
  	// 静态属性初始哈 CloudJacksonJson extends LegacyJacksonJson 所以先加载父类
  	// LegacyJacksonJson 中 protected final EurekaJacksonCodec codec = new EurekaJacksonCodec();
  	// EurekaJacksonCodec 参考下面描述 1.1
  	// 1.2 上面流程走完, CloudJacksonJson protected final CloudJacksonCodec codec = new CloudJacksonCodec(); 加载自己属性, 作用同 1.1
	public static final CloudJacksonJson JACKSON_JSON = new CloudJacksonJson();

	@Bean
	public HasFeatures eurekaServerFeature() {
		return HasFeatures.namedFeature("Eureka Server",
				EurekaServerAutoConfiguration.class);
	}

  	// 3 此处加载 eureka server managere 配置信息(可以自己重写), 上一步是初始化 EurekaClientConfigBean
	@Configuration
	protected static class EurekaServerConfigBeanConfiguration {
		@Bean
		@ConditionalOnMissingBean
		public EurekaServerConfig eurekaServerConfig(EurekaClientConfig clientConfig) {
          	// 相当于配置文件, 此处配置参数, 可以根据实际情况自定义
			EurekaServerConfigBean server = new EurekaServerConfigBean();
            // 是否将自己注册进 eureka 服务中心, 我们这配置文件中为 false
			if (clientConfig.shouldRegisterWithEureka()) {
				// Set a sensible default if we are supposed to replicate
              	// server 注册信息同步重试次数, 默认 0, 
				server.setRegistrySyncRetries(5);
			}
			return server;
		}
	}

	@Bean
	@ConditionalOnProperty(prefix = "eureka.dashboard", name = "enabled", matchIfMissing = true)
	public EurekaController eurekaController() {
		return new EurekaController(this.applicationInfoManager);
	}

    // 执行完会初始化 EurekaClientConfigBean, 配置大部分都是使用的默认属性, 我们自己可以根据项目需要, 可以对属性种子进行配置
	static {
      	// 2 加载 eureka 编码器, 用作数据编码解码, 可参考 CodecWrappers 查看具体有哪些实现类
      	// CodecWrappers 工厂类, 用于生产 CodecWrapper
		CodecWrappers.registerWrapper(JACKSON_JSON);
		EurekaJacksonCodec.setInstance(JACKSON_JSON.getCodec());
	}

  	//4 解析 eureka 服务的元数据 CodecWrapper
	@Bean
	public ServerCodecs serverCodecs() {
		return new CloudServerCodecs(this.eurekaServerConfig);
	}

	private static CodecWrapper getFullJson(EurekaServerConfig serverConfig) {
		CodecWrapper codec = CodecWrappers.getCodec(serverConfig.getJsonCodecName());
		return codec == null ? CodecWrappers.getCodec(JACKSON_JSON.codecName()) : codec;
	}

	private static CodecWrapper getFullXml(EurekaServerConfig serverConfig) {
		CodecWrapper codec = CodecWrappers.getCodec(serverConfig.getXmlCodecName());
		return codec == null ? CodecWrappers.getCodec(CodecWrappers.XStreamXml.class)
				: codec;
	}

	class CloudServerCodecs extends DefaultServerCodecs {

		public CloudServerCodecs(EurekaServerConfig serverConfig) {
			super(getFullJson(serverConfig),
					CodecWrappers.getCodec(CodecWrappers.JacksonJsonMini.class),
					getFullXml(serverConfig),
					CodecWrappers.getCodec(CodecWrappers.JacksonXmlMini.class));
		}
	}

  	// 5. 此处为核心开始,实例注册, 初始化工作
	@Bean
	public PeerAwareInstanceRegistry peerAwareInstanceRegistry(
			ServerCodecs serverCodecs) {
		this.eurekaClient.getApplications(); // force initialization
      	// eurekaServerConfig: 服务端管理配置连接信息
      	// eurekaClientConfig: 客户端管理配置连接信息
      	// serverCodecs: 服务信息解析器
      	// eurekaClient: CloudEurekaClient 负责管理注册到 Eureka Server的 client;
      	// ExpectedNumberOfRenewsPerMin: 每分钟心跳次数, 默认 1
      	// DefaultOpenForTrafficCount: 每分钟期望收到得到心跳次数
		return new InstanceRegistry(this.eurekaServerConfig, this.eurekaClientConfig,
				serverCodecs, this.eurekaClient,
				this.instanceRegistryProperties.getExpectedNumberOfRenewsPerMin(),
				this.instanceRegistryProperties.getDefaultOpenForTrafficCount());
	}

    // step 6. 准备工作, 
    // 作用: 更新其他节点信息
	@Bean
	@ConditionalOnMissingBean
	public PeerEurekaNodes peerEurekaNodes(PeerAwareInstanceRegistry registry,
			ServerCodecs serverCodecs) {
            // 真正开始初始化 eureka 注册中心相关信息
		return new RefreshablePeerEurekaNodes(registry, this.eurekaServerConfig,
				this.eurekaClientConfig, serverCodecs, this.applicationInfoManager);
	}
	
	/**
	 */
	static class RefreshablePeerEurekaNodes extends PeerEurekaNodes
			implements ApplicationListener<EnvironmentChangeEvent> {

		public RefreshablePeerEurekaNodes(
				final PeerAwareInstanceRegistry registry,
				final EurekaServerConfig serverConfig,
				final EurekaClientConfig clientConfig, 
				final ServerCodecs serverCodecs,
				final ApplicationInfoManager applicationInfoManager) {
			super(registry, serverConfig, clientConfig, serverCodecs, applicationInfoManager);
		}

		@Override
		public void onApplicationEvent(final EnvironmentChangeEvent event) {
			if (shouldUpdate(event.getKeys())) {
				updatePeerEurekaNodes(resolvePeerUrls());
			}
		}
		
		/*
		 * Check whether specific properties have changed.
		 */
		protected boolean shouldUpdate(final Set<String> changedKeys) {
			assert changedKeys != null;
			
			// if eureka.client.use-dns-for-fetching-service-urls is true, then
			// service-url will not be fetched from environment.
			if (clientConfig.shouldUseDnsForFetchingServiceUrls()) {
				return false;
			}
			
			if (changedKeys.contains("eureka.client.region")) {
				return true;
			}
			
			for (final String key : changedKeys) {
				// property keys are not expected to be null.
				if (key.startsWith("eureka.client.service-url.") ||
					key.startsWith("eureka.client.availability-zones.")) {
					return true;
				}
			}
			
			return false;
		}
	}

    // step 7. 执行 step 6 的相关 task
	@Bean
	public EurekaServerContext eurekaServerContext(ServerCodecs serverCodecs,
			PeerAwareInstanceRegistry registry, PeerEurekaNodes peerEurekaNodes) {
		return new DefaultEurekaServerContext(this.eurekaServerConfig, serverCodecs,
				registry, peerEurekaNodes, this.applicationInfoManager);
	}

    // step 8. 初始化 Eureka 容器, 到这里, Eureka 启动完成
	@Bean
	public EurekaServerBootstrap eurekaServerBootstrap(PeerAwareInstanceRegistry registry,
			EurekaServerContext serverContext) {
		return new EurekaServerBootstrap(this.applicationInfoManager,
				this.eurekaClientConfig, this.eurekaServerConfig, registry,
				serverContext);
	}

	/**
	 * Register the Jersey filter
	 */
	@Bean
	public FilterRegistrationBean jerseyFilterRegistration(
			javax.ws.rs.core.Application eurekaJerseyApp) {
		FilterRegistrationBean bean = new FilterRegistrationBean();
		bean.setFilter(new ServletContainer(eurekaJerseyApp));
		bean.setOrder(Ordered.LOWEST_PRECEDENCE);
		bean.setUrlPatterns(
				Collections.singletonList(EurekaConstants.DEFAULT_PREFIX + "/*"));

		return bean;
	}

	/**
	 * Construct a Jersey {@link javax.ws.rs.core.Application} with all the resources
	 * required by the Eureka server.
     * 2 eureka 环境信息, 包含了springcloud 自带配置文件, 本地配置信息和从git上获取的配置信息, 
	 */
	@Bean
	public javax.ws.rs.core.Application jerseyApplication(Environment environment,
			ResourceLoader resourceLoader) {
		// 初始化 标准 servlet context 环境
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
				false, environment);

		// Filter to include only classes that have a particular annotation.
		//
		provider.addIncludeFilter(new AnnotationTypeFilter(Path.class));
		provider.addIncludeFilter(new AnnotationTypeFilter(Provider.class));

		// Find classes in Eureka packages (or subpackages)
		//
		Set<Class<?>> classes = new HashSet<>();
		for (String basePackage : EUREKA_PACKAGES) {
			Set<BeanDefinition> beans = provider.findCandidateComponents(basePackage);
			for (BeanDefinition bd : beans) {
				Class<?> cls = ClassUtils.resolveClassName(bd.getBeanClassName(),
						resourceLoader.getClassLoader());
				classes.add(cls);
			}
		}

		// Construct the Jersey ResourceConfig
		//
		Map<String, Object> propsAndFeatures = new HashMap<>();
		propsAndFeatures.put(
				// Skip static content used by the webapp
				ServletContainer.PROPERTY_WEB_PAGE_CONTENT_REGEX,
				EurekaConstants.DEFAULT_PREFIX + "/(fonts|images|css|js)/.*");

		DefaultResourceConfig rc = new DefaultResourceConfig(classes);
		rc.setPropertiesAndFeatures(propsAndFeatures);

		return rc;
	}

	@Bean
	public FilterRegistrationBean traceFilterRegistration(
			@Qualifier("httpTraceFilter") Filter filter) {
		FilterRegistrationBean bean = new FilterRegistrationBean();
		bean.setFilter(filter);
		bean.setOrder(Ordered.LOWEST_PRECEDENCE - 10);
		return bean;
	}
}

```

#### EurekaJacksonCodec

```java
public class EurekaJacksonCodec {
  	// 可以序列化所有标准 jdk 类型, 不支持 JAXB 注解
  	private final ObjectMapper mapper;
  	// ObjectReader ObjectWriter 序列化器, 将不同配置文件构造为一个新实例, 允许框架使用
  	private final Map<Class<?>, Supplier<ObjectReader>> objectReaderByClass;
    private final Map<Class<?>, ObjectWriter> objectWriterByClass;
  // 1.1 上层调用为: LegacyJacksonJson 中 protected final EurekaJacksonCodec codec = new EurekaJacksonCodec();
  public EurekaJacksonCodec() {
        this(formatKey(loadConfig(), VERSIONS_DELTA_TEMPLATE), formatKey(loadConfig(), 						APPS_HASHCODE_TEMPTE));
    }
  static EurekaClientConfig loadConfig() {
        return com.netflix.discovery.DiscoveryManager.getInstance().getEurekaClientConfig();
  }
  
  // 用来初始化序列化器和反序列化器
  public EurekaJacksonCodec(String versionDeltaKey, String appsHashCodeKey) {
  }
}


```

#### InstanceRegistry

```java
public class InstanceRegistry extends PeerAwareInstanceRegistryImpl
		implements ApplicationContextAware {
	// 初始化注册信息
	public InstanceRegistry(EurekaServerConfig serverConfig,
			EurekaClientConfig clientConfig, ServerCodecs serverCodecs,
			EurekaClient eurekaClient, int expectedNumberOfRenewsPerMin,
			int defaultOpenForTrafficCount) {
		super(serverConfig, clientConfig, serverCodecs, eurekaClient);

		this.expectedNumberOfRenewsPerMin = expectedNumberOfRenewsPerMin;
		this.defaultOpenForTrafficCount = defaultOpenForTrafficCount;
	}
	
	
}
```

#### PeerAwareInstanceRegistryImpl

```java
@Singleton
public class PeerAwareInstanceRegistryImpl extends AbstractInstanceRegistry implements 		PeerAwareInstanceRegistry {
    public enum Action {
          Heartbeat, Register, Cancel, StatusUpdate, DeleteStatusOverride;

          private com.netflix.servo.monitor.Timer timer = Monitors.newTimer(this.name());

          public com.netflix.servo.monitor.Timer getTimer() {
              return this.timer;
          }
      }
  
  	@Inject
    public PeerAwareInstanceRegistryImpl(
            EurekaServerConfig serverConfig,
            EurekaClientConfig clientConfig,
            ServerCodecs serverCodecs,
            EurekaClient eurekaClient
    ) {
        super(serverConfig, clientConfig, serverCodecs);
        this.eurekaClient = eurekaClient;
        this.numberOfReplicationsLastMin = new MeasuredRate(1000 * 60 * 1);
        // We first check if the instance is STARTING or DOWN, then we check explicit overrides,
        // then we check the status of a potentially existing lease.
      	// 检查当前实例状态, 续约状态
        this.instanceStatusOverrideRule = new FirstMatchWinsCompositeRule(new DownOrStartingRule(),
                new OverrideExistsRule(overriddenInstanceStatusMap), new LeaseExistsRule());
    }
    
    // 在 7 之后, Eureka Server 上下文初始化后执行
    @Override
    public void init(PeerEurekaNodes peerEurekaNodes) throws Exception {
        // 节点计数器
        this.numberOfReplicationsLastMin.start();
        this.peerEurekaNodes = peerEurekaNodes;
        // 初始化缓存数据层, 详见 ResponseCacheImpl
        initializedResponseCache();
        // 15 分钟更新一次续约阀值, 确保由于网络问题而导致太多实例失效
        scheduleRenewalThresholdUpdateTask();
        initRemoteRegionRegistry();

        try {
            // 监听注册
            Monitors.registerObject(this);
        } catch (Throwable e) {
            logger.warn("Cannot register the JMX monitor for the InstanceRegistry :", e);
        }
    }
    
    private void scheduleRenewalThresholdUpdateTask() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateRenewalThreshold();
            }
        }, serverConfig.getRenewalThresholdUpdateIntervalMs(),
                       serverConfig.getRenewalThresholdUpdateIntervalMs());
    }
    
    private void updateRenewalThreshold() {
        try {
            Applications apps = eurekaClient.getApplications();
            int count = 0;
            // 已经注册的服务的数量
            for (Application app : apps.getRegisteredApplications()) {
                for (InstanceInfo instance : app.getInstances()) {
                    if (this.isRegisterable(instance)) {
                        ++count;
                    }
                }
            }
            synchronized (lock) {
                // Update threshold only if the threshold is greater than the
                // current expected threshold of if the self preservation is disabled.
                // serverConfig.getRenewalPercentThreshold() = 0.85
                // 参考 AbstractInstanceRegistry.getNumOfRenewsInLastMin()
                if ((count * 2) > (serverConfig.getRenewalPercentThreshold() * numberOfRenewsPerMinThreshold)
                        || (!this.isSelfPreservationModeEnabled())) {
                    this.expectedNumberOfRenewsPerMin = count * 2;
                    this.numberOfRenewsPerMinThreshold = (int) ((count * 2) * serverConfig.getRenewalPercentThreshold());
                }
            }
            logger.info("Current renewal threshold is : {}", numberOfRenewsPerMinThreshold);
        } catch (Throwable e) {
            logger.error("Cannot update renewal threshold", e);
        }
    }
}
```

####ResponseCacheImpl

```java
public class ResponseCacheImpl implements ResponseCache {
    // 定时任务, 定时将二级缓存中的数据同步到一级缓存中(包括了删除和加载)
    private final java.util.Timer timer = new java.util.Timer("Eureka-CacheFillTimer", true);
    // 一级缓存, 本质上是 HasMap, 无过期时间, 保存服务信息的对外输出数据结构
    private final ConcurrentMap<Key, Value> readOnlyCacheMap = new ConcurrentHashMap<Key, Value>();
    // 二级缓存, 本质是 guava 的缓存, 包含失效机制, 保存服务信息的对外输出数据结构
    private final LoadingCache<Key, Value> readWriteCacheMap;
}
```

#### AbstractInstanceRegistry 

 https://blog.csdn.net/qq_27529917/article/details/80934523 

```java
public abstract class AbstractInstanceRegistry implements InstanceRegistry {
    // 数据存储层, 存储在内存中
    // 第一层 key: spring.application.name  value: ConcurrentHashMap
    // 第二层 key: 服务的 InstanceId  value: Lease 对象
    // Lease 对象中包含了服务详情和服务治理相关的属性
    private final ConcurrentHashMap<String, Map<String, Lease<InstanceInfo>>> registry
            = new ConcurrentHashMap<String, Map<String, Lease<InstanceInfo>>>();
    // 保存注册到 eureka server 中服务的基本信息, 和活动时间, 会定时清除过期或失效的服务
    // 通过增量信息来保持同步，能够极大的减少Server和Client之间的数据的传输，降低IO消耗。
    private ConcurrentLinkedQueue<RecentlyChangedItem> recentlyChangedQueue = new ConcurrentLinkedQueue<RecentlyChangedItem>();
    // 
    private Timer deltaRetentionTimer = new Timer("Eureka-DeltaRetentionTimer", true);
    // Eureka 定时执行 evict 方法任务调度器
    private Timer evictionTimer = new Timer("Eureka-EvictionTimer", true);
  	// 创建一个新的空示例
  	protected AbstractInstanceRegistry(EurekaServerConfig serverConfig, EurekaClientConfig 	clientConfig, ServerCodecs serverCodecs) {
        this.serverConfig = serverConfig;
        this.clientConfig = clientConfig;
        this.serverCodecs = serverCodecs;
        this.recentCanceledQueue = new CircularQueue<Pair<Long, String>>(1000);
        this.recentRegisteredQueue = new CircularQueue<Pair<Long, String>>(1000);
		// 计数存活的时间, 分钟为单位
        this.renewsLastMin = new MeasuredRate(1000 * 60 * 1);
		// 延迟 30 秒, 让后每个30 秒执行一次, 检查 recentlyChangedQueue
        this.deltaRetentionTimer.schedule(getDeltaRetentionTask(),
                serverConfig.getDeltaRetentionTimerIntervalInMs(),
                serverConfig.getDeltaRetentionTimerIntervalInMs());
    }
}
```

#### PeerEurekaNodes

管理 Eureka Server 生命周期

```java
@Singleton
public class PeerEurekaNodes {
	// Eureka Server 集群节点
	private volatile List<PeerEurekaNode> peerEurekaNodes = Collections.emptyList();
    // Eureka Server 服务地址
    private volatile Set<String> peerEurekaNodeUrls = Collections.emptySet();
    
    
            PeerAwareInstanceRegistry registry,
            EurekaServerConfig serverConfig,
            EurekaClientConfig clientConfig,
            ServerCodecs serverCodecs,
            ApplicationInfoManager applicationInfoManager) {
        this.registry = registry;
        this.serverConfig = serverConfig;
        this.clientConfig = clientConfig;
        this.serverCodecs = serverCodecs;
        this.applicationInfoManager = applicationInfoManager;
    }

    // 会在 Eureka Server 上下文初始化后执行 ( 7 之后)
    public void start() {
        taskExecutor = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r, "Eureka-PeerNodesUpdater");
                        thread.setDaemon(true);
                        return thread;
                    }
                }
        );
        try {
            // 初始化集群节点路径
            updatePeerEurekaNodes(resolvePeerUrls());
            // 定时任务
            Runnable peersUpdateTask = new Runnable() {
                @Override
                public void run() {
                    try {
                        // 为了方便调试 
                        // 在本地配置文件中添加下面的配置项, 30秒后执行, 系统默认是十分钟
                        // eureka.server.peer-eureka-nodes-update-interval-ms: 30000
                        updatePeerEurekaNodes(resolvePeerUrls());
                    } catch (Throwable e) {
                        logger.error("Cannot update the replica Nodes", e);
                    }

                }
            };
            
            // 延迟十分钟后执行 peersUpdateTask, 十分钟后执行下个任务
            taskExecutor.scheduleWithFixedDelay(
                    peersUpdateTask,
                    serverConfig.getPeerEurekaNodesUpdateIntervalMs(),
                    serverConfig.getPeerEurekaNodesUpdateIntervalMs(),
                    TimeUnit.MILLISECONDS
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        for (PeerEurekaNode node : peerEurekaNodes) {
            logger.info("Replica node URL:  {}", node.getServiceUrl());
        }
    }
    
    // 解析配置文件中配置的集群的路径
    protected List<String> resolvePeerUrls() {
        // 本身基本信息
        InstanceInfo myInfo = applicationInfoManager.getInfo();
        String zone = InstanceInfo.getZone(clientConfig.getAvailabilityZones(clientConfig.getRegion()), myInfo);
        // 当配置文件中配置了集群配置, 解析获取其他节点的地址
        List<String> replicaUrls = EndpointUtils
                .getDiscoveryServiceUrls(clientConfig, zone, new EndpointUtils.InstanceInfoBasedUrlRandomizer(myInfo));

        int idx = 0;
        while (idx < replicaUrls.size()) {
            if (isThisMyUrl(replicaUrls.get(idx))) {
                replicaUrls.remove(idx);
            } else {
                idx++;
            }
        }
        return replicaUrls;
    }
    
    // 更新其他节点的信息
    protected void updatePeerEurekaNodes(List<String> newPeerUrls) {
        if (newPeerUrls.isEmpty()) {
            logger.warn("The replica size seems to be empty. Check the route 53 DNS Registry");
            return;
        }

        Set<String> toShutdown = new HashSet<>(peerEurekaNodeUrls);
        // 失效节点
        toShutdown.removeAll(newPeerUrls);
        Set<String> toAdd = new HashSet<>(newPeerUrls);
        // 新增节点
        toAdd.removeAll(peerEurekaNodeUrls);

        // 说明集群节点没有改变
        if (toShutdown.isEmpty() && toAdd.isEmpty()) { // No change
            return;
        }

        // Remove peers no long available 
        List<PeerEurekaNode> newNodeList = new ArrayList<>(peerEurekaNodes);

        // 移除已经失效的节点
        if (!toShutdown.isEmpty()) {
            logger.info("Removing no longer available peer nodes {}", toShutdown);
            int i = 0;
            while (i < newNodeList.size()) {
                PeerEurekaNode eurekaNode = newNodeList.get(i);
                if (toShutdown.contains(eurekaNode.getServiceUrl())) {
                    newNodeList.remove(i);
                    eurekaNode.shutDown();
                } else {
                    i++;
                }
            }
        }

        // Add new peers
        if (!toAdd.isEmpty()) {
            logger.info("Adding new peer nodes {}", toAdd);
            for (String peerUrl : toAdd) {
                newNodeList.add(createPeerEurekaNode(peerUrl));
            }
        }

        this.peerEurekaNodes = newNodeList;
        this.peerEurekaNodeUrls = new HashSet<>(newPeerUrls);
    }
    
    protected PeerEurekaNode createPeerEurekaNode(String peerEurekaNodeUrl) {
        HttpReplicationClient replicationClient = JerseyReplicationClient.createReplicationClient(serverConfig, serverCodecs, peerEurekaNodeUrl);
        String targetHost = hostFromUrl(peerEurekaNodeUrl);
        if (targetHost == null) {
            targetHost = "host";
        }
        return new PeerEurekaNode(registry, targetHost, peerEurekaNodeUrl, replicationClient, serverConfig);
    }
    
    
}
```

#### DefaultEurekaServerContext

```java
/**
	Eureka 上下文
 */
public class DefaultEurekaServerContext implements EurekaServerContext {
	public DefaultEurekaServerContext(EurekaServerConfig serverConfig,
                               ServerCodecs serverCodecs,
                               PeerAwareInstanceRegistry registry,
                               PeerEurekaNodes peerEurekaNodes,
                               ApplicationInfoManager applicationInfoManager) {
        this.serverConfig = serverConfig;
        this.serverCodecs = serverCodecs;
        this.registry = registry;
        this.peerEurekaNodes = peerEurekaNodes;
        this.applicationInfoManager = applicationInfoManager;
    }

    // 在执行构造方法后执行
    @PostConstruct
    @Override
    public void initialize() {
        logger.info("Initializing ...");
        // 执行 6 PeerEurekaNodes 初始化
        peerEurekaNodes.start();
        try {
            registry.init(peerEurekaNodes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        logger.info("Initialized");
    }
}
```

#### EurekaServerInitializerConfiguration

配置类, 用于初始化 Eureka 容器

```java
@Configurationpublic class EurekaServerInitializerConfiguration      implements ServletContextAware, SmartLifecycle, Ordered {
    public void start() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					//TODO: is this class even needed now?
			eurekaServerBootstrap.contextInitialized(EurekaServerInitializerConfiguration.this.servletContext);
					log.info("Started Eureka Server");

					publish(new EurekaRegistryAvailableEvent(getEurekaServerConfig()));
					EurekaServerInitializerConfiguration.this.running = true;
					publish(new EurekaServerStartedEvent(getEurekaServerConfig()));
				}
				catch (Exception ex) {
					// Help!
					log.error("Could not initialize Eureka servlet context", ex);
				}
			}
		}).start();
	}
}
```

#### CloudEurekaClient extends DiscoveryClient 

