// Implementación de la interfaz de cliente que define los métodos remotos
// para gestionar callbacks
package afs;

import java.io.File;
import java.io.IOException;
import java.rmi.*;
import java.rmi.server.*;

public class VenusCBImpl extends UnicastRemoteObject implements VenusCB {
    VenusFile venusFile;

    public VenusCBImpl(VenusFile venusFile) throws RemoteException {
        this.venusFile = venusFile;
    }

    public void invalidate(String fileName /* añada los parámetros que requiera */) throws RemoteException {
        System.out.println("Voy a invalidar el fichero: " + fileName);
    
        File file = new File("Cache/" + fileName);
            if (file.exists()) {
                file.delete();
            }
            
            //Delete del file
        return;
    }
}

