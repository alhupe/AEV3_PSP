package ejemploSpring.controller;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Clase controladora para manejar las solicitudes de la API relacionadas con
 * películas.
 */
@RestController
public class Controller {
	File directorio = new File("Pelis");
	String[] llistaFitxers = directorio.list(new FiltreExtensio(".txt"));
	String autoritzatsPath = "autoritzats.txt"; // Ruta del archivo de usuarios autorizados

	/**
	 * Recupera información de películas según el ID proporcionado.
	 *
	 * @param id El ID de la película o "all" para obtener información sobre todas
	 *           las películas.
	 * @return Representación JSON de películas o de una película específica.
	 */
	@RequestMapping("/APIpelis/t")
	String apiPelis(@RequestParam String id) {
		if (id.equals("all")) {
			List<Pelicula> peliculas = obtenerPeliculasDesdeDirectorio(directorio.getAbsolutePath());
			return construirJson(peliculas).toString();
		} else {
			Pelicula pelicula = obtenerPeliculaPorId(Integer.parseInt(id));

			if (pelicula != null) {
				List<String> ressenyes = obtenerResenyes(pelicula.getId(),
						directorio.getAbsolutePath() + "/" + pelicula.getId() + ".txt");
				return construirJsonConResenyes(pelicula.getId(), pelicula.getTitulo(), ressenyes).toString();
			} else {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND).toString();
			}
		}
	}

	/**
	 * Obtiene resenyas de una película específica.
	 *
	 * @param id          El ID de la película.
	 * @param rutaArchivo La ruta al archivo que contiene las resenyas.
	 * @return Lista de resenyas de la película.
	 */

	private List<String> obtenerResenyes(String id, String rutaArchivo) {
		List<String> ressenyes = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
			String linea;
			while ((linea = br.readLine()) != null) {
				if (linea.startsWith("Usuari")) {
					String resenya = linea;
					ressenyes.add(resenya);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ressenyes;
	}

	/**
	 * Obtiene el título de una película desde el archivo correspondiente.
	 *
	 * @param rutaArchivo La ruta al archivo que contiene la información de la
	 *                    película.
	 * @return El título de la película.
	 */

	private String obtenerTituloDesdeArchivo(String rutaArchivo) {
		try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
			String linea;
			while ((linea = br.readLine()) != null) {
				if (linea.startsWith("Titulo: ")) {
					return linea.replace("Titulo: ", "");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "";
	}

	/**
	 * Obtiene una película según su ID.
	 *
	 * @param id El ID de la película.
	 * @return Objeto Pelicula si se encuentra, de lo contrario, null.
	 */
	private Pelicula obtenerPeliculaPorId(int id) {
		for (String nombreArchivo : llistaFitxers) {
			int currentId = obtenerIdDesdeNombreArchivo(nombreArchivo);
			if (currentId == id) {
				String rutaArchivo = directorio.getAbsolutePath() + "/" + nombreArchivo;
				List<String> resenhas = obtenerResenyes(String.valueOf(currentId), rutaArchivo);
				return new Pelicula(String.valueOf(currentId), obtenerTituloDesdeArchivo(rutaArchivo), resenhas);
			}
		}
		return null;
	}

	/**
	 * Construye un objeto JSON con resenyas de una película.
	 *
	 * @param id        El ID de la película.
	 * @param titulo    El título de la película.
	 * @param ressenyes Lista de resenyas de la película.
	 * @return Objeto JSON con la información de la película y sus resenyas.
	 */

	private JSONObject construirJsonConResenyes(String id, String titulo, List<String> ressenyes) {
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();

		for (String resenya : ressenyes) {
			jsonArray.put(resenya);
		}

		jsonObject.put("id", id);
		jsonObject.put("titol", titulo);
		jsonObject.put("ressenyes", jsonArray);

		return jsonObject;
	}

	/**
	 * Obtiene la lista de películas desde el directorio especificado.
	 *
	 * @param directorio Ruta del directorio de películas.
	 * @return Lista de películas.
	 */

	private List<Pelicula> obtenerPeliculasDesdeDirectorio(String directorio) {
		List<Pelicula> peliculas = new ArrayList<>();

		for (String nombreArchivo : llistaFitxers) {
			int id = obtenerIdDesdeNombreArchivo(nombreArchivo);
			peliculas.addAll(obtenerPeliculasDesdeArchivo(directorio + "/" + nombreArchivo, id));
		}

		return peliculas;
	}

	/**
	 * Obtiene el ID de una película a partir de su nombre de archivo.
	 *
	 * @param nombreArchivo El nombre del archivo de la película.
	 * @return El ID de la película.
	 */

	private int obtenerIdDesdeNombreArchivo(String nombreArchivo) {
		String numeroArchivo = nombreArchivo.replaceAll("[^0-9]", "");
		try {
			return Integer.parseInt(numeroArchivo);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * Obtiene películas desde un archivo específico.
	 *
	 * @param rutaArchivo La ruta al archivo que contiene la información de las
	 *                    películas.
	 * @param id          El ID de la película.
	 * @return Lista de películas.
	 */

	private List<Pelicula> obtenerPeliculasDesdeArchivo(String rutaArchivo, int id) {
		List<Pelicula> peliculas = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
			String linea;
			String titulo = "";
			List<String> resenhas = new ArrayList<>();

			while ((linea = br.readLine()) != null) {
				if (linea.startsWith("Titulo: ")) {
					titulo = linea.replace("Titulo: ", "");
				} else if (linea.startsWith("Usuari")) {
					resenhas.add(linea);
				}
			}

			if (!titulo.isEmpty()) {
				peliculas.add(new Pelicula(String.valueOf(id), titulo, resenhas));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return peliculas;
	}

	/**
	 * Construye un objeto JSON con la información de varias películas.
	 *
	 * @param peliculas Lista de películas.
	 * @return Objeto JSON con la información de las películas.
	 */

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

		return jsonObject;
	}

	/**
	 * Maneja la solicitud para agregar una nueva resenya de película.
	 *
	 * @param requestBody Cuerpo de la solicitud que contiene la información de la
	 *                    nueva resenya.
	 * @return Respuesta HTTP indicando el resultado de la operación.
	 */

	@PostMapping("/APIpelis/novaRessenya")
	ResponseEntity<String> novaRessenya(@RequestBody Map<String, String> requestBody) {
		String idPelicula = requestBody.get("id");
		String nomUsuari = requestBody.get("usuari");
		String textResenya = requestBody.get("ressenya");

		if (usuariAutoritzat(nomUsuari)) {
			String rutaArchivo = directorio.getAbsolutePath() + "/" + idPelicula + ".txt";
			String novaResenyaFormat = nomUsuari + ": " + textResenya;

			try (BufferedWriter writer = new BufferedWriter(new FileWriter(rutaArchivo, true))) {
				writer.newLine();
				writer.write(novaResenyaFormat);
			} catch (IOException e) {
				e.printStackTrace();
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}

			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}

	/**
	 * Maneja la solicitud para agregar una nueva película.
	 *
	 * @param requestBody Cuerpo de la solicitud que contiene la información de la
	 *                    nueva película.
	 * @return Respuesta HTTP indicando el resultado de la operación.
	 */

	@PostMapping("/APIpelis/novaPeli")
	public ResponseEntity<String> novaPelicula(@RequestBody Map<String, String> requestBody) {
		String nomUsuari = requestBody.get("usuari");
		String titolPelicula = requestBody.get("titol");

		if (usuariAutoritzat(nomUsuari)) {
			int nouId = obtenerUltimoId() + 1;
			System.out.println("Nuevo ID: " + nouId);
			String nomFitxer = directorio.getAbsolutePath() + "/" + nouId + ".txt";

			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(nomFitxer));
				writer.write("Titulo: " + titolPelicula);
			} catch (IOException e) {
				e.printStackTrace();
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			llistaFitxers = directorio.list(new FiltreExtensio(".txt"));

			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}

	/**
	 * Maneja la solicitud para agregar un nuevo usuario.
	 *
	 * @param requestBody Cuerpo de la solicitud que contiene la información del
	 *                    nuevo usuario.
	 * @return Respuesta HTTP indicando el resultado de la operación.
	 */

	@PostMapping("/APIpelis/nouUsuari")
	public ResponseEntity<String> nouUsuari(@RequestBody Map<String, String> requestBody) {
		String nomUsuari = requestBody.get("usuari");

		if (usuariRegistrat(nomUsuari)) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(autoritzatsPath, true))) {
			writer.newLine();
			writer.write(nomUsuari);
		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	/**
	 * Verifica si un usuario está autorizado basándose en la lista de usuarios
	 * autorizados.
	 *
	 * @param nomUsuari Nombre de usuario a verificar.
	 * @return true si el usuario está autorizado, false en caso contrario.
	 */
	private boolean usuariAutoritzat(String nomUsuari) {
		List<String> usuarisAutoritzats = obtenirUsuarisAutoritzats();
		return usuarisAutoritzats.contains(nomUsuari);
	}

	/**
	 * Verifica si un usuario está registrado basándose en la lista de usuarios
	 * registrados en las resenyas de películas.
	 *
	 * @param nomUsuari Nombre de usuario a verificar.
	 * @return true si el usuario está registrado, false en caso contrario.
	 */
	private boolean usuariRegistrat(String nomUsuari) {
		List<String> usuarisRegistrats = obtenirUsuarisRegistrats();
		return usuarisRegistrats.contains(nomUsuari);
	}

	/**
	 * Obtiene la lista de usuarios autorizados desde el archivo de usuarios
	 * autorizados.
	 *
	 * @return Lista de usuarios autorizados.
	 */
	private List<String> obtenirUsuarisAutoritzats() {
		List<String> usuaris = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(autoritzatsPath))) {
			String linea;
			while ((linea = br.readLine()) != null) {
				usuaris.add(linea);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return usuaris;
	}

	/**
	 * Obtiene la lista de usuarios registrados a través de las resenyas de todas las
	 * películas.
	 *
	 * @return Lista de usuarios registrados.
	 */
	private List<String> obtenirUsuarisRegistrats() {
		List<String> usuaris = new ArrayList<>();
		for (String nombreArchivo : llistaFitxers) {
			int id = obtenerIdDesdeNombreArchivo(nombreArchivo);
			List<Pelicula> peliculas = obtenerPeliculasDesdeArchivo(directorio + "/" + nombreArchivo, id);
			for (Pelicula pelicula : peliculas) {
				for (String usuari : pelicula.getResenhas()) {
					usuaris.add(usuari.split(":")[0].trim());
				}
			}
		}
		return usuaris;
	}

	/**
	 * Obtiene el último ID de película existente en la lista de archivos.
	 *
	 * @return El último ID de película encontrado.
	 */
	private int obtenerUltimoId() {
		int ultimoId = 0;
		for (String nombreArchivo : llistaFitxers) {
			int id = obtenerIdDesdeNombreArchivo(nombreArchivo);
			if (id > ultimoId) {
				ultimoId = id;
			}
		}
		return ultimoId;
	}

	/**
	 * Clase estática interna que representa una película con su ID, título y
	 * resenyas.
	 */
	private static class Pelicula {
		private String id;
		private String titulo;
		private List<String> resenhas;

		/**
		 * Constructor de la clase Pelicula.
		 *
		 * @param id       ID de la película.
		 * @param titulo   Título de la película.
		 * @param resenhas Lista de resenyas de la película.
		 */
		public Pelicula(String id, String titulo, List<String> resenhas) {
			this.id = id;
			this.titulo = titulo;
			this.resenhas = resenhas;
		}

		/**
		 * Obtiene el ID de la película.
		 *
		 * @return ID de la película.
		 */
		public String getId() {
			return id;
		}

		/**
		 * Obtiene el título de la película.
		 *
		 * @return Título de la película.
		 */
		public String getTitulo() {
			return titulo;
		}

		/**
		 * Obtiene la lista de resenyas de la película.
		 *
		 * @return Lista de resenyas de la película.
		 */
		public List<String> getResenhas() {
			return resenhas;
		}
	}

}
