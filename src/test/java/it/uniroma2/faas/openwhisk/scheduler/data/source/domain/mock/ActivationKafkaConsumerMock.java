package it.uniroma2.faas.openwhisk.scheduler.data.source.domain.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.uniroma2.faas.openwhisk.scheduler.data.source.domain.model.*;
import it.uniroma2.faas.openwhisk.scheduler.data.source.remote.consumer.kafka.AbstractKafkaConsumer;
import it.uniroma2.faas.openwhisk.scheduler.data.source.remote.consumer.kafka.ConsumableKafkaConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ActivationKafkaConsumerMock<T extends IConsumable> extends AbstractKafkaConsumer<T> {

    private final static Logger LOG = LogManager.getLogger(ConsumableKafkaConsumer.class.getCanonicalName());

    private final Random random = new Random();
    private final String recordOnlyTarget = "{\"action\":{\"name\":\"hello_py\",\"path\":\"guest\",\"version\":\"0.0.1\"},\"activationId\":\"%s\",\"blocking\":true,\"content\":{\"kTest\":\"vTest\",\"$scheduler\":{\"target\":\"invoker0\",\"priority\":%d,\"limits\":{\"concurrency\":8,\"memory\":128,\"timeout\":60000,\"userMemory\":2147483648}}},\"initArgs\":[],\"lockedArgs\":{},\"revision\":\"1-ff07dcb3291545090f86e9fc1a01b5bf\",\"rootControllerIndex\":{\"asString\":\"0\",\"instanceType\":\"controller\"},\"transid\":[\"4K6K9Uoz09O5ut42VEiFI9zrOAiJX8oq\",1611192819095],\"user\":{\"authkey\":{\"api_key\":\"23bc46b1-71f6-4ed5-8c54-816aa4f8c502:123zO3xZCLrMN6v2BKK1dXYFpXlPkccOFqm12CdAsMgRU4VrNZ9lyGVCGuMDGIwP\"},\"limits\":{},\"namespace\":{\"name\":\"guest\",\"uuid\":\"23bc46b1-71f6-4ed5-8c54-816aa4f8c502\"},\"rights\":[\"READ\",\"PUT\",\"DELETE\",\"ACTIVATE\"],\"subject\":\"guest\"}}";
    private final String recordWithCause = "{\"action\":{\"name\":\"fn1\",\"path\":\"guest\",\"version\":\"0.0.2\"},\"activationId\":\"%s\",\"blocking\":true,\"cause\":\"%s\",\"content\":{\"sleep_time\":15,\"user\":\"Kira\",\"$scheduler\":{\"target\":\"invoker0\",\"priority\":%d,\"limits\":{\"concurrency\":8,\"memory\":128,\"timeout\":60000,\"userMemory\":2147483648}}},\"initArgs\":[],\"revision\":\"2-2bd48aaaf6e1721bca963e680eee313e\",\"rootControllerIndex\":{\"asString\":\"0\",\"instanceType\":\"controller\"},\"transid\":[\"bxytKe9Qk3EYVEYQnpHs02NDFo5eKAd3\",1609870677855,[\"2rkHZR02ErZB6kol4tl5LN4oxiHB5VH8\",1609870676410,[\"iRFErOK5Hflz8qduy49vBKWWMFTG44IW\",1609870676273]]],\"user\":{\"authkey\":{\"api_key\":\"23bc46b1-71f6-4ed5-8c54-816aa4f8c502:123zO3xZCLrMN6v2BKK1dXYFpXlPkccOFqm12CdAsMgRU4VrNZ9lyGVCGuMDGIwP\"},\"limits\":{},\"namespace\":{\"name\":\"guest\",\"uuid\":\"23bc46b1-71f6-4ed5-8c54-816aa4f8c502\"},\"rights\":[\"READ\",\"PUT\",\"DELETE\",\"ACTIVATE\"],\"subject\":\"guest\"}}";

    protected final ObjectMapper objectMapper;
    protected final int pollingIntervalMs;

    public ActivationKafkaConsumerMock(@Nonnull List<String> topics, Properties kafkaProperties) {
        this(topics, kafkaProperties, 500);
    }

    public ActivationKafkaConsumerMock(@Nonnull List<String> topics, Properties kafkaProperties, int pollingIntervalMs) {
        super(topics, kafkaProperties);
        this.pollingIntervalMs = pollingIntervalMs;
        this.objectMapper = new ObjectMapper();
    }

    // to stream type String
//    public static final String SUCCESS_STREAM = "success_stream";
//    public static final String FAILURE_STREAM = "failure_stream";
    // to stream type ISubject.IStream
//    public static final class SUCCESS_STREAM implements IStream {
//        public static final SUCCESS_STREAM INSTANCE = new SUCCESS_STREAM();
//        private SUCCESS_STREAM() {}
//    }
//    public static final class FAILURE_STREAM implements IStream {
//        public static final FAILURE_STREAM INSTANCE = new FAILURE_STREAM();
//        private FAILURE_STREAM() {}
//    }
    // to stream type UUID
    public static final UUID SUCCESS_STREAM = UUID.randomUUID();
    public static final UUID FAILURE_STREAM = UUID.randomUUID();
    public static final UUID ACTIVATION_STREAM = UUID.randomUUID();

    /*@Override
    public @Nonnull <S> Map<UUID, Collection<S>> streamsPartition(@Nonnull final Collection<S> data) {
        List<S> coll = new ArrayList<>(data);
        Collection<S> firstHalf = new ArrayList<>();
        Collection<S> secondHalf = new ArrayList<>();
        for (int i = 0; i < coll.size() / 2; ++i) {
            firstHalf.add(coll.get(i));
        }
        for (int i = coll.size() / 2; i < coll.size() - 1; ++i) {
            secondHalf.add(coll.get(i));
        }
        Map<UUID, Collection<S>> map = new HashMap<>() {{
            put(SUCCESS_STREAM, firstHalf);
            put(FAILURE_STREAM, secondHalf);
        }};

        System.out.println(map.toString());

        return map;
    }*/

    @Override
    public @Nonnull <S> Map<UUID, Collection<S>> streamsPartition(@Nonnull final Collection<S> data) {
        return new HashMap<>(1) {{
            put(ACTIVATION_STREAM, data);
        }};
    }

    /**
     * It is assumed that only one thread per instance calls this method.
     * @return
     */
    @Override
    public @Nullable Collection<T> consume() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final Collection<T> data = new ArrayDeque<>(10);
        for (int i = 0; i < 10; ++i) {
            try {
                T activation = (T) objectMapper.readValue(String.format(recordOnlyTarget,
                        UUID.randomUUID(),
                        random.ints(0, 6).findFirst().getAsInt()),
                        Activation.class);
                data.add(activation);
                T activationWithCause = (T) objectMapper.readValue(String.format(recordWithCause,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        random.ints(0, 6).findFirst().getAsInt()),
                        Activation.class);
                data.add(activationWithCause);
            } catch (JsonProcessingException e) {
                LOG.warn("Exception parsing Activation from record: ");
            }
        }
        for (int i = 0; i < 5; ++i) {
            data.add((T) new Health(new Instance(0, Instance.Type.CONTROLLER, "evbrg", "222222 B")));
            data.add((T) new Event(null, "frev", "rgbr", "vrgbr", "brynth", 45464L, "htunt"));
        }
        LOG.trace("Sending {} consumable to observers.", data.size());
        return data;
    }

}