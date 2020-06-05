// Clase de cliente que define la interfaz a las aplicaciones.
// Proporciona la misma API que RandomAccessFile.
package afs;

import java.rmi.*;
import java.util.ArrayList;

import java.util.List;
import java.io.*;

//

public class VenusFile {
    public static final String cacheDir = "Cache/";
    private ViceReader reader;
    private ViceWriter writer;
    private RandomAccessFile file;
    private final Venus venus;
    private final String mode;
    private final String fileName;
    private boolean fileChange; 
    private VenusCB callback;
    private static int blocksize = Integer.parseInt(System.getenv("BLOCKSIZE"));

    public VenusFile(final Venus venus, final String fileName, final String mode) throws RemoteException, IOException {
        // @TODO Buscamos el fichero en el cache antes de abrirlo desde el servidor
        this.venus = venus;
        this.fileName = fileName;
        this.mode = mode;
        reader = null;
        this.file = null;

        try {

            final File file2 = new File(cacheDir + fileName);

            callback = new VenusCBImpl(this);

            if (!file2.exists()) {
                // EL fichero no existe en cache
                reader = venus.srv.download(fileName, mode, callback); // Tenemos acceso al fichero remoto para poder
                                                                       // leerlo mas tarde

                if (reader == null) {
                    throw new FileNotFoundException();
                }
                // Vamos a meter el fichero en cache
                file = new RandomAccessFile(cacheDir + fileName, "rw"); // Para nuestra copia local no queda otra si
                                                                        // queremos crearla
                byte[] temp = new byte[blocksize];
                temp = null;
                temp = reader.read(blocksize);
                while (temp != null) {
                    file.write(temp); // Aqui se va a mover el puntador solo
                    temp = new byte[blocksize];
                    temp = reader.read(blocksize);
                }

                file.close(); // Lo cerramos
                file = new RandomAccessFile(cacheDir + fileName, mode); //Nos permite poner el modo que queriamos desde el principio (feo)
                fileChange = false; // Recuperamos el tamano

                file.seek(0); // iniciaremos el puntero a 0

            } else {
                // El fichero si existe
                file = new RandomAccessFile(cacheDir + fileName, mode); // No podemos hacer eso antes porque sino
                                                                        // creariamos el fichero en el cache.
            }

        } catch (FileNotFoundException e) {
            throw new FileNotFoundException();
        } catch (Exception e) {

        }

    }

    public int read(byte[] b) throws RemoteException, IOException {
        if (file == null) {
            // El fichero es remoto
            byte[] temp;
            if (b.length < blocksize) {
                temp = new byte[b.length];
            } else {
                temp = new byte[blocksize];
            }
            int i = 0;
            temp = reader.read(temp.length);
            while (temp != null) {
                for (byte c : temp) {
                    b[i] = c;
                    i++;
                }
                temp = new byte[blocksize];
                if (b.length - i == 0) {
                    return i;
                } else if (b.length - i < blocksize) {
                    temp = new byte[b.length - i];
                } else {
                    temp = new byte[blocksize];
                }
                // Acabamos leyendo los datos
                temp = reader.read(temp.length);
            }

            return i;
        } else {
            // El fichero no es remoto
            // file.seek(0); Parece ser que no es un comportamieto deseado
            int res = file.read(b);
            System.out.println("Res: \n" + res);
            return res;
        }
        // final String file = "cliente.txt";
        // VenusFile.comand("rm " + file);
        // VenusFile.comand("echo >> " + file);
        // try (FileOutputStream fileOuputStream = new FileOutputStream(file)) {
        // fileOuputStream.write(b);
        // } catch (final Exception e) {
        // }

    }

    public void write(final byte[] b) throws RemoteException, IOException {
        if (file == null) {
            // El fichero aun es remoto
            // Se crea una copia en el cache
            // Se supone que se puede escribir, si no es el caso tendremos un error mas
            // adelante
            file = new RandomAccessFile(cacheDir + fileName, mode);
            byte[] temp = new byte[blocksize];
            temp = null;
            System.out.println("We are going to download the files");
            temp = reader.read(blocksize);
            while (temp != null) {
                System.out.println("Escribimos");
                file.write(temp); // Aqui se va a mover el puntador solo
                temp = new byte[blocksize];
                temp = reader.read(blocksize);
            }
            fileChange = false; // Recuperamos el tamano
        } // Eso no deberia existir pero por si acas

        System.out.println("File on Cache");
        System.out.println("El puntador esta: " + file.getFilePointer());
        // Ahora tenemos el fichero en cache
        /*if (file.length() == 0) {
            file.seek(0);
        } else {
            file.seek(file.length()); // Nos colocamos en el ultimo sitio del fichero para escribir
        }*/ //Eso permitiria siempre escribir al final
        System.out.println("Length before writing: " + file.length());
        file.write(b); // Escribimos en el fichero
        fileChange = true;
        System.out.println("Length after writing: " + file.length());

    }

    public void seek(final long p) throws RemoteException, IOException {
        if(p>file.length()){
            file.seek(file.length());
        }else{
            file.seek(p);
        }
    }

    public void setLength(final long l) throws RemoteException, IOException {
        if(l  > file.length()){
        //Para evitar todo problema vamos a anadir manualente varios caracteres vacios
            byte c = ' ';
            byte [] buf = new byte[(int)l];
            for (int i=0; i<l; i++) buf[i] = (byte)c;
        }else{
            file.setLength(l);
        }
    }

    public void close() throws RemoteException, IOException {
        if (file == null) {
            // El fichero es remoto, podemos suponer que no hubo modificaciones
            reader.close();
        } else if (fileChange == false){
            reader.close();
            file.close();
        } else {
            // En ese caso puede haber modificaciones
            reader.close(); // Vamos a necesitar un writer
            // @TODO verificar que hay modificaciones antes de hacer el writer
            writer = venus.srv.upload(fileName, mode, callback);
            // Tenemos acceso al fichero para poder escribir dentro

            byte[] temp;
            file.seek(0); 
            int pointer = 0;
            if (fileChange == false) { //Cambiar la variable initialLength
                // No se hace nada
                System.out.println("No hemos echo ninguna modificacion");
                temp = null;
            } else if (file.length() < blocksize) {
                temp = new byte[(int) (file.length())];
                file.seek(0); //Colocamos el puntador al principio del documento
                file.read(temp, 0, (int) (file.length()));

            } else {
                temp = new byte[blocksize];
                file.seek(0);
                file.read(temp, 0, temp.length);
                pointer += blocksize;
            }
            while (temp != null) {
                writer.write(temp);
                if (file.length() == pointer) {
                    temp = null;
                } else if (file.length() - pointer < blocksize) {
                    temp = new byte[(int) (file.length() - pointer)];
                    file.seek(pointer);
                    file.read(temp, 0, temp.length);
                    pointer = (int)file.length();
                } else {
                    temp = new byte[blocksize];
                    file.seek(pointer);
                    file.read(temp, 0, temp.length);
                    pointer += blocksize;
                }
            }
            file.close();
            writer.close();
        }
    }
}
