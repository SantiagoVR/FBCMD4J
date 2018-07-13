package org.fbcmd4j;

import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Properties;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.ResponseList;
import facebook4j.Post;

public class Main {
	private static final Logger logger = LogManager.getLogger(Main.class);
	private static final String CONFIG_DIR = "config"; 
	private static final String CONFIG_FILE = "fbcmd4j.properties";
	private static final String APP_VERSION = "v1.0";
	
	public static void main(String[] args) {
		logger.info("Inicializando app");
		Facebook facebook = null;
		Properties props = null;
		// Carga propiedades
		try {
			props = Utils.loadConfigFile(CONFIG_DIR, CONFIG_FILE);
		} catch (IOException ex) {	
			System.out.println(ex);
			logger.error(ex);
		}
		
		int seleccion;
		try (Scanner scanner = new Scanner(System.in)){
			while(true){
				facebook = Utils.configuraFacebook(props);
				// Inicio Menu
				System.out.format("Simple Facebook client %s\n\n", APP_VERSION);
				System.out.println("Opciones: ");
				System.out.println("(0) Obtener el newsfeed");
				System.out.println("(1) Obtener el wall");
				System.out.println("(2) Publicar estado");
				System.out.println("(3) Publicar un link");
				System.out.println("(4) Salir");
				System.out.println("\nPor favor ingrese una opcion: ");
				// Fin de Menu
				try {
					seleccion = scanner.nextInt();
					scanner.nextLine();

					switch(seleccion){
						case 0:
							System.out.println("Newsfeed");
							ResponseList<Post> list = facebook.getFeed();
							for(Post newsfeed : list){
								Utils.printPost(newsfeed);
							}
							guardarArchivo("NewsFeed", list, scanner);
							break;
						case 1:
							System.out.println("Wall");
							ResponseList<Post> muro = facebook.getPosts();
							for (Post wall : muro) {
								Utils.printPost(wall);
							}
							guardarArchivo("Wall", muro, scanner);
							break;
						case 2:
							System.out.println("Escribe tu estado: ");
							String estado = scanner.nextLine();
							Utils.postStatus(estado, facebook);
							break;
						case 3:
							System.out.println("Escribe tu link: ");
							String link = scanner.nextLine();
							Utils.postLink(link, facebook);
							break;
						case 4:
							System.exit(0);
						default:
							logger.error("Opcion invalida");
							break;
					}
				} catch (InputMismatchException ex){
					System.out.println("Ocurrio un errror, favor de revisar log.");
					logger.error("Opcion invalida. %s. \n", ex.getClass());
					scanner.next();
				} catch (FacebookException ex){
					ex.printStackTrace();
					System.out.println("Ocurrio un errror, favor de revisar log.");
					logger.error(ex.getErrorMessage());
					scanner.next();
				} catch (Exception ex){
					System.out.println("Ocurrio un errror, favor de revisar log.");
					logger.error(ex);
					scanner.next();
				} 
			}
		} catch (Exception ex){
			logger.error(ex);
		}
	}
	public static void guardarArchivo(String fileName, ResponseList<Post> posts, Scanner scanner) {
		System.out.println("¿Guardar todos los resultados en un archivo de texto? Si/No");
		String option = scanner.nextLine();
		
		if (option.contains("Si") || option.contains("si")) {
			List<Post> archivo = new ArrayList<>();
			int numero = 0;

			while(numero <= 0) {
				try {
					System.out.println("¿Cuantos posts deseas guardar?");
					numero = Integer.parseInt(scanner.nextLine());					
			
					if(numero <= 0) {
						System.out.println("Ingresar el numero");
					} else {
						for(int i = 0; i<numero; i++) {
							if(i>posts.size()-1) break;
							archivo.add(posts.get(i));
						}
					}
				} catch(NumberFormatException e) {
					logger.error(e);
				}
			}

			Utils.archivoGuardar(fileName, archivo);
		}
	}
}