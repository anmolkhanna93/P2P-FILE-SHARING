package org.ufl.cise.cn.file;

import org.ufl.cise.cn.conf.CommonConfig;
import org.ufl.cise.cn.exceptions.BitTorrentPrototypeException;
import org.ufl.cise.cn.log.Logger;
import java.io.*;


public class FileSplit {

    String filePath;
    private final File file;
    private final File filePartDirectory;
    private static final String partsLocation = "parts/";
    private static String fileName;
    public FileSplit(int peerId, String fileName){
        filePath = "./peer_" + peerId + "/" + partsLocation + fileName;
        filePartDirectory = new File(filePath);
        filePartDirectory.mkdirs();
        file = new File(filePartDirectory.getParent() + "/../" + fileName);
        this.fileName = fileName;
    }

    public byte[] convertPartToBytes(int index) {
        File file = new File(filePartDirectory.getPath() + "/part_" + index + ".part");

        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            int length = (int) file.length();
            byte[] fileBytes = new byte[length];
                    inputStream.read(fileBytes, 0, length);
            inputStream.close();
            return fileBytes;
        } catch (FileNotFoundException e) {
            Logger.getLogger().error(e);
        } catch (IOException e) {
            Logger.getLogger().error(e);
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (IOException ex) {}
            }
        }
        return null;
    }

    public void writeFilePart(byte[] payload, int partId){
        FileOutputStream outputStream;
        File newFileName = new File(filePartDirectory.getPath() + "/part_" + partId + ".part");
        try {
            outputStream = new FileOutputStream(newFileName);
            outputStream.write(payload);
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            Logger.getLogger().error(e);
        } catch (IOException e) {
            Logger.getLogger().error(e);
        }
    }

    public void mergeParts(int pieceCount)
            throws NumberFormatException, BitTorrentPrototypeException {
        byte fileContent[] = new byte[Integer.parseInt(CommonConfig.getProperty(CommonConfig.FileSize.toString()))];
        int byteReadSoFar = 0;
        try {
            for (int pieceNumber = 0; pieceNumber < pieceCount; pieceNumber++) {
                File pieceFile = new File(file.getParent() + "/parts/" +
                        file.getName() + "/"  + "/part_" + pieceNumber + ".part");
                InputStream inStream = new BufferedInputStream(new FileInputStream(pieceFile));
                int pieceLength = (int) pieceFile.length();
                inStream.read(fileContent, byteReadSoFar, pieceLength);
                byteReadSoFar += pieceLength;
                inStream.close();
            }
            OutputStream output = new BufferedOutputStream(
                    new FileOutputStream(filePartDirectory.getParent() + "/../" + fileName));
            output.write(fileContent);
            output.close();


        } catch (IOException ioException) {
            throw new BitTorrentPrototypeException(ioException.getMessage());
        }
    }

    public int split(int pieceSize) throws NumberFormatException, BitTorrentPrototypeException {
        String newFileName = file.getParent() + "/parts/" +
                file.getName() + "/" ;
        int fileSize = (int) file.length();
        int pieceCount = 0;
        byte[] tempBuffer = null;
        try {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            int bytesSizeReadSoFar = 0;
            try {
                inputStream = new BufferedInputStream(new FileInputStream(file));
                while (bytesSizeReadSoFar < fileSize) {
                    String pieceFileName = "part_" + pieceCount + ".part";
                    int remainingBytes = fileSize - bytesSizeReadSoFar;
                    if (remainingBytes < pieceSize) {
                        pieceSize = remainingBytes;
                    }
                    tempBuffer = new byte[pieceSize];
                    int bytesRead = inputStream.read(tempBuffer, 0, pieceSize);
                    if (bytesRead > 0) {
                        bytesSizeReadSoFar += bytesRead;
                        pieceCount++;
                    }
                    try {
                        outputStream = new BufferedOutputStream(
                                new FileOutputStream(newFileName + pieceFileName));
                        outputStream.write(tempBuffer);
                    } finally {
                        outputStream.close();
                    }
                }

            } finally {
                inputStream.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new BitTorrentPrototypeException(ex.getMessage());
        }

        return pieceCount;
    }
}