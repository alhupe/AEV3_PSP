package ejemploSpring.controller;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@RestController
public class Controller {
    File directorio = new File("Pelis");
    String[] llistaFitxers = directorio.list(new FiltreExtensio(".txt"));

    @RequestMapping("/APIpelis/t")
    String apiPelis(@RequestParam String id) {
        if (id.equals("all")) {
            // Leer el contenido de todos los archivos en el directorio y construir el JSON
            List<Pelicula> peliculas = obtenerPeliculasDesdeDirectorio(directorio.getAbsolutePath());
            return construirJson(peliculas).toString();
        } else {
            // Obtener información específica de la película con el ID proporcionado
            Pelicula pelicula = obtenerPeliculaPorId(Integer.parseInt(id));

            // Verificar si la película existe
            if (pelicula != null) {
                List<String> ressenyes = obtenerResenyes(pelicula.getId(), directorio.getAbsolutePath() + "/" + pelicula.getId() + ".txt");
                return construirJsonConResenyes(pelicula.getId(), pelicula.getTitulo(), ressenyes).toString();
            } else {
                // Película no encontrada, devolver código 404
                return new ResponseEntity<>(HttpStatus.NOT_FOUND).toString();
            }
        }
    }

    @PostMapping("/APIpelis/novaRessenya")
    ResponseEntity<String> novaRessenya(@RequestBody NovaResenya novaResenya) {
        String idPelicula = novaResenya.getId();
        String nomUsuari = novaResenya.getUsuari();
        String textResenya = novaResenya.getResenya();

        // Buscar el archivo de la película correspondiente al "idPelicula"
        String rutaArchivo = directorio.getAbsolutePath() + "/" + idPelicula + ".txt";

        // Construir la nueva reseña en el formato deseado
        String novaResenyaFormat = nomUsuari + ": " + textResenya;

        // Escribir la nueva reseña al final del archivo
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(rutaArchivo, true))) {
            writer.newLine();
            writer.write(novaResenyaFormat);
        } catch (IOException e) {
            e.printStackTrace();
            // En caso de error, devolver un código 500 (Internal Server Error)
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Devolver un código 204 (No Content) ya que la respuesta no tiene contenido
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    private List<String> obtenerResenyes(String id, String rutaArchivo) {
        List<String> ressenyes = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.startsWith("Usuari")) {
                    // Extraer la reseña del usuario y añadir a la lista de reseñas
                    String reseña = linea;
                    ressenyes.add(reseña);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ressenyes;
    }

    private String obtenerTituloDesdeArchivo(String rutaArchivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.startsWith("Titulo: ")) {
                    // Extraer el título
                    return linea.replace("Titulo: ", "");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    private Pelicula obtenerPeliculaPorId(int id) {
        for (String nombreArchivo : llistaFitxers) {
            int currentId = obtenerIdDesdeNombreArchivo(nombreArchivo);
            if (currentId == id) {
                // Encontrar la película con el ID proporcionado
                return new Pelicula(String.valueOf(currentId), obtenerTituloDesdeArchivo(directorio.getAbsolutePath() + "/" + nombreArchivo), "");
            }
        }
        return null;
    }

    private JSONObject construirJsonConResenyes(String id, String titulo, List<String> ressenyes) {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        for (String reseña : ressenyes) {
            jsonArray.put(reseña);
        }

        jsonObject.put("id", id);
        jsonObject.put("titol", titulo);
        jsonObject.put("ressenyes", jsonArray);

        return jsonObject;
    }

    private List<Pelicula> obtenerPeliculasDesdeDirectorio(String directorio) {
        List<Pelicula> peliculas = new ArrayList<>();

        for (String nombreArchivo : llistaFitxers) {
            int id = obtenerIdDesdeNombreArchivo(nombreArchivo);
            peliculas.addAll(obtenerPeliculasDesdeArchivo(directorio + "/" + nombreArchivo, id));
        }

        return peliculas;
    }

    private int obtenerIdDesdeNombreArchivo(String nombreArchivo) {
        // Extraer el número del nombre del archivo
        String numeroArchivo = nombreArchivo.replaceAll("[^0-9]", "");
        try {
            return Integer.parseInt(numeroArchivo);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private List<Pelicula> obtenerPeliculasDesdeArchivo(String rutaArchivo, int id) {
        List<Pelicula> peliculas = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            String titulo = "";

            while ((linea = br.readLine()) != null) {
                if (linea.startsWith("Titulo: ")) {
                    // Extraer el título
                    titulo = linea.replace("Titulo: ", "");
                }
            }

            // Añadir una única instancia de Pelicula por cada archivo (id único)
            if (!titulo.isEmpty()) {
                peliculas.add(new Pelicula(String.valueOf(id), titulo, ""));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return peliculas;
    }

    private JSONObject construirJson(List<Pelicula> peliculas) {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        for (Pelicula pelicula : peliculas) {
            JSONObject peliculaJson = new JSONObject();
            peliculaJson.put("id", pelicula.getId());
            peliculaJson.put("titol", pelicula.getTitulo());
            jsonArray.put(peliculaJson);
        }

        jsonObject.put("titols", jsonArray);

        // Devolver el objeto JSON sin modificar el orden de las claves
        return jsonObject;
    }

    // Clase para representar una película con identificador, título y número de
    // usuario
    private static class Pelicula {
        private final String id;
        private final String titulo;
        private final String numeroUsuario;

        public Pelicula(String id, String titulo, String numeroUsuario) {
            this.id = id;
            this.titulo = titulo;
            this.numeroUsuario = numeroUsuario;
        }

        public String getId() {
            return id;
        }

        public String getTitulo() {
            return titulo;
        }

        public String getNumeroUsuario() {
            return numeroUsuario;
        }
    }

    private static class NovaResenya {
        private String usuari;
        private String id;
        private String ressenya;

        // Agregar getters y setters

        public String getUsuari() {
            return usuari;
        }

        public void setUsuari(String usuari) {
            this.usuari = usuari;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getResenya() {
            return ressenya;
        }

        public void setResenya(String ressenya) {
            this.ressenya = ressenya;
        }
    }
}
