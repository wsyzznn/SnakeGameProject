package com.snakegame.dds.SnakeGame;

import com.zrdds.infrastructure.ZRSequence;

public class PlayerInputSeq extends ZRSequence<PlayerInput> {

    protected Object[] alloc_element(int length) {
        PlayerInput[] result = new PlayerInput[length];
        for (int i = 0; i < result.length; ++i) {
             result[i] = new PlayerInput();
        }
        return result;
    }

    protected Object copy_from_element(Object dstEle, Object srcEle){
        PlayerInput typedDst = (PlayerInput)dstEle;
        PlayerInput typedSrc = (PlayerInput)srcEle;
        return typedDst.copy(typedSrc);
    }

    public void pull_from_nativeI(long nativeSeq){

    }

    public void push_to_nativeI(long nativeSeq){

    }
}