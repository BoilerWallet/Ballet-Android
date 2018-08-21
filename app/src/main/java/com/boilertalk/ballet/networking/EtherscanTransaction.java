package com.boilertalk.ballet.networking;

public class EtherscanTransaction {
    public String srcAddr;
    public String dstAddr;
    public long value;
    public long blockNumber;
    public long timestamp;
    public String txHash;

    public EtherscanTransaction(long blockNumber, String srcAddr, String dstAddr, long value,
                                long timestamp, String txHash) {
        this.blockNumber = blockNumber;
        this.srcAddr = srcAddr;
        this.dstAddr = dstAddr;
        this.value = value;
        this.timestamp = timestamp;
        this.txHash = txHash;
    }

    @Override
    public boolean equals(Object o) {
        boolean same = false;

        if(o instanceof EtherscanTransaction) {
            EtherscanTransaction e = (EtherscanTransaction) o;
            same = (this.txHash.equals(e.txHash));
        }

        return same;
    }

    @Override
    public int hashCode() {
        return (txHash == null) ? 0 : txHash.hashCode();
    }
}
