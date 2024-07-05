package csi.server.common.util;

/**
 * Created by centrifuge on 12/2/2015.
 */
public interface CsiSimpleBuffer {

    public CsiSimpleBuffer append(String stringIn);
    public CsiSimpleBuffer appendElement(Integer valueIn);
    public CsiSimpleBuffer appendElement(int valueIn);
    public CsiSimpleBuffer appendElement(byte byteIn);
    public CsiSimpleBuffer appendCharacterBytes(byte[] bufferIn);
    public CsiSimpleBuffer appendCharacterBytes(byte[] bufferIn, int baseIn, int limitIn);
    public int start();
    public int length();
    public CsiSimpleBuffer truncate();
    public CsiSimpleBuffer truncate(int valueIn);
    public CsiSimpleBuffer clip(int valueIn);
    public CsiSimpleBuffer stripQuotes();
    public CsiSimpleBuffer shiftOut(int locationIn);
    public char[] copyCharacters();
    public byte[] copyBytes();
    public byte[] copyBytes(int baseIn);
    public byte[] copyBytes(int baseIn, int limitIn);
    public boolean equals(String valueIn, int startIn);
    public boolean equals(String valueIn, int startIn, int stopIn);
    public boolean lastValueEquals(final Integer valueIn);
}
