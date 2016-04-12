package io.advantageous.consul.domain;

import io.advantageous.boon.json.annotations.JsonProperty;

import java.util.List;

/**
 * https://www.consul.io/docs/agent/http/session.html#session_create
 * {
 "LockDelay": "15s",
 "Name": "my-service-lock",
 "Node": "foobar",
 "Checks": ["a", "b", "c"],
 "Behavior": "release",
 "TTL": "0s"
 }
 */
public class Session {

    /**
     * LockDelay can be specified as a duration string using a "s" suffix for seconds. The default is 15s.
     */
    @JsonProperty("LockDelay")
    private String lockDelay;

    /**
     * Name can be used to provide a human-readable name for the Session.
     */
    @JsonProperty("Name")
    private String name;


    /**
     * Id for Session
     */
    @JsonProperty("Id")
    private String id;

    /**
     * Node must refer to a node that is already registered, if specified.
     * By default, the agent's own node name is used.
     */
    @JsonProperty("Node")
    private String node;

    /**
     * Checks is used to provide a list of associated health checks. It is highly recommended that,
     * if you override this list, you include the default "serfHealth".
     */
    @JsonProperty("Checks")
    private List<String> checks;


    /**
     * Behavior can be set to either release or delete. This controls the behavior when a session is invalidated.
     * By default, this is release, causing any locks that are held to be released.
     * Changing this to delete causes any locks that are held to be deleted. delete is useful for
     * creating ephemeral key/value entries.
     */
    @JsonProperty("Behavior")
    private String behavior;

    /**
     * The TTL field is a duration string, and like LockDelay it can use "s" as a suffix for seconds.
     * If specified, it must be between 10s and 86400s currently. When provided, the session is invalidated
     * if it is not renewed before the TTL expires. The lowest practical TTL should be used to keep the number
     * of managed sessions low. When locks are forcibly expired, such as during a leader election, sessions may
     * not be reaped for up to double this TTL, so long TTL values (>1 hour) should be avoided.
     * See the session internals page for more documentation of this feature.
     */
    @JsonProperty("TTL")
    private String ttl;


    @JsonProperty("ModifyIndex")
    private long modifyIndex;



    @JsonProperty("CreateIndex")
    private long createIndex;

    public String getLockDelay() {
        return lockDelay;
    }


    public String getName() {
        return name;
    }

    public Session setName(String name) {
        this.name = name;
        return this;
    }

    public String getNode() {
        return node;
    }

    public Session setNode(String node) {
        this.node = node;
        return this;
    }

    public List<String> getChecks() {
        return checks;
    }

    public Session setChecks(List<String> checks) {
        this.checks = checks;
        return this;
    }

    public String getBehavior() {
        return behavior;
    }

    public Session setSessionBehavior(SessionBehavior behavior) {
        this.behavior = behavior.name().toLowerCase();
        return this;
    }

    public String getTtl() {
        return ttl;
    }

    public Session setTtlSeconds(long ttl) {
        this.ttl = "" + ttl + "s";
        return this;
    }


    public Session setTtlMinutes(long ttl) {
        this.ttl = "" + ttl + "m";
        return this;
    }

    public Session setTtlHours(long ttl) {
        this.ttl = "" + ttl + "h";
        return this;
    }


    public Session setLockDelaySeconds(long lockDelay) {
        this.lockDelay = "" + lockDelay + "s";
        return this;
    }

    public Session setLockDelayMinutes(long lockDelay) {
        this.lockDelay = "" + lockDelay + "m";
        return this;
    }

    public long getCreateIndex() {
        return createIndex;
    }

    public Session setCreateIndex(long createIndex) {
        this.createIndex = createIndex;
        return this;
    }

    public String getId() {
        return id;
    }

    public Session setId(String id) {
        this.id = id;
        return this;
    }

    public long getModifyIndex() {
        return modifyIndex;
    }

    public Session setModifyIndex(long modifyIndex) {
        this.modifyIndex = modifyIndex;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Session session = (Session) o;

        if (createIndex != session.createIndex) return false;
        if (lockDelay != null ? !lockDelay.equals(session.lockDelay) : session.lockDelay != null) return false;
        if (name != null ? !name.equals(session.name) : session.name != null) return false;
        if (id != null ? !id.equals(session.id) : session.id != null) return false;
        if (node != null ? !node.equals(session.node) : session.node != null) return false;
        if (checks != null ? !checks.equals(session.checks) : session.checks != null) return false;
        if (behavior != null ? !behavior.equals(session.behavior) : session.behavior != null) return false;
        return ttl != null ? ttl.equals(session.ttl) : session.ttl == null;

    }

    @Override
    public int hashCode() {
        int result = lockDelay != null ? lockDelay.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (node != null ? node.hashCode() : 0);
        result = 31 * result + (checks != null ? checks.hashCode() : 0);
        result = 31 * result + (behavior != null ? behavior.hashCode() : 0);
        result = 31 * result + (ttl != null ? ttl.hashCode() : 0);
        result = 31 * result + (int) (createIndex ^ (createIndex >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Session{" +
                "lockDelay='" + lockDelay + '\'' +
                ", name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", node='" + node + '\'' +
                ", checks=" + checks +
                ", behavior='" + behavior + '\'' +
                ", ttl='" + ttl + '\'' +
                ", createIndex=" + createIndex +
                '}';
    }
}
