package it.uniroma2.faas.openwhisk.scheduler.scheduler;

import it.uniroma2.faas.openwhisk.scheduler.data.source.IObserver;
import it.uniroma2.faas.openwhisk.scheduler.data.source.remote.consumer.kafka.ActivationKafkaConsumer;
import it.uniroma2.faas.openwhisk.scheduler.data.source.remote.producer.kafka.AbstractKafkaProducer;
import it.uniroma2.faas.openwhisk.scheduler.data.source.remote.producer.kafka.BaseKafkaProducer;
import it.uniroma2.faas.openwhisk.scheduler.scheduler.advanced.BufferedScheduler;
import it.uniroma2.faas.openwhisk.scheduler.scheduler.advanced.TracerScheduler;
import it.uniroma2.faas.openwhisk.scheduler.scheduler.domain.model.Config;
import it.uniroma2.faas.openwhisk.scheduler.scheduler.policy.IPolicy;
import it.uniroma2.faas.openwhisk.scheduler.scheduler.policy.Policy;
import it.uniroma2.faas.openwhisk.scheduler.scheduler.policy.PolicyFactory;
import it.uniroma2.faas.openwhisk.scheduler.util.AppExecutors;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.System.exit;

/**
 * Nota: la corrente implementazione è trasparente alla connessione/disconnessione di nuovi invoker nel sistema
 *      (questo non è vero per alcuni tipi di Scheduler che potrebbero necessitare di una modifica nei Consumer
 *       per adattarsi a tale cambiamento)
 */
public class SchedulerFacade {

    private static final Logger LOG = LogManager.getRootLogger();

    public static final String SOURCE_TOPIC = "scheduler";

    private final Config config;

    public SchedulerFacade(@Nonnull Config config) {
        checkNotNull(config, "Configuration can not be null.");
        this.config = config;
    }

    private void setLogLevel() {
        // programmatically workaround for log4j bug
        // see@ https://stackoverflow.com/questions/30120330/log4j2-unable-to-register-shutdown-hook-because-jvm-is-shutting-down#:~:text=If%20you%20get%20that%20error,actually%20be%20called%20during%20shutdown.&text=The%20exception%20is%20as%20a,LOG4J2%2D658%20in%20Apache%20Issues.
        // see@ https://stackoverflow.com/questions/30657619/programmatically-disabling-shutdown-hook-in-log4j-2
        // see@ https://stackoverflow.com/questions/17400136/how-to-log-within-shutdown-hooks-with-log4j2
        /*final LoggerContextFactory factory = LogManager.getFactory();
        if (factory instanceof Log4jContextFactory) {
            Log4jContextFactory contextFactory = (Log4jContextFactory) factory;
            ((DefaultShutdownCallbackRegistry) contextFactory.getShutdownCallbackRegistry()).stop();
        }*/
        // current implementation using shutdownHook="disable" in configuration file resources/log4j2.xml

        // see@ https://stackoverflow.com/questions/23434252/programmatically-change-log-level-in-log4j2/23846361
        Level level = Level.toLevel(config.getSysLog(), Level.INFO);
        Configurator.setAllLevels(LOG.getName(), level);
        Configurator.setRootLevel(level);

        // alternative way to change logger's level
        /*LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        Configuration configuration = loggerContext.getConfiguration();
        LoggerConfig loggerConfig = configuration.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(Level.DEBUG);
        loggerContext.updateLoggers();*/
    }

    public void start() {
        setLogLevel();

        LOG.info("v{}", config.getVersion());
        LOG.debug(config.toString());

        // create global app executors
        AppExecutors executors = new AppExecutors(
                Executors.newFixedThreadPool(2),
                null
        );

        // see@ https://stackoverflow.com/questions/51753883/increase-the-number-of-messages-read-by-a-kafka-consumer-in-a-single-poll
        // see@ https://stackoverflow.com/questions/61552431/kafka-consumer-poll-multiple-records-fetch
        // kafka consumer
        Properties kafkaConsumerProperties = new Properties() {{
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getKafkaBootstrapServers());
            put(ConsumerConfig.GROUP_ID_CONFIG, "ow-scheduler-consumer");
            put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
            put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1_000);
            // end session after 30 s
            put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30_000);
            // wait for 5 MiB of data
            put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, config.getKafkaFetchMinBytes());
            // if min bytes has not reached limit, wait for 2000 ms
            put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, config.getKafkaFetchMaxWaitMs());
            // min bytes to fetch for each partition from broker server
            put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, config.getKafkaMaxPartitionFetchBytes());
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        }};

        final ActivationKafkaConsumer activationKafkaConsumer = new ActivationKafkaConsumer(
                List.of(SOURCE_TOPIC), kafkaConsumerProperties, config.getKafkaPollTimeoutMs()
        );

        // kafka producer
        Properties kafkaProducerProperties = new Properties() {{
            put(ProducerConfig.CLIENT_ID_CONFIG, AbstractKafkaProducer.class.getSimpleName());
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getKafkaBootstrapServers());
            // wait acks only from leader
            put(ProducerConfig.ACKS_CONFIG, "1");  // 1 -> leader
            put(ProducerConfig.RETRIES_CONFIG, 0);
//            put(ProducerConfig.BATCH_SIZE_CONFIG, 16 * 1024);
            put(ProducerConfig.LINGER_MS_CONFIG, 10);
//            put(ProducerConfig.BUFFER_MEMORY_CONFIG, 100 * 1024);
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        }};
        final BaseKafkaProducer kafkaProducer = new BaseKafkaProducer(kafkaProducerProperties, null);

        // define scheduler
        IPolicy policy = PolicyFactory.createPolicy(Policy.from(config.getSchedulerPolicy()));
        LOG.trace("Scheduler policy selected: {}.", policy.getPolicy());
        Scheduler scheduler = new BaseScheduler(policy, kafkaProducer);
        LOG.trace("Creating Scheduler {}.", scheduler.getClass().getSimpleName());
        if (config.getSchedulerTracer()) {
            scheduler = new TracerScheduler(scheduler);
            LOG.trace("Adding Scheduler {}.", scheduler.getClass().getSimpleName());
        }
        if (config.getSchedulerBuffered()) {
            scheduler = new BufferedScheduler(scheduler);
            ((BufferedScheduler) scheduler).setThreshold(config.getSchedulerBufferedThreshold());
            LOG.trace("Adding Scheduler {}.", scheduler.getClass().getSimpleName());
        }

        List<IObserver> activationObservers = new ArrayList<>();
        activationObservers.add(scheduler);

        activationKafkaConsumer.register(activationObservers);

        // data source consumer list
        List<Callable<String>> dataSourceConsumers = new ArrayList<>() {{
            add(activationKafkaConsumer);
        }};

        List<Closeable> closeables = new ArrayList<>() {{
           add(activationKafkaConsumer);
           add(kafkaProducer);
        }};

        // register hook to release resources
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            closeables.forEach(closeable -> {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            executors.shutdown();
            LogManager.shutdown();
        }));

        try {
            // see@ https://stackoverflow.com/questions/20495414/thread-join-equivalent-in-executor
            // invokeAll() blocks until all tasks are completed
            executors.networkIO().invokeAll(dataSourceConsumers);
        } catch (InterruptedException e) {
            LOG.fatal("Scheduler interrupted: {}.", e.getMessage());
            exit(1);
        }

    }

}