package com.boilertalk.ballet.networking;

public class EtherscanTransaction {
    public String srcAddr;
    public String dstAddr;
    public long value;
    public long blockNumber;
    public long timestamp;

    public EtherscanTransaction(long blockNumber, String srcAddr, String dstAddr, long value,
                                long timestamp) {
        this.blockNumber = blockNumber;
        this.srcAddr = srcAddr;
        this.dstAddr = dstAddr;
        this.value = value;
        this.timestamp = timestamp;
    }
}
