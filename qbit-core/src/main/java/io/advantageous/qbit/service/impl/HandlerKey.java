package io.advantageous.qbit.service.impl;

/**
 * Maps an incoming call to a response handler.
 * This uniquely identifies a method call based on its message id and return address combo.
 * We use this as a key into the
 */
class HandlerKey {
    final String returnAddress;
    final long messageId;

    HandlerKey(String returnAddress, long messageId) {
        this.returnAddress = returnAddress;
        this.messageId = messageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final HandlerKey that = (HandlerKey) o;
        return messageId == that.messageId
                && !(returnAddress != null
                ? !returnAddress.equals(that.returnAddress)
                : that.returnAddress != null);
    }

    @Override
    public int hashCode() {
        int result = returnAddress != null ? returnAddress.hashCode() : 0;
        result = 31 * result + (int) (messageId ^ (messageId >>> 32));
        return result;
    }
}
