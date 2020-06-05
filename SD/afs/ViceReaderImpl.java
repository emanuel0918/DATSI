// Implementación de la interfaz de servidor que define los métodos remotos
// para completar la descarga de un fichero
package afs;

import java.io.*;
import java.rmi.*;
import java.rmi.server.*;

public class ViceReaderImpl extends UnicastRemoteObject implements ViceReader {
    private static final String AFSDir = "AFSDir/";
    public RandomAccessFile file;
    public int offset; //Realmente no se necesita pero nos evita hacer llamadas de funciones
    private ViceImpl vice;

    public ViceReaderImpl(final String fileName, final String mode, Vice vice /* añada los parámetros que requiera */)
            throws RemoteException, FileNotFoundException {

            try{
                file = new RandomAccessFile(AFSDir + fileName, mode);
                offset = 0;
                this.vice = (ViceImpl) vice;
            }catch(FileNotFoundException e){
                throw new FileNotFoundException(); 
            }
        
    }

    public byte[] read(final int tam) throws IOException {
        vice.lockManager.readLock().lock();
        byte[] b;
        final long size = file.length();

        if(offset%tam != 0 && offset != size){
            offset = 0;
            file.seek(offset);
        }
        
        if (offset == size) {
            offset = 0;
            file.seek(offset);
            vice.lockManager.readLock().unlock();
            return null;
        } else if (size - offset < tam) {
            b = new byte[(int) size - offset];
            file.read(b, 0, (int) size - offset); 
            offset = (int) size;
            file.seek(offset);
        } else {
            b = new byte[tam];
            file.read(b, 0, tam);
            offset += tam;
            file.seek(offset);
        }

        //We are going to consider than if we read only a part or the entire files we can go to inital position again
        vice.lockManager.readLock().unlock();
        return b;
    }

    public void close() throws RemoteException {
        try {
            file.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}       

