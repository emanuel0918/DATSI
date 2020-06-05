// Clase de cliente que inicia la interacción con el servicio de
// ficheros remotos
package afs;

import java.rmi.*; 

public class Venus {

    public Vice srv;
    public Venus() {
         if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());
        try{
        srv = (Vice) Naming.lookup("//" + System.getenv("REGISTRY_HOST") + ":" + System.getenv("REGISTRY_PORT") + "/AFS");
        } catch (RemoteException e) {
            System.err.println("Error de comunicacion: " + e.toString());
        }
        catch (Exception e) {
            System.err.println("Excepcion en ClienteBanco:");
            e.printStackTrace();
        }
    }
}
