// Implementación de la interfaz de servidor que define los métodos remotos
// para iniciar la carga y descarga de ficheros
package afs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.RandomAccess;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ViceImpl extends UnicastRemoteObject implements Vice {
  /**
   *
   */

   private LockManager lock;
   public ReentrantReadWriteLock lockManager;
   public static HashMap<String, ArrayList<VenusCB>> hm;
   public VenusCB callback;
   public String fileName;

  public ViceImpl() throws RemoteException {
    lock = new LockManager();
    hm = new HashMap<>();
  }

  public ViceReader download(String fileName, String mode,  VenusCB callback /* añada los parámetros que requiera */)
      throws RemoteException, FileNotFoundException {
        ViceReaderImpl reader = null;
    try {

      // @TODO Crear instancia ViceReaderImpl que se retornara
      // Si hizo esto pero ...
      System.out.println("Data downloaded desde ViceImpl");
      this.callback = callback;
      this.fileName = fileName;
      lockManager = lock.bind(fileName);

      if (!hm.containsKey(fileName)) {
        hm.put(fileName, new ArrayList<>());
        hm.get(fileName).add(callback); // Puede haber problemas si se cierra el cliente y no el servidor
      } else {
        hm.get(fileName).add(callback);
      }
    
      
      reader = new ViceReaderImpl(fileName, mode, this);
      // Aqui abri que pensar a poner un synchornised
    }catch(FileNotFoundException e){
      throw new FileNotFoundException(); 
  }catch (Exception e) {
      // TODO Auto-generated catch block
      System.out.println("Problema con el ViceReader");
      e.printStackTrace();
      return null;
    } 

      return reader;       
  }
  
    public ViceWriter upload(String fileName, String mode, VenusCB callback /* añada los parámetros que requiera */)
          throws RemoteException {
            ViceWriterImpl writer = null;
            try{
              writer = new ViceWriterImpl(fileName, mode, this);
            }catch(Exception e){
              
            }

        lockManager.writeLock().lock();

        return writer;
    }
}
