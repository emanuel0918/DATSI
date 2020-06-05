// Implementación de la interfaz de servidor que define los métodos remotos
// para completar la carga de un fichero
package afs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.rmi.*;
import java.rmi.server.*;
import java.sql.Wrapper;
import java.util.ArrayList;

public class ViceWriterImpl extends UnicastRemoteObject implements ViceWriter {
    private static final String AFSDir = "AFSDir/";
    public RandomAccessFile file;
    public int offset;
    private ViceImpl vice;


    public ViceWriterImpl(String fileName, String mode, Vice vice /* añada los parámetros que requiera */)
            throws RemoteException, FileNotFoundException {
        file = new RandomAccessFile(AFSDir + fileName, mode);
        offset = 0;
        this.vice = (ViceImpl) vice;
        try {
            file.seek(0);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void write(byte[] b) throws RemoteException {
        try {
            file.write(b);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void close() throws RemoteException {
        vice.lockManager.writeLock().unlock();
        VenusCB temp;
        try {
            file.close();
            ArrayList<VenusCB> list = ViceImpl.hm.get(vice.fileName);
            ArrayList<VenusCB> newList = new ArrayList<>();
            list.forEach(i -> {
                if (i != vice.callback) {
                    try {
                        i.invalidate(vice.fileName);
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }else{
                    newList.add(i);
                }
            });
            ViceImpl.hm.put(vice.fileName, newList);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}       

