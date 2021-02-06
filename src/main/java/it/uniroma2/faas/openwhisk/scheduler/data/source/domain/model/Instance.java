package it.uniroma2.faas.openwhisk.scheduler.data.source.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.annotation.Nonnull;

public class Instance {

    /*
    {"instance":0,"instanceType":"invoker","uniqueName":"owdev-invoker-0","userMemory":"2147483648 B"}
     */

    private final int instance;
    private final Type instanceType;
    private final String uniqueName;
    private final String userMemory;

    public enum Type {
        INVOKER,
        CONTROLLER;

        @JsonCreator
        public static @Nonnull Type from(@Nonnull String type) {
            for (Type i : Type.values()) {
                if (type.equalsIgnoreCase(i.name())) {
                    return i;
                }
            }
            throw new TypeNotPresentException(type, new Throwable("Selected type not yet implemented."));
        }

        @JsonValue
        public @Nonnull String getName() {
            return this.name().toLowerCase();
        }
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Instance(@JsonProperty("instance") int instance,
                    @JsonProperty("instanceType") Type instanceType,
                    @JsonProperty("uniqueName") String uniqueName,
                    @JsonProperty("userMemory") String userMemory) {
        this.instance = instance;
        this.instanceType = instanceType;
        this.uniqueName = uniqueName;
        this.userMemory = userMemory;
    }

    public int getInstance() {
        return instance;
    }

    public Type getInstanceType() {
        return instanceType;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public String getUserMemory() {
        return userMemory;
    }

    @Override
    public String toString() {
        return "ComponentInstance{" +
                "instance=" + instance +
                ", instanceType=" + instanceType +
                ", uniqueName='" + uniqueName + '\'' +
                ", userMemory='" + userMemory + '\'' +
                '}';
    }

}