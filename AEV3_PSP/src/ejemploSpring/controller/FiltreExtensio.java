package ejemploSpring.controller;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Clase que implementa la interfaz FilenameFilter para filtrar archivos por su extensión.
 */
public class FiltreExtensio implements FilenameFilter {
    private String extensio;

    /**
     * Constructor de la clase FiltreExtensio.
     *
     * @param extensio Extensión de archivo a filtrar.
     */
    public FiltreExtensio(String extensio) {
        this.extensio = extensio;
    }

    /**
     * Método de la interfaz FilenameFilter que determina si un archivo debe ser aceptado o no
     * basándose en su extensión.
     *
     * @param dir  Directorio en el que se encuentra el archivo.
     * @param name Nombre del archivo.
     * @return true si el archivo tiene la extensión especificada, false en caso contrario.
     */
    public boolean accept(File dir, String name) {
        return name.endsWith(extensio);
    }
}
