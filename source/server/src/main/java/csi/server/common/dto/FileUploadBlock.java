package csi.server.common.dto;

import java.util.Base64;

import com.google.common.annotations.GwtIncompatible;
import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.exception.CentrifugeException;

/**
 * Created by centrifuge on 7/14/2015.
 */
public class FileUploadBlock implements IsSerializable {
   private static final String BASE64_TAG = "base64,";
   private static final int BASE64_TAG_SIZE = BASE64_TAG.length();

   private String fileName;
   private int blockNumber;
   private long blockSize;
   private String base64Block;

   public FileUploadBlock() {
   }

   public FileUploadBlock(String fileName, int blockNumber, long blockSize, String base64Block) {
      this.fileName = fileName;
      this.blockNumber = blockNumber;
      this.blockSize = blockSize;
      this.base64Block = base64Block;
   }

   public String getFileName() {
      return fileName;
   }
   public int getBlockNumber() {
      return blockNumber;
   }
   public long getBlockSize() {
      return blockSize;
   }
   public String getBase64Block() {
      return base64Block;
   }

   public void setFileName(String fileName) {
      this.fileName = fileName;
   }
   public void setBlockNumber(int blockNumber) {
      this.blockNumber = blockNumber;
   }
   public void setBlockSize(long blockSize) {
      this.blockSize = blockSize;
   }
   public void setBase64Block(String base64Block) {
      this.base64Block = base64Block;
   }

   @GwtIncompatible("For server side decoding")
   public byte[] getBlock() throws CentrifugeException {
      if (base64Block != null) {
         int offset = base64Block.indexOf(BASE64_TAG);

         if (offset >= 0) {
            return Base64.getDecoder().decode(base64Block.substring(BASE64_TAG_SIZE + offset));
         }
      }
      throw new CentrifugeException("Unrecognized Base64 format!");
   }
}
