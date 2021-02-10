package it.uniroma2.faas.openwhisk.scheduler.data.source.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.uniroma2.faas.openwhisk.scheduler.scheduler.domain.model.Activation;

public class ActivationTest {

    public static void readWriteActivation() {
        String actvWithNullTrans = "{\"action\":{\"name\":\"fn2\",\"path\":\"guest\",\"version\":\"0.0.1\"},\"activationId\":\"aac24f71a5464d95824f71a546fd953f\",\"blocking\":true,\"cause\":\"7902f85843f349df82f85843f309dfb0\",\"content\":{\"extras\":{\"cmd\":\"Sleep executed for 15 s.\",\"fn_name\":\"/guest/fn1\"},\"message\":\"Hello Kira!\",\"sleep_time\":15},\"initArgs\":[],\"revision\":\"1-4fa71c318cc896439a823a243edb9d41\",\"rootControllerIndex\":{\"asString\":\"0\",\"instanceType\":\"controller\"},\"user\":{\"authkey\":{\"api_key\":\"23bc46b1-71f6-4ed5-8c54-816aa4f8c502:123zO3xZCLrMN6v2BKK1dXYFpXlPkccOFqm12CdAsMgRU4VrNZ9lyGVCGuMDGIwP\"},\"limits\":{},\"namespace\":{\"name\":\"guest\",\"uuid\":\"23bc46b1-71f6-4ed5-8c54-816aa4f8c502\"},\"rights\":[\"READ\",\"PUT\",\"DELETE\",\"ACTIVATE\"],\"subject\":\"guest\"}}";
        String actvWithZeroTrans = "{\"action\":{\"name\":\"fn2\",\"path\":\"guest\",\"version\":\"0.0.1\"},\"activationId\":\"aac24f71a5464d95824f71a546fd953f\",\"blocking\":true,\"cause\":\"7902f85843f349df82f85843f309dfb0\",\"content\":{\"extras\":{\"cmd\":\"Sleep executed for 15 s.\",\"fn_name\":\"/guest/fn1\"},\"message\":\"Hello Kira!\",\"sleep_time\":15},\"initArgs\":[],\"revision\":\"1-4fa71c318cc896439a823a243edb9d41\",\"rootControllerIndex\":{\"asString\":\"0\",\"instanceType\":\"controller\"},\"transid\":[],\"user\":{\"authkey\":{\"api_key\":\"23bc46b1-71f6-4ed5-8c54-816aa4f8c502:123zO3xZCLrMN6v2BKK1dXYFpXlPkccOFqm12CdAsMgRU4VrNZ9lyGVCGuMDGIwP\"},\"limits\":{},\"namespace\":{\"name\":\"guest\",\"uuid\":\"23bc46b1-71f6-4ed5-8c54-816aa4f8c502\"},\"rights\":[\"READ\",\"PUT\",\"DELETE\",\"ACTIVATE\"],\"subject\":\"guest\"}}";
        String actvWithOneTrans = "{\"action\":{\"name\":\"fn2\",\"path\":\"guest\",\"version\":\"0.0.1\"},\"activationId\":\"aac24f71a5464d95824f71a546fd953f\",\"blocking\":true,\"cause\":\"7902f85843f349df82f85843f309dfb0\",\"content\":{\"extras\":{\"cmd\":\"Sleep executed for 15 s.\",\"fn_name\":\"/guest/fn1\"},\"message\":\"Hello Kira!\",\"sleep_time\":15},\"initArgs\":[],\"revision\":\"1-4fa71c318cc896439a823a243edb9d41\",\"rootControllerIndex\":{\"asString\":\"0\",\"instanceType\":\"controller\"},\"transid\":[\"gNq0c7Gd9aNQgleFP6Q0wNpzPtNDYUxG\",1609870696549],\"user\":{\"authkey\":{\"api_key\":\"23bc46b1-71f6-4ed5-8c54-816aa4f8c502:123zO3xZCLrMN6v2BKK1dXYFpXlPkccOFqm12CdAsMgRU4VrNZ9lyGVCGuMDGIwP\"},\"limits\":{},\"namespace\":{\"name\":\"guest\",\"uuid\":\"23bc46b1-71f6-4ed5-8c54-816aa4f8c502\"},\"rights\":[\"READ\",\"PUT\",\"DELETE\",\"ACTIVATE\"],\"subject\":\"guest\"}}";
        String actvWithFourTrans = "{\"action\":{\"name\":\"fn2\",\"path\":\"guest\",\"version\":\"0.0.1\"},\"activationId\":\"aac24f71a5464d95824f71a546fd953f\",\"blocking\":true,\"cause\":\"7902f85843f349df82f85843f309dfb0\",\"content\":{\"extras\":{\"cmd\":\"Sleep executed for 15 s.\",\"fn_name\":\"/guest/fn1\"},\"message\":\"Hello Kira!\",\"sleep_time\":15},\"initArgs\":[],\"revision\":\"1-4fa71c318cc896439a823a243edb9d41\",\"rootControllerIndex\":{\"asString\":\"0\",\"instanceType\":\"controller\"},\"transid\":[\"gNq0c7Gd9aNQgleFP6Q0wNpzPtNDYUxG\",1609870696549,[\"VLZT89oIjCpXIsRyW4fo0nKIakSiAbEn\",1609870696415,[\"bxytKe9Qk3EYVEYQnpHs02NDFo5eKAd3\",1609870677855,[\"2rkHZR02ErZB6kol4tl5LN4oxiHB5VH8\",1609870676410,[\"iRFErOK5Hflz8qduy49vBKWWMFTG44IW\",1609870676273]]]]],\"user\":{\"authkey\":{\"api_key\":\"23bc46b1-71f6-4ed5-8c54-816aa4f8c502:123zO3xZCLrMN6v2BKK1dXYFpXlPkccOFqm12CdAsMgRU4VrNZ9lyGVCGuMDGIwP\"},\"limits\":{},\"namespace\":{\"name\":\"guest\",\"uuid\":\"23bc46b1-71f6-4ed5-8c54-816aa4f8c502\"},\"rights\":[\"READ\",\"PUT\",\"DELETE\",\"ACTIVATE\"],\"subject\":\"guest\"}}";

        String actvHealth = "{\"action\":{\"name\":\"invokerHealthTestAction0\",\"path\":\"whisk.system\",\"version\":\"0.0.1\"},\"activationId\":\"ebc2a7c4501044be82a7c4501054bed9\",\"blocking\":false,\"content\":{\"$scheduler\":{\"target\":\"invoker0\"}},\"initArgs\":[],\"lockedArgs\":{},\"revision\":null,\"rootControllerIndex\":{\"asString\":\"0\",\"instanceType\":\"controller\"},\"transid\":[\"sid_invokerHealth\",1612976091340],\"user\":{\"authkey\":{\"api_key\":\"d0feeca8-6f8f-48c3-beec-a86f8fc8c3a5:nJwMmhFtHKWymXGFjWsIj12DDnrliJ4VWjLLGvsnsUwj1I4C1XYCbZ2cijFFF3FW\"},\"limits\":{},\"namespace\":{\"name\":\"whisk.system\",\"uuid\":\"d0feeca8-6f8f-48c3-beec-a86f8fc8c3a5\"},\"rights\":[],\"subject\":\"whisk.system\"}}";

        try {
            String string = actvHealth;

            System.out.println(string);
            Activation activation = new ObjectMapper().readValue(string, Activation.class);
            System.out.println(activation);
            System.out.println(new ObjectMapper().writeValueAsString(activation));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public static void readActivationTest() {
        String record = "{\"action\":{\"name\":\"test_annotations\",\"path\":\"guest\",\"version\":\"0.0.1\"},\"activationId\":\"0b307ee5a1304a57b07ee5a1300a57e7\",\"blocking\":true,\"content\":{\"key0\":\"value0\",\"key1\":\"value1\",\"key2\":\"value2\"},\"initArgs\":[],\"revision\":\"3-b3eeb1e516fd89366574c6051f024fa7\",\"rootControllerIndex\":{\"asString\":\"0\",\"instanceType\":\"controller\"},\"transid\":[\"rxseFbQm3cy0QYASqVGkujB8lxEDE5Mu\",1609810319398],\"user\":{\"authkey\":{\"api_key\":\"23bc46b1-71f6-4ed5-8c54-816aa4f8c502:123zO3xZCLrMN6v2BKK1dXYFpXlPkccOFqm12CdAsMgRU4VrNZ9lyGVCGuMDGIwP\"},\"limits\":{},\"namespace\":{\"name\":\"guest\",\"uuid\":\"23bc46b1-71f6-4ed5-8c54-816aa4f8c502\"},\"rights\":[\"READ\",\"PUT\",\"DELETE\",\"ACTIVATE\"],\"subject\":\"guest\"}}";
        String recordPriority = "{\"action\":{\"name\":\"test_annotations\",\"path\":\"guest\",\"version\":\"0.0.1\"},\"activationId\":\"0b307ee5a1304a57b07ee5a1300a57e7\",\"blocking\":true,\"content\":{\"key0\":\"value0\",\"key1\":\"value1\",\"key2\":\"value2\",\"$scheduler\":{\"target\":\"invoker2\"}},\"initArgs\":[],\"revision\":\"3-b3eeb1e516fd89366574c6051f024fa7\",\"rootControllerIndex\":{\"asString\":\"0\",\"instanceType\":\"controller\"},\"transid\":[\"rxseFbQm3cy0QYASqVGkujB8lxEDE5Mu\",1609810319398],\"user\":{\"authkey\":{\"api_key\":\"23bc46b1-71f6-4ed5-8c54-816aa4f8c502:123zO3xZCLrMN6v2BKK1dXYFpXlPkccOFqm12CdAsMgRU4VrNZ9lyGVCGuMDGIwP\"},\"limits\":{},\"namespace\":{\"name\":\"guest\",\"uuid\":\"23bc46b1-71f6-4ed5-8c54-816aa4f8c502\"},\"rights\":[\"READ\",\"PUT\",\"DELETE\",\"ACTIVATE\"],\"subject\":\"guest\"}}";
        String recordToTest = "{\"action\":{\"name\":\"invokerHealthTestAction0\",\"path\":\"whisk.system\",\"version\":\"0.0.1\"},\"activationId\":\"c87969bfe8cf4c2bb969bfe8cf3c2bf0\",\"blocking\":false,\"content\":{\"$scheduler\":{\"target\":\"invoker0\"}},\"initArgs\":[],\"lockedArgs\":{},\"revision\":null,\"rootControllerIndex\":{\"asString\":\"0\",\"instanceType\":\"controller\"},\"transid\":[\"sid_invokerHealth\",1611036640735],\"user\":{\"authkey\":{\"api_key\":\"979552f2-6935-40aa-9552-f2693520aa71:nH3V9vtkRxy8Hcw1a88xHd7jotqefxjH5jTf8v5e1qnWXerfR67vvZyrj3EX9vu4\"},\"limits\":{},\"namespace\":{\"name\":\"whisk.system\",\"uuid\":\"979552f2-6935-40aa-9552-f2693520aa71\"},\"rights\":[],\"subject\":\"whisk.system\"}}";
        String testHealthRecord = "{\"action\":{\"name\":\"invokerHealthTestAction0\",\"path\":\"whisk.system\",\"version\":\"0.0.1\"},\"activationId\":\"568a98b8da0548598a98b8da058859df\",\"blocking\":false,\"initArgs\":[],\"lockedArgs\":{},\"revision\":null,\"rootControllerIndex\":{\"asString\":\"0\",\"instanceType\":\"controller\"},\"transid\":[\"sid_invokerHealth\",1612140411000],\"user\":{\"authkey\":{\"api_key\":\"07784fcd-9cb3-400a-b84f-cd9cb3f00a97:mMHbHXq0fXI3oTI5xQ5c9TtlayzGtxeXU6AEqYcBuZFzJWGUyrarMG8es68NANBW\"},\"limits\":{},\"namespace\":{\"name\":\"whisk.system\",\"uuid\":\"07784fcd-9cb3-400a-b84f-cd9cb3f00a97\"},\"rights\":[],\"subject\":\"whisk.system\"}}";
        try {
            Activation activation = new ObjectMapper().readValue(recordPriority, Activation.class);
            System.out.println(activation);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public static void writeActivationTest() {
        String record = "{\"action\":{\"name\":\"test_annotations\",\"path\":\"guest\",\"version\":\"0.0.1\"},\"activationId\":\"0b307ee5a1304a57b07ee5a1300a57e7\",\"blocking\":true,\"content\":{\"key0\":\"value0\",\"key1\":\"value1\",\"key2\":\"value2\"},\"initArgs\":[],\"revision\":\"3-b3eeb1e516fd89366574c6051f024fa7\",\"rootControllerIndex\":{\"asString\":\"0\",\"instanceType\":\"controller\"},\"transid\":[\"rxseFbQm3cy0QYASqVGkujB8lxEDE5Mu\",1609810319398],\"user\":{\"authkey\":{\"api_key\":\"23bc46b1-71f6-4ed5-8c54-816aa4f8c502:123zO3xZCLrMN6v2BKK1dXYFpXlPkccOFqm12CdAsMgRU4VrNZ9lyGVCGuMDGIwP\"},\"limits\":{},\"namespace\":{\"name\":\"guest\",\"uuid\":\"23bc46b1-71f6-4ed5-8c54-816aa4f8c502\"},\"rights\":[\"READ\",\"PUT\",\"DELETE\",\"ACTIVATE\"],\"subject\":\"guest\"}}";
        String recordNullContent = "{\"action\":{\"name\":\"test_annotations\",\"path\":\"guest\",\"version\":\"0.0.1\"},\"activationId\":\"0b307ee5a1304a57b07ee5a1300a57e7\",\"blocking\":true,\"initArgs\":[],\"revision\":\"3-b3eeb1e516fd89366574c6051f024fa7\",\"rootControllerIndex\":{\"asString\":\"0\",\"instanceType\":\"controller\"},\"transid\":[\"rxseFbQm3cy0QYASqVGkujB8lxEDE5Mu\",1609810319398],\"user\":{\"authkey\":{\"api_key\":\"23bc46b1-71f6-4ed5-8c54-816aa4f8c502:123zO3xZCLrMN6v2BKK1dXYFpXlPkccOFqm12CdAsMgRU4VrNZ9lyGVCGuMDGIwP\"},\"limits\":{},\"namespace\":{\"name\":\"guest\",\"uuid\":\"23bc46b1-71f6-4ed5-8c54-816aa4f8c502\"},\"rights\":[\"READ\",\"PUT\",\"DELETE\",\"ACTIVATE\"],\"subject\":\"guest\"}}";
        String recordPriority = "{\"action\":{\"name\":\"test_annotations\",\"path\":\"guest\",\"version\":\"0.0.1\"},\"activationId\":\"0b307ee5a1304a57b07ee5a1300a57e7\",\"blocking\":true,\"content\":{\"key0\":\"value0\",\"key1\":\"value1\",\"key2\":\"value2\",\"scheduler\":{\"target\":\"invoker2\"}},\"initArgs\":[],\"revision\":\"3-b3eeb1e516fd89366574c6051f024fa7\",\"rootControllerIndex\":{\"asString\":\"0\",\"instanceType\":\"controller\"},\"transid\":[\"rxseFbQm3cy0QYASqVGkujB8lxEDE5Mu\",1609810319398],\"user\":{\"authkey\":{\"api_key\":\"23bc46b1-71f6-4ed5-8c54-816aa4f8c502:123zO3xZCLrMN6v2BKK1dXYFpXlPkccOFqm12CdAsMgRU4VrNZ9lyGVCGuMDGIwP\"},\"limits\":{},\"namespace\":{\"name\":\"guest\",\"uuid\":\"23bc46b1-71f6-4ed5-8c54-816aa4f8c502\"},\"rights\":[\"READ\",\"PUT\",\"DELETE\",\"ACTIVATE\"],\"subject\":\"guest\"}}";
        String recordToTest = "{\"action\":{\"name\":\"invokerHealthTestAction0\",\"path\":\"whisk.system\",\"version\":\"0.0.1\"},\"activationId\":\"c87969bfe8cf4c2bb969bfe8cf3c2bf0\",\"blocking\":false,\"content\":{\"scheduler\":{\"target\":\"invoker0\"}},\"initArgs\":[],\"lockedArgs\":{},\"revision\":null,\"rootControllerIndex\":{\"asString\":\"0\",\"instanceType\":\"controller\"},\"transid\":[\"sid_invokerHealth\",1611036640735],\"user\":{\"authkey\":{\"api_key\":\"979552f2-6935-40aa-9552-f2693520aa71:nH3V9vtkRxy8Hcw1a88xHd7jotqefxjH5jTf8v5e1qnWXerfR67vvZyrj3EX9vu4\"},\"limits\":{},\"namespace\":{\"name\":\"whisk.system\",\"uuid\":\"979552f2-6935-40aa-9552-f2693520aa71\"},\"rights\":[],\"subject\":\"whisk.system\"}}";
        String recordOnlyTarget = "{\"action\":{\"name\":\"hello_py\",\"path\":\"guest\",\"version\":\"0.0.1\"},\"activationId\":\"1b319ed429234155b19ed42923a155cf\",\"blocking\":true,\"content\":{\"kTest\":\"vTest\",\"scheduler\":{\"target\":\"invoker0\",\"priority\":3}},\"initArgs\":[],\"lockedArgs\":{},\"revision\":\"1-ff07dcb3291545090f86e9fc1a01b5bf\",\"rootControllerIndex\":{\"asString\":\"0\",\"instanceType\":\"controller\"},\"transid\":[\"4K6K9Uoz09O5ut42VEiFI9zrOAiJX8oq\",1611192819095],\"user\":{\"authkey\":{\"api_key\":\"23bc46b1-71f6-4ed5-8c54-816aa4f8c502:123zO3xZCLrMN6v2BKK1dXYFpXlPkccOFqm12CdAsMgRU4VrNZ9lyGVCGuMDGIwP\"},\"limits\":{},\"namespace\":{\"name\":\"guest\",\"uuid\":\"23bc46b1-71f6-4ed5-8c54-816aa4f8c502\"},\"rights\":[\"READ\",\"PUT\",\"DELETE\",\"ACTIVATE\"],\"subject\":\"guest\"}}";
        try {
            String string = recordNullContent;
            Activation activation = new ObjectMapper().readValue(string, Activation.class);
            System.out.println(new ObjectMapper().writeValueAsString(activation));

            activation = activation.with(4);
            System.out.println(new ObjectMapper().writeValueAsString(activation));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ActivationTest.readWriteActivation();
//        ActivationTest.readActivationTest();
//        ActivationTest.writeActivationTest();
    }

}