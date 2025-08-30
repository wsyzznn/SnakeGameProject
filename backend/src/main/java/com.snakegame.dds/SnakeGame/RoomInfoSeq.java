package com.snakegame.dds.SnakeGame;

import com.zrdds.infrastructure.ZRSequence;

public class RoomInfoSeq extends ZRSequence<RoomInfo> {

    protected Object[] alloc_element(int length) {
        RoomInfo[] result = new RoomInfo[length];
        for (int i = 0; i < result.length; ++i) {
             result[i] = new RoomInfo();
        }
        return result;
    }

    protected Object copy_from_element(Object dstEle, Object srcEle){
        RoomInfo typedDst = (RoomInfo)dstEle;
        RoomInfo typedSrc = (RoomInfo)srcEle;
        return typedDst.copy(typedSrc);
    }

    public void pull_from_nativeI(long nativeSeq){

    }

    public void push_to_nativeI(long nativeSeq){

    }
}